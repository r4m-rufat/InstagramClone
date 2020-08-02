package empty.folder.instagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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

import java.util.ArrayList;

import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.Profile.Activity_AccountSettings;
import empty.folder.instagram.R;
import empty.folder.instagram.Utils.FirebaseMethods;
import empty.folder.instagram.Utils.Permission;

public class PhotoFragment extends Fragment {

    // constants
    private static final int GALLERY_FRAGMENT_NUMBER = 0;
    private static final int PHOTO_FRAGMENT_NUMBER = 1;
    private static final int CAMERA_REQUEST_CODE = 4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        Button openCamera = view.findViewById(R.id.openCamera);

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUMBER){
                    if (((ShareActivity)getActivity()).checkPermissions(Permission.CAMERA_PERMISSION[0])){
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }else{
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }

            }
        });

        return view;
    }


    private boolean forIntentTask(){
        if(((ShareActivity)getActivity()).getTask() == 0){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE){
            // navigate to the final share screen

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");

            if (forIntentTask()){

                Toast.makeText(getActivity(), "you are not in forIntentTask", Toast.LENGTH_SHORT).show();

                try {
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string._selected_bitmap_), bitmap);
                    startActivity(intent);
                }catch (NullPointerException e){

                }

            }else{

                try {
                    Intent intent = new Intent(getActivity(), Activity_AccountSettings.class);
                    intent.putExtra(getString(R.string._selected_bitmap_), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string._edit_profile_));
                    startActivity(intent);
                    getActivity().finish();

                }catch (NullPointerException e){

                }

            }
        }
    }
}
