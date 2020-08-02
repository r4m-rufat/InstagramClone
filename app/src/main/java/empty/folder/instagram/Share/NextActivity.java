package empty.folder.instagram.Share;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.Profile.Activity_AccountSettings;
import empty.folder.instagram.R;
import empty.folder.instagram.Utils.FilePaths;
import empty.folder.instagram.Utils.ImageManager;
import empty.folder.instagram.Utils.StringManipulation;
import empty.folder.instagram.Utils.UniversalImageLoader;

public class NextActivity extends AppCompatActivity {

    // Variables
    private String append = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    private double mPhotoUploadProgress = 0;
    private static final String TAG = "NextActivity";
    Intent intent;
    private Bitmap bitmap;

    // Firebase
    private Uri filePath;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference mStorageReference;
    String userID;

    // Widgets
    private ImageView backImage, shareImage;
    private TextView txtShare;
    private EditText mCaption;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        context = NextActivity.this;
        shareImage = findViewById(R.id.shareImageView);
        mCaption = findViewById(R.id.descriptionShare);
        mStorageReference = FirebaseStorage.getInstance().getReference();

        setupFireBase();

        backImage = findViewById(R.id.ic_share_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtShare = findViewById(R.id.txtShare);
        txtShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (intent.hasExtra(getString(R.string._selected_image_))) {

                    imgUrl = intent.getStringExtra(context.getString(R.string._selected_image_));
                    // upload the image to firebase
                    String caption = mCaption.getText().toString();
                    uploadNewImage(context.getString(R.string.new_photo), caption, imgUrl, imageCount, null);
                } else if (intent.hasExtra(getString(R.string._selected_bitmap_))) {

                    bitmap = intent.getParcelableExtra(getString(R.string._selected_bitmap_));
                    // upload the image to firebase
                    String caption = mCaption.getText().toString();
                    uploadNewImage(context.getString(R.string.new_photo), caption, null, imageCount, bitmap);

                }
            }
        });
        setImage();
    }

    public void uploadNewImage(String photoType, final String caption, final String imgUrl, int count, Bitmap bitmap) {
        FilePaths filePaths = new FilePaths();

        // condition 1: new photo
        if (photoType.equals(getString(R.string.new_photo))) {
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference.
                    child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));


            // convert image url to bitmap
            if (bitmap == null) {
                bitmap = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);

            final UploadTask uploadTask;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: upload task is success");

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();
                            addPhotoToDatabase(caption, downloadUrl);
                        }
                    });
                    Toast.makeText(NextActivity.this, "uploaded", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(context, HomeActivity.class);
                    startActivity(intent);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NextActivity.this, "Photo upload failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: upload task is failed");

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if (progress - 5 > mPhotoUploadProgress) {
                        Toast.makeText(NextActivity.this, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_LONG).show();
                        mPhotoUploadProgress = progress;
                    }
                }
            });

        }
        // condition 2: profile_photo
//        else if (photoType.equals(context.getString(R.string.new_profile_photo))) {
//
//            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            final StorageReference storageReference = mStorageReference.
//                    child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profilePhoto");
//
//
//            // convert image url to bitmap
//            if (bitmap == null) {
//                bitmap = ImageManager.getBitmap(imgUrl);
//            }
//            byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);
//
//            final UploadTask uploadTask;
//            uploadTask = storageReference.putBytes(bytes);
//
//            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Log.d(TAG, "onSuccess: upload task is success");
//
//                    String firebaseUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
//                    // insert into 'user_account_settings' node
//                    addProfilePhotoInformationToDatabase(firebaseUrl);
//
//                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            final String firebase = uri.toString();
//                            addProfilePhotoInformationToDatabase(firebase);
//                        }
//                    });
//
//                    Toast.makeText(NextActivity.this, "uploaded", Toast.LENGTH_SHORT).show();
////                    ((Activity_AccountSettings)context).
////                            setViewPager(((Activity_AccountSettings)context).sectionsStatePagerAdapter.getFragmentNumber(context.getString(R.string.profile_fragment)));
//
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(NextActivity.this, "Photo upload failed", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "onFailure: upload task is failed");
//
//                }
//            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                    if (progress - 5 > mPhotoUploadProgress) {
//                        Toast.makeText(NextActivity.this, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_LONG).show();
//                        mPhotoUploadProgress = progress;
//                    }
//                }
//            });
//
//        }


    }

    private void addProfilePhotoInformationToDatabase(String imageUrl) {

        databaseReference.child(context.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(context.getString(R.string.new_profile_photo))
                .setValue(imageUrl);

    }

    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        return dateFormat.format(new Date());
    }

    private void addPhotoToDatabase(String caption, String fireBaseUrl) {

        Log.d(TAG, "addPhotoToDatabase: adding photo to database");

        String tags = StringManipulation.getTags(caption);

        String photoKey = databaseReference.child(context.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setImage_path(fireBaseUrl);
        photo.setDate(getDate());
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(photoKey);

        // insert into database
        databaseReference.child(context.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(photoKey).setValue(photo);
        databaseReference.child(context.getString(R.string.dbname_photos)).child(photoKey).setValue(photo);

    }


    /**
     * gets the image url from incoming intent displays the choosen image
     */
    private void setImage() {
        intent = getIntent();

        if (intent.hasExtra(getString(R.string._selected_image_))) {

            imgUrl = intent.getStringExtra(context.getString(R.string._selected_image_));
            UniversalImageLoader.setImage(imgUrl, shareImage, null, append);

        } else if (intent.hasExtra(getString(R.string._selected_bitmap_))) {

            bitmap = intent.getParcelableExtra(getString(R.string._selected_bitmap_));
            shareImage.setImageBitmap(bitmap);

        }

    }


    private void setupFireBase () {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageCount = getImageCount(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private int getImageCount (DataSnapshot dataSnapshot){
        int count = 0;
        for (DataSnapshot ds : dataSnapshot
                .child(context.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()) {

            count++;

        }
        return count;
    }

    @Override
    public void onStart () {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop () {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
