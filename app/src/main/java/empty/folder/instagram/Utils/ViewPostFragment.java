package empty.folder.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.Likes.LikesActivity;
import empty.folder.instagram.Models.Comment;
import empty.folder.instagram.Models.Like;
import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.Models.User;
import empty.folder.instagram.Models.UserAccountSettings;
import empty.folder.instagram.Profile.ProfileActivity;
import empty.folder.instagram.R;
import empty.folder.instagram.Search.SearchActivity;
import empty.folder.instagram.Share.ShareActivity;
import empty.folder.instagram.Utils.GridViewPagerAdapter;
import empty.folder.instagram.Utils.Square_Image_View;
import empty.folder.instagram.Utils.UniversalImageLoader;

import static android.view.View.GONE;

public class ViewPostFragment extends Fragment {

    // widgets
    private Square_Image_View imageView;
    private TextView caption, username, dateTime, imageLikes,commentsCount;
    private ImageView ic_back, ellipses, redHeart, whiteHeart, profileImage, ic_comment;

    private BottomNavigationViewEx bottomNavigationViewEx;

    // variables
    private Photo mPhoto;
    private int activityNumber = 0;
    private String Username;
    private String profileUrl;
    private UserAccountSettings userAccountSettings;
    private GestureDetector gestureDetector;
    private Heart heart;
    private Boolean photoLikedByUser;
    private StringBuilder usersBuilder;
    private String LikesString = "";
    private User mCurrentUser;



    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public interface OnCommentSelectedListener{
        void onCommentSelectedListener(Photo photo);
    }
    OnCommentSelectedListener onCommentSelectedListener;

    public ViewPostFragment(){
        super();
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_view, container, false);

        imageView = view.findViewById(R.id.center_postViewImage);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewEx);
        caption = view.findViewById(R.id.image_captions);
        username = view.findViewById(R.id.username_forPostView);
        dateTime = view.findViewById(R.id.image_dateTime);
        imageLikes = view.findViewById(R.id.image_likes);
        // i will write txtPhoto but it is not correct now
        ic_back = view.findViewById(R.id.ic_back_photo);
        ellipses = view.findViewById(R.id.ic_settings_forPostView);
        redHeart = view.findViewById(R.id.icon_heart_fill_red);
        whiteHeart = view.findViewById(R.id.icon_heart_outline);
        profileImage = view.findViewById(R.id.profile_photo_forPostView);
        ic_comment = view.findViewById(R.id.icon_comment_for_postView);
        commentsCount = view.findViewById(R.id.image_comments_count);

        // objects
        heart = new Heart(redHeart, whiteHeart);

        gestureDetector = new GestureDetector(getActivity(), new GestureListener());

        setupFireBase();
        setupBottomNavigationViewEx();
