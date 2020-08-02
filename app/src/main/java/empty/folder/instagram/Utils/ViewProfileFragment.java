package empty.folder.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.Likes.LikesActivity;
import empty.folder.instagram.Models.Comment;
import empty.folder.instagram.Models.Like;
import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.Models.User;
import empty.folder.instagram.Models.UserAccountSettings;
import empty.folder.instagram.Models.UserSettings;
import empty.folder.instagram.Profile.Activity_AccountSettings;
import empty.folder.instagram.Profile.ProfileActivity;
import empty.folder.instagram.R;
import empty.folder.instagram.Search.SearchActivity;
import empty.folder.instagram.Share.ShareActivity;

public class ViewProfileFragment extends Fragment {

    // constants
    private static final int NUMBER_OF_COLUMNS = 3;
    private static final int ACTIVITY_NUMBER = 4;

    // user_accoount_settings widgets
    private TextView Posts, Followers, Following, DisplayName, Description, Website, Follow, Unfollow, Username;
    private ProgressBar progressBar;
    private CircleImageView profilePhoto;
    private GridView gridView;
    private TextView editProfile;
    private ImageView ic_back;
    private BottomNavigationViewEx bottomNavigationViewEx;
    Context context;


    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    String userID;

    // variables
    private User user;
    private int followingCount = 0;
    private int followersCount = 0;
    private int postsCount = 0;

