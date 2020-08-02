package empty.folder.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.R;
import empty.folder.instagram.Share.NextActivity;
import empty.folder.instagram.Utils.FilePaths;
import empty.folder.instagram.Utils.FirebaseMethods;
import empty.folder.instagram.Utils.ImageManager;
import empty.folder.instagram.Utils.SectionsPagerAdapter;
import empty.folder.instagram.Utils.SectionsStatePagerAdapter;

import java.util.ArrayList;

public class Activity_AccountSettings extends AppCompatActivity {

    public SectionsStatePagerAdapter sectionsStatePagerAdapter;
    private ViewPager viewPager;
    private RelativeLayout relativeLayout;

    Context context;

    public Activity_AccountSettings() {
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        viewPager = findViewById(R.id.viewpager_container);
        relativeLayout = findViewById(R.id.rlayout1);
        context = this;

        setupSettingsList();
        setupFragment();
        getIncomingIntentForEditProfile();

        ImageView imageView = findViewById(R.id.back_profile_menu);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupSettingsList() {
        ListView listView = findViewById(R.id.txtSettingOptionsList);
        ArrayList<String> options = new ArrayList();
        options.add(getString(R.string._edit_profile_));
        options.add(getString(R.string._sign_out_));
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewPager(position);
            }
        });
    }

    private void getIncomingIntentForEditProfile() {

        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string._selected_image_)) ||
                intent.hasExtra(getString(R.string._selected_bitmap_))){
            // if there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string._edit_profile_))) {
                if (intent.hasExtra(getString(R.string._selected_image_))) {
                    // set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(context);
                    firebaseMethods.uploadNewImage(getString(R.string.new_profile_photo), null, 0, intent.getStringExtra(getString(R.string._selected_image_)), null);

                }else if (intent.hasExtra(getString(R.string._selected_bitmap_))){
                    FirebaseMethods firebaseMethods = new FirebaseMethods(context);
                    firebaseMethods.uploadNewImage(getString(R.string.new_profile_photo), null, 0,
                            null, (Bitmap) intent.getParcelableExtra(getString(R.string._selected_bitmap_)));
                }

            }
        }

        if (intent.hasExtra(getString(R.string.calling_activity))) {
            setViewPager(sectionsStatePagerAdapter.getFragmentNumber(getString(R.string._edit_profile_)));
        }

    }
    private void setupFragment() {
        sectionsStatePagerAdapter = new SectionsStatePagerAdapter(this.getSupportFragmentManager());
        sectionsStatePagerAdapter.addSectionsFragment(new EditProfileFragment(), getString(R.string._edit_profile_));
        sectionsStatePagerAdapter.addSectionsFragment(new SignOutFragment(), getString(R.string._sign_out_));
    }

    public void setViewPager(int fragmentNumber) {
        this.relativeLayout.setVisibility(View.GONE);
        this.viewPager.setAdapter(this.sectionsStatePagerAdapter);
        this.viewPager.setCurrentItem(fragmentNumber);
    }
}