//        getViewPhotoDetails();

        return view;
    }

    private void init(){

        try {
//            photo = takePhotoFromBundle();
            UniversalImageLoader.setImage(takePhotoFromBundle().getImage_path(), imageView, null, "");
            activityNumber = takeActivityNumberFromBundle();
            String photo_id = takePhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate(objectMap.get(getString(R.string.field_date)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        List<Comment> comments = new ArrayList<Comment>();

                        for (DataSnapshot singleSnapshot: dataSnapshot
                                .child(getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(singleSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(singleSnapshot.getValue(Comment.class).getComment());
                            comment.setDate(singleSnapshot.getValue(Comment.class).getDate());
                            comments.add(comment);
                        }
                        photo.setComments(comments);

                        mPhoto = photo;

                        getCurrentUser();
                        getViewPhotoDetails();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            UniversalImageLoader.setImage(mPhoto.getImage_path(), imageView, null, "");
            activityNumber = takeActivityNumberFromBundle();

        }catch (NullPointerException e){

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()){
            init();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            onCommentSelectedListener = (OnCommentSelectedListener) getActivity();
        }catch(ClassCastException e){

        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            Query query = databaseReference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                        String keyID = dataSnapshot.getKey();

                        // step 1) the user already liked the photo
                        if (photoLikedByUser && dataSnapshot.getValue(Like.class).getUser_id()
                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            databaseReference.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            Toast.makeText(getActivity(), "removed", Toast.LENGTH_SHORT).show();

                            databaseReference.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            heart.toggleLike();
                            // getLikeString();
                        }

                        // step 2) the user already don't like photo
                        else if (!photoLikedByUser){
                            // add new like
                            addNewLike();
//                            Toast.makeText(getActivity(), "for dont equals " + photoLikedByUser, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (!snapshot.exists()){
                        // add new like
//                        Toast.makeText(getActivity(), "due to database", Toast.LENGTH_SHORT).show();
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            return true;
        }
    }

    private void addNewLike(){
        String newLikeId = databaseReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        databaseReference.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        databaseReference.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        heart.toggleLike();
        // getLikeString();

    }



    private void getLikeString(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersBuilder = new StringBuilder();
                for (final DataSnapshot dataSnapshot: snapshot.getChildren()){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getContext().getString(R.string.field_user_id))
                            .equalTo(dataSnapshot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                                usersBuilder.append(dataSnapshot.getValue(User.class).getUsername());
                                usersBuilder.append(",");
                            }

                            String[] splitUsers = usersBuilder.toString().split(",");
                            if (usersBuilder.toString().contains(mCurrentUser.getUsername() + ",")){
                                photoLikedByUser = true;
                            }else{
                                photoLikedByUser = false;
                            }

                            int userscount = splitUsers.length;
                            if (userscount == 1){
                                LikesString = "Liked by " + splitUsers[0];
                            }else if (userscount == 2){
                                LikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                            }else if (userscount == 3){
                                LikesString = "Liked by "
                                        + splitUsers[0] + ", "
                                        + splitUsers[1] + " and "
                                        + splitUsers[2];
                            }else if (userscount == 4) {
                                LikesString = "Liked by "
                                        + splitUsers[0] + ", "
                                        + splitUsers[1] + ", "
                                        + splitUsers[2] + " and "
                                        + splitUsers[3];
                            }else if (userscount > 4){
                                LikesString = "Liked by "
                                        + splitUsers[0] + ", "
                                        + splitUsers[1] + ", "
                                        + splitUsers[2] + " and "
                                        + (splitUsers.length - 3) + " others";
                            }
                            setupWidgets();
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                if (!snapshot.exists()){
                    LikesString = "";
                    photoLikedByUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCurrentUser(){

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    mCurrentUser = dataSnapshot.getValue(User.class);
                }
                // getLikeString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getViewPhotoDetails(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    userAccountSettings = dataSnapshot.getValue(UserAccountSettings.class);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    /**
     * returns a string representing the number of days ago the post was shared
     * @return
     */
    private String getDateTimeDifference(){
        String difference = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        Date today = calendar.getTime();
        simpleDateFormat.format(today);
        Date time;
        final String photoTime = mPhoto.getDate();
        try {
            time = simpleDateFormat.parse(photoTime);
            difference = String.valueOf(Math.round((today.getTime() - time.getTime()) / 100 / 60 / 60 / 24));
        }catch (ParseException e){
            difference = "0";
        }

        return difference;
    }

    private void setupWidgets(){
        String timeDifference = getDateTimeDifference();
        if (!timeDifference.equals("0")){
            dateTime.setText(timeDifference + " DAYS AGO");
        }else{
            dateTime.setText("TODAY");
        }

        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), profileImage, null, "");
        username.setText(userAccountSettings.getUsername());
        imageLikes.setText(LikesString);
        caption.setText(userAccountSettings.getUsername() + " " + mPhoto.getCaption());

        if (mPhoto.getComments().size() == 1){
            commentsCount.setText("View " + mPhoto.getComments().size() + " comment");
        }else if (mPhoto.getComments().size() > 1){
            commentsCount.setText("View all " + mPhoto.getComments().size() + " comments");
        }else{
            commentsCount.setText("");
        }

        if (photoLikedByUser){
            whiteHeart.setVisibility(GONE);
            redHeart.setVisibility(View.VISIBLE);

            redHeart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }else{
            whiteHeart.setVisibility(View.VISIBLE);
            redHeart.setVisibility(View.GONE);

            whiteHeart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }

        commentsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCommentSelectedListener.onCommentSelectedListener(mPhoto);
            }
        });

        ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        ic_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCommentSelectedListener.onCommentSelectedListener(mPhoto);
            }
        });

    }

    /**
     * take the photo from incoming bundle from ProfileActivity interface
     * @return
     */
    private Photo takePhotoFromBundle(){
        Bundle bundle = getArguments();
        if (bundle != null){
            return bundle.getParcelable(getString(R.string.photo));
        }else{
            return null;
        }
    }

    /**
     * take the activity number from incoming bundle from ProfileActivity interface
     * @return
     */
    private int takeActivityNumberFromBundle(){
        Bundle bundle = getArguments();
        if (bundle != null){
            return bundle.getInt(getString(R.string.activity_number));
        }else{
            return 0;
        }
    }

    private void setupFireBase(){

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };
    }

    private void setupBottomNavigationViewEx() {
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.id_home:
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_search:
                        startActivity(new Intent(getActivity(), SearchActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_circle:
                        startActivity(new Intent(getActivity(), ShareActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_alert:
                        startActivity(new Intent(getActivity(), LikesActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_profile:
                        startActivity(new Intent(getActivity(), ProfileActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }


}