    // interface
    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile_in_search, container, false);

        Posts = view.findViewById(R.id.textNumPosts_forPostView);
        Followers = view.findViewById(R.id.textNumFollowers_forPostView);
        Following = view.findViewById(R.id.textNumFollowing_forPostView);
        Website = view.findViewById(R.id.description_link);
        DisplayName = view.findViewById(R.id.description_profile_name);
        Description = view.findViewById(R.id.description_content);
        progressBar = view.findViewById(R.id.profileProgressBar);
        profilePhoto = view.findViewById(R.id.profile_image);
        Username = view.findViewById(R.id.view_profileName);
        Follow = view.findViewById(R.id.textFollow);
        Unfollow = view.findViewById(R.id.textUnfollow);
        ic_back = view.findViewById(R.id.ic_back_forPostView);
        gridView = view.findViewById(R.id.gridView);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewEx);
        editProfile = view.findViewById(R.id.textEditProfile);
        context = getActivity();


        progressBar.setVisibility(View.VISIBLE);

        try {
            user = getUserFromBundle();
            init();
            progressBar.setVisibility(View.INVISIBLE);
        }catch (NullPointerException e){
            Toast.makeText(context, "something is wrong!", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }
        setupBottomNavigationViewEx();
        setupFireBase();


        isFollowing();
        getPostsCount();
        getFollowersCount();
        getFollowingCount();

        Follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(user.getUserid())
                        .child(getString(R.string.field_user_id))
                        .setValue(user.getUserid());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(user.getUserid())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                setFollowing();
            }
        });

        Unfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(user.getUserid())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(user.getUserid())
                        .removeValue();
                setUnfollow();
            }
        });

        // Click To Edit Profile Text

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Activity_AccountSettings.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }

    private void getPostsCount(){
        postsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.
                child(getString(R.string.dbname_user_photos))
                .child(user.getUserid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    postsCount++;
                }
                Posts.setText(String.valueOf(postsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getFollowingCount(){
        followingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.
                child(  getString(R.string.dbname_following))
                .child(user.getUserid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    followingCount++;
                }
                Following.setText(String.valueOf(followingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getFollowersCount(){

        followersCount = 0;


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.
                child(getString(R.string.dbname_followers))
                .child(user.getUserid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    followersCount++;
                }
                Followers.setText(String.valueOf(followersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void init(){

        // set the profile widgets
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference.
                child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(user.getUserid());

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    UserSettings userSettings = new UserSettings();
                    userSettings.setUser(user);
                    userSettings.setUserAccountSettings(dataSnapshot.getValue(UserAccountSettings.class));
                    setProfileWidgets(userSettings);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // get the user profile photos

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query2 = databaseReference
                .child(getString(R.string.dbname_user_photos))
                .child(user.getUserid());

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Photo> photos = new ArrayList<Photo>();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate(objectMap.get(getString(R.string.field_date)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> comments = new ArrayList<Comment>();

                    for (DataSnapshot singleSnapshot: dataSnapshot
                            .child(getString(R.string.field_comments)).getChildren()){
                        Comment comment = new Comment();
                        comment.setUser_id(singleSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(singleSnapshot.getValue(Comment.class).getComment());
                        comment.setDate(singleSnapshot.getValue(Comment.class).getDate());
                        comments.add(comment);
                    }
                    photo.setComments(comments);

                    List<Like> likes = new ArrayList<Like>();
                    for (DataSnapshot singleSnapshot: dataSnapshot
                            .child(getString(R.string.field_likes)).getChildren()){
                        Like like = new Like();
                        like.setUser_id(singleSnapshot.getValue(Like.class).getUser_id());
                        likes.add(like);
                    }
                    photo.setLikes(likes);
                    photos.add(photo);
                }
                setUpGridImageView(photos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void isFollowing(){
        setUnfollow();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.
                child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(user.getUserid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    setFollowing();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setFollowing(){
        Follow.setVisibility(View.GONE);
        Unfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);
    }

    private void setUnfollow(){
        Follow.setVisibility(View.VISIBLE);
        Unfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.GONE);
    }

    private void setCurrentUserProfile(){
        Follow.setVisibility(View.GONE);
        Unfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);
    }

    private void setUpGridImageView(final ArrayList<Photo> photos){
        // setup grid view for user photos
        int gridWith = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWith / NUMBER_OF_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        ArrayList<String> imgUrls = new ArrayList<>();
        for (int i = 0; i<photos.size(); i++){
            imgUrls.add(photos.get(i).getImage_path());
        }

        GridViewPagerAdapter gridViewPagerAdapter = new GridViewPagerAdapter(context, R.layout.layout_grid_imageview, "", imgUrls);
        gridView.setAdapter(gridViewPagerAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUMBER);
            }
        });
    }

    private User getUserFromBundle(){
        Bundle bundle = getArguments();
        if (bundle != null){
            return bundle.getParcelable(getString(R.string.intent_user));
        }else{
            return null;
        }
    }


    private void setupFireBase(){

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

            }
        };

    }

    /**
     * retrieve account settings for the user currently logged in
     * Database: user_account_settings node
     * @param dataSnapshot
     * @return
     */
    private UserSettings getUserSettings(DataSnapshot dataSnapshot){

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseUser.getUid();

        UserAccountSettings userAccountSettings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()){

            // user_account_settings node

            if (ds.getKey().equals(context.getString(R.string.dbname_user_account_settings))){

                try {

                    userAccountSettings.setDisplay_name(ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getDisplay_name());

                    userAccountSettings.setUsername(ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getUsername());

                    userAccountSettings.setDescription(ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getDescription());

                    userAccountSettings.setWebsite(ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getWebsite());

                    userAccountSettings.setProfile_photo(ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getProfile_photo());

                    userAccountSettings.setPosts(ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getPosts());

                    userAccountSettings.setFollowers(ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getFollowers());

                    userAccountSettings.setFollowing(ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getFollowing());

                }catch (NullPointerException e){

                }
            }
            // users node

            if (ds.getKey().equals(context.getString(R.string.dbname_users))){

                user.setUsername(ds.child(userID)
                        .getValue(User.class)
                        .getUsername());

                user.setEmail(ds.child(userID)
                        .getValue(User.class)
                        .getEmail());

                user.setPhone(ds.child(userID)
                        .getValue(User.class)
                        .getPhone());

                user.setUserid(ds.child(userID)
                        .getValue(User.class)
                        .getUserid());

            }


        }

        return new UserSettings(userAccountSettings, user);

    }

    private void setProfileWidgets(UserSettings userSettings){

        UserAccountSettings userAccountSettings = userSettings.getUserAccountSettings();

        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), profilePhoto, null, "");

        DisplayName.setText(userAccountSettings.getDisplay_name());
        Website.setText(userAccountSettings.getWebsite());
        Description.setText(userAccountSettings.getDescription());
        Username.setText(userAccountSettings.getUsername());
        Posts.setText(String.valueOf(userAccountSettings.getPosts()));
        Followers.setText(String.valueOf(userAccountSettings.getFollowers()));
        Following.setText(String.valueOf(userAccountSettings.getFollowing()));

        ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
            }
        });

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
                        startActivity(new Intent(context, HomeActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_search:
                        startActivity(new Intent(context, SearchActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_circle:
                        startActivity(new Intent(context, ShareActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_alert:
                        startActivity(new Intent(context, LikesActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_profile:
                        startActivity(new Intent(context, ProfileActivity.class));
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }catch (ClassCastException e){

        }
        super.onAttach(context);
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
