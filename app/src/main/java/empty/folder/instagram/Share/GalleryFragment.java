package empty.folder.instagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.ArrayList;

import empty.folder.instagram.Profile.Activity_AccountSettings;
import empty.folder.instagram.R;
import empty.folder.instagram.Utils.FilePaths;
import empty.folder.instagram.Utils.FileSearch;
import empty.folder.instagram.Utils.GridViewPagerAdapter;

public class GalleryFragment extends Fragment {

    private static final int NUM_GRID_COLUMNS = 3;
    private GridView gridView;
    private ImageView imageView,closeIcon;
    private ProgressBar progressBar;
    private Spinner spinner;
    private TextView txtNext;

    private String append = "file:/";

    private ArrayList<String> directories;
    private String selectedImage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        gridView = view.findViewById(R.id.galleryGridView);
        imageView = view.findViewById(R.id.galleryImage);
        progressBar = view.findViewById(R.id.fragmentGalleryProgressbar);
        spinner = view.findViewById(R.id.gallerySpinner);
        closeIcon = view.findViewById(R.id.ic_close);
        txtNext = view.findViewById(R.id.textNext);
        progressBar.setVisibility(View.GONE);


        directories = new ArrayList<>();

        init();

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        txtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forIntentTask()){
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string._selected_image_), selectedImage);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getActivity(), Activity_AccountSettings.class);
                    intent.putExtra(getString(R.string._selected_image_), selectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string._edit_profile_));
                    startActivity(intent);
                    getActivity().finish();
                }
            }

        });

        return view;
    }

    private boolean forIntentTask(){
        if (((ShareActivity)getActivity()).getTask() == 0){
            return true;
        }else{
            return false;
        }
    }

    private void init(){

        FilePaths filePaths = new FilePaths();
        if (FileSearch.getDirectoryPath(filePaths.PICTURES_DIR) != null){
            directories = FileSearch.getDirectoryPath(filePaths.PICTURES_DIR);
        }

        final ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++){
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index).replace("/", "");
            directoryNames.add(string);
        }

//        directoryNames.add(filePaths.CAMERA_DIR);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setUpGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void setUpGridView(String selectedDirectory){

        final ArrayList<String> imgURLs = FileSearch.getFilePath(selectedDirectory);
        int gridWith = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWith/ NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridViewPagerAdapter adapter = new GridViewPagerAdapter(getActivity(), R.layout.layout_grid_imageview, append, imgURLs);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "clicked", Toast.LENGTH_SHORT).show();
                setImage(imgURLs.get(position), imageView, append);
                selectedImage = imgURLs.get(position);
            }
        });

        try {
            setImage(imgURLs.get(0), imageView, append);
            selectedImage = imgURLs.get(0);
        }catch (ArrayIndexOutOfBoundsException e){

        }


    }

    private void setImage(String imgUrl, ImageView image, String append){

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgUrl, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
