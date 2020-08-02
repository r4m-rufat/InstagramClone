package empty.folder.instagram.Utils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.VectorEnabledTintResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.Models.Comment;
import empty.folder.instagram.Models.Like;
import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.R;

public class ViewCommentsFragment extends Fragment {

    public ViewCommentsFragment(){
        super();
        setArguments(new Bundle());
    }

    // widgets
    private ImageView ic_back, ic_sendComment;
    private EditText mComment;

    // variables
    private Photo mPhoto;
    private ArrayList<Comment> comments;
    private ListView listView;
    private Context context;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        ic_back = view.findViewById(R.id.ic_back_forComments);
        ic_sendComment = view.findViewById(R.id.ic_sendComment);
        mComment = view.findViewById(R.id.etextComment);
        listView = view.findViewById(R.id.listViewForComments);
        comments = new ArrayList<Comment>();
        context = getActivity();

        try {
            mPhoto = takePhotoFromBundle();

        }catch (NullPointerException e){

        }

        setupFireBase();


        return view;
    }

    private void setUpWidgets(){

        CommentListAdapter adapter = new CommentListAdapter(context, R.layout.layout_comment, comments);
        listView.setAdapter(adapter);

        ic_sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mComment.getText().toString().equals("")){
                    addNewComment(mComment.getText().toString());
                    mComment.setText("");
                    closeKeyboard();
                }else{
                    Toast.makeText(getActivity(), "you can't post empty comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getCallingActivityFromBundle().equals(getString(R.string.home_activity))){
                    getActivity().getSupportFragmentManager().popBackStack();
                    ((HomeActivity)getActivity()).showLayout();
                }else{
                    getActivity().getSupportFragmentManager().popBackStack();
                }



            }
        });
    }

    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null){
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment){
        String commentID = databaseReference.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate(getTime());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // insert into photos node
        databaseReference.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        // insert into user_photos node
        databaseReference.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

    }

    private String getTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        return simpleDateFormat.format(new Date());
    }

    /**
     * take the activity from Home Activity
     * @return
     */
    private String getCallingActivityFromBundle(){
        Bundle bundle = getArguments();
        if (bundle != null){
            return bundle.getString(getString(R.string.home_activity));
        }else{
            return null;
        }
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

    private void setupFireBase(){

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        if (mPhoto.getComments().size() == 0){
            comments.clear();
            Comment firsComment = new Comment();
            firsComment.setComment(mPhoto.getCaption());
            firsComment.setUser_id(mPhoto.getUser_id());
            firsComment.setDate(mPhoto.getDate());

            comments.add(firsComment);
            mPhoto.setComments(comments);
            setUpWidgets();
        }

        databaseReference.child(context.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(context.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Query query = databaseReference
                                .child(context.getString(R.string.dbname_photos))
                                .orderByChild(context.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                                    photo.setCaption(objectMap.get(context.getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(context.getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(context.getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(context.getString(R.string.field_user_id)).toString());
                                    photo.setDate(objectMap.get(context.getString(R.string.field_date)).toString());
                                    photo.setImage_path(objectMap.get(context.getString(R.string.field_image_path)).toString());

                                    comments.clear();
                                    Comment firsComment = new Comment();
                                    firsComment.setComment(mPhoto.getCaption());
                                    firsComment.setUser_id(mPhoto.getUser_id());
                                    firsComment.setDate(mPhoto.getDate());

                                    comments.add(firsComment);
                                    for (DataSnapshot singleSnapshot: dataSnapshot
                                            .child(context.getString(R.string.field_comments)).getChildren()){
                                        Comment comment = new Comment();
                                        comment.setUser_id(singleSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(singleSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate(singleSnapshot.getValue(Comment.class).getDate());
                                        comments.add(comment);
                                    }
                                    photo.setComments(comments);

                                    mPhoto = photo;

                                    setUpWidgets();
//                    List<Like> likes = new ArrayList<Like>();
//                    for (DataSnapshot singleSnapshot: dataSnapshot
//                            .child(getString(R.string.field_likes)).getChildren()){
//                        Like like = new Like();
//                        like.setUser_id(singleSnapshot.getValue(Like.class).getUser_id());
//                        likes.add(like);
//                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
