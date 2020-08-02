package empty.folder.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.provider.ContactsContract;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.Models.Comment;
import empty.folder.instagram.Models.Like;
import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.Models.User;
import empty.folder.instagram.Models.UserAccountSettings;
import empty.folder.instagram.Profile.ProfileActivity;
import empty.folder.instagram.R;

public class HomeItemsListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener{
        void onLoadMoreItemsListener();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;


    private LayoutInflater layoutInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference databaseReference;
    private String currentUsername = "";


    public HomeItemsListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        mContext = context;
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder{

        CircleImageView profileImage;
        String likesString;
        TextView username, date, caption, likes, comments;
        Square_Image_View squareImage;
        ImageView redHeart, whiteHeart, comment;

        UserAccountSettings userAccountSettings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        boolean photoLikedByCurrentUser;
        Heart heart;
        GestureDetector gestureDetector;
        Photo photo;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;

        if (convertView == null){
            convertView = layoutInflater.inflate(mLayoutResource, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.profileImage = convertView.findViewById(R.id.profile_photo_forHome);
            viewHolder.username = convertView.findViewById(R.id.username_forHome);
            viewHolder.caption = convertView.findViewById(R.id.image_captions);
            viewHolder.comment = convertView.findViewById(R.id.icon_comment_for_postView);
            viewHolder.squareImage = convertView.findViewById(R.id.center_postViewImage);
            viewHolder.date = convertView.findViewById(R.id.image_dateTime);
            viewHolder.likes = convertView.findViewById(R.id.image_likes);
            viewHolder.comments = convertView.findViewById(R.id.image_comments_count);
            viewHolder.heart = new Heart(viewHolder.redHeart, viewHolder.whiteHeart);
            viewHolder.photo = getItem(position);
            viewHolder.gestureDetector = new GestureDetector(mContext, new GestureListener(viewHolder));
            viewHolder.redHeart = convertView.findViewById(R.id.icon_heart_fill_red);
            viewHolder.whiteHeart = convertView.findViewById(R.id.icon_heart_outline);
            viewHolder.users = new StringBuilder();

            convertView.setTag(viewHolder);


        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // get the current users name
        getCurrentUsername();

        /**
         * it will be in update
         */
        //getLikeString(viewHolder);

        // set the caption
        viewHolder.caption.setText(getItem(position).getCaption());

        // set the comments
        List<Comment> comments = getItem(position).getComments();
        viewHolder.comments.setText("View all " + comments.size() + " comments");

        viewHolder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)mContext).onCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));

                ((HomeActivity)mContext).hideLayout();

            }
        });

        // set the time it was posted
        String time = getDateTimeDifference(getItem(position));
        if (!time.equals("0")){
            viewHolder.date.setText(time + " DAYS AGO");
        }else{
            viewHolder.date.setText("TODAY");
        }

        // set the profile image
        final ImageLoader imageLoader= ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), viewHolder.squareImage);

        // get the profile image and username
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot dataSnapshot: snapshot.getChildren()){

                    viewHolder.username.setText(dataSnapshot.getValue(UserAccountSettings.class).getUsername());
                    /*
                    viewHolder.username.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), viewHolder.user);
                            mContext.startActivity(intent);

                        }
                    });

                     */

                    imageLoader.displayImage(dataSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            viewHolder.profileImage);
                    /*

                    viewHolder.profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.intent_user), viewHolder.user);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            mContext.startActivity(intent);
                        }
                    });

                      */

                    viewHolder.userAccountSettings = dataSnapshot.getValue(UserAccountSettings.class);
                    viewHolder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity)mContext).onCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));

                            ((HomeActivity)mContext).hideLayout();

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // get the user object
        Query userQuery = databaseReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot dataSnapshot: snapshot.getChildren()){

                    viewHolder.user = dataSnapshot.getValue(User.class);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (reachedEndOfList(position)){
            loadMoreData();
        }

        return convertView;

    }

    private boolean reachedEndOfList(int position){
        return position == (getCount() - 1) ;
    }

    private void loadMoreData(){

        try{
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        }catch (ClassCastException e){

        }

        try{
            mOnLoadMoreItemsListener.onLoadMoreItemsListener();
        }catch (NullPointerException e){

        }
    }
    private void getCurrentUsername(){

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot dataSnapshot: snapshot.getChildren()){

                    currentUsername = dataSnapshot.getValue(UserAccountSettings.class).getUsername();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        public ViewHolder mHolder;
        public GestureListener(ViewHolder viewHolder){
            mHolder = viewHolder;
        }
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            Query query = databaseReference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                        String keyID = dataSnapshot.getKey();

                        // step 1) the user already liked the photo
                        if (mHolder.photoLikedByCurrentUser && dataSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            databaseReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();


                            databaseReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            // getLikeString(mHolder);
                        }

                        // step 2) the user already don't like photo
                        else if (!mHolder.photoLikedByCurrentUser){
                            // add new like
                            addNewLike(mHolder);
//                            Toast.makeText(getActivity(), "for dont equals " + photoLikedByUser, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (!snapshot.exists()){
                        // add new like
//                        Toast.makeText(getActivity(), "due to database", Toast.LENGTH_SHORT).show();
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            return true;
        }
    }

    private void addNewLike(final ViewHolder viewHolder){
        String newLikeId = databaseReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        databaseReference.child(mContext.getString(R.string.dbname_photos))
                .child(viewHolder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        databaseReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(viewHolder.photo.getUser_id())
                .child(viewHolder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        viewHolder.heart.toggleLike();
        // getLikeString(viewHolder);

    }

    private void getLikeString(final ViewHolder viewHolder){

        try{

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            Query query = databaseReference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(viewHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    viewHolder.users = new StringBuilder();
                    for (final DataSnapshot dataSnapshot: snapshot.getChildren()){
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(dataSnapshot.getValue(Like.class).getUser_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                                    viewHolder.users.append(dataSnapshot.getValue(User.class).getUsername());
                                    viewHolder.users.append(",");
                                }

                                String[] splitUsers = viewHolder.users.toString().split(",");
                                if (viewHolder.users.toString().contains(currentUsername + ",")){
                                    viewHolder.photoLikedByCurrentUser = true;
                                }else{
                                    viewHolder.photoLikedByCurrentUser = false;
                                }

                                int userscount = splitUsers.length;
                                if (userscount == 1){
                                    viewHolder.likesString = "Liked by " + splitUsers[0];
                                }else if (userscount == 2){
                                    viewHolder.likesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                                }else if (userscount == 3){
                                    viewHolder.likesString = "Liked by "
                                            + splitUsers[0] + ", "
                                            + splitUsers[1] + " and "
                                            + splitUsers[2];
                                }else if (userscount == 4) {
                                    viewHolder.likesString = "Liked by "
                                            + splitUsers[0] + ", "
                                            + splitUsers[1] + ", "
                                            + splitUsers[2] + " and "
                                            + splitUsers[3];
                                }else if (userscount > 4){
                                    viewHolder.likesString = "Liked by "
                                            + splitUsers[0] + ", "
                                            + splitUsers[1] + ", "
                                            + splitUsers[2] + " and "
                                            + (splitUsers.length - 3) + " others";
                                }
                                setupLikesString(viewHolder, viewHolder.likesString);
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    if (!snapshot.exists()){
                        viewHolder.likesString = "";
                        viewHolder.photoLikedByCurrentUser = false;
                        // setup likes string
                        setupLikesString(viewHolder, viewHolder.likesString);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }catch (NullPointerException e){
            viewHolder.likesString = "";
            viewHolder.photoLikedByCurrentUser = false;
            // setup likes string
            setupLikesString(viewHolder, viewHolder.likesString);
        }

    }

    private void setupLikesString(final ViewHolder viewHolder, String likesString){

        if (viewHolder.photoLikedByCurrentUser){
            viewHolder.whiteHeart.setVisibility(View.GONE);
            viewHolder.redHeart.setVisibility(View.VISIBLE);
            viewHolder.redHeart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return viewHolder.gestureDetector.onTouchEvent(event);
                }
            });
        }else{

            viewHolder.whiteHeart.setVisibility(View.VISIBLE);
            viewHolder.redHeart.setVisibility(View.GONE);
            viewHolder.whiteHeart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return viewHolder.gestureDetector.onTouchEvent(event);
                }
            });

        }

        viewHolder.likes.setText(likesString);

    }

    /**
     * returns a string representing the number of days ago the post was shared
     * @return
     */
    private String getDateTimeDifference(Photo photo){
        String difference = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        Date today = calendar.getTime();
        simpleDateFormat.format(today);
        Date time;
        final String photoTime = photo.getDate();
        try {
            time = simpleDateFormat.parse(photoTime);
            difference = String.valueOf(Math.round((today.getTime() - time.getTime()) / 100 / 60 / 60 / 24));
        }catch (ParseException e){
            difference = "0";
        }

        return difference;
    }


}
