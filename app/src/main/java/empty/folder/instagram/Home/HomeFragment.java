package empty.folder.instagram.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import empty.folder.instagram.Models.Comment;
import empty.folder.instagram.Models.Like;
import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.Models.UserAccountSettings;
import empty.folder.instagram.R;
import empty.folder.instagram.Utils.HomeItemsListAdapter;

public class HomeFragment extends Fragment {

    // vars
    private ArrayList<Photo> photos;
    private ArrayList<Photo> paginatedPhotos;
    private ArrayList<String> Following;
    private ListView listView;
    private HomeItemsListAdapter listAdapter;
    private int results;


    public HomeFragment() {
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        listView = view.findViewById(R.id.listView_forHome);
        photos = new ArrayList<>();
        Following = new ArrayList<>();

        getFollowing();

        return view;
    }

    private void getFollowing(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        try {
            Query query = databaseReference
                    .child(getString(R.string.dbname_following))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (final DataSnapshot dataSnapshot: snapshot.getChildren()){

                        Following.add(dataSnapshot.child(getString(R.string.field_user_id)).getValue().toString());

                    }
                    Following.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    // get the photos
                    getPhotos();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (NullPointerException e){

        }

    }

    private void getPhotos(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        for (int i = 0; i < Following.size(); i++){
            final int count  = i;
            Query query = databaseReference
                    .child(getString(R.string.dbname_user_photos))
                    .child(Following.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(Following.get(i));;

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (final DataSnapshot dataSnapshot: snapshot.getChildren()){

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


                        photos.add(photo);
                    }

                    if (count >= Following.size() - 1){
                        displayPhotos();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void displayPhotos(){
        paginatedPhotos = new ArrayList<>();
        if (photos != null){
            try {
                Collections.sort(photos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                });

                int iterations = photos.size();

                if (iterations > 10){
                    iterations = 10;
                }

                results = 10;

                for (int i = 0; i<iterations; i++){

                    paginatedPhotos.add(photos.get(i));

                }

                listAdapter = new HomeItemsListAdapter(getActivity(), R.layout.fragment_list_item_for_home, paginatedPhotos);
                listView.setAdapter(listAdapter);

            }catch (NullPointerException e){

            }catch (IndexOutOfBoundsException e){

            }
        }
    }

    public void displayMorePhotos(){

        try {

            if (photos.size() > results && photos.size() > 0){
                int iterations;
                if (photos.size() > results + 10){
                    iterations = 10;
                }else{
                    iterations = photos.size() - results;
                }

                // add new photos to the paginated results
                for (int i = results; i < results + iterations; i++){
                    paginatedPhotos.add(photos.get(i));
                }

                results = results + iterations;

                // add the new photos to the paginated results
                listAdapter.notifyDataSetChanged();
            }

        }catch (NullPointerException e){

        }catch (IndexOutOfBoundsException e){

        }
    }

}