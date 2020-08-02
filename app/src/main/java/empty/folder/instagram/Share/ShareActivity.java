package empty.folder.instagram.Share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.Likes.LikesActivity;
import empty.folder.instagram.Profile.ProfileActivity;
import empty.folder.instagram.R;
import empty.folder.instagram.Search.SearchActivity;
import empty.folder.instagram.Utils.Permission;
import empty.folder.instagram.Utils.SectionsPagerAdapter;


public class ShareActivity extends AppCompatActivity {
    Context context = this;
    public static final int VERIFY_REQUEST_PERMISSIONS = 1;


    // widgets
    private ViewPager viewPager;

    public ShareActivity() {
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
//        setupBottomNavigationViewEx();

        // check and verify permissions with block
        if (checkPermissionsArray(Permission.PERMISSIONS)){
                setUpViewPager();
        }else{
            verifyPermission(Permission.PERMISSIONS);
        }

    }

    /**
     * check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions){
            for (int i = 0;i<permissions.length;i++){
                String check = permissions[i];
                if (!checkPermissions(check)){
                    return false;
                }
            }
        return true;
    }

    /**
     * check a single permission
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission){

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){
            return false;
        }else{
            return true;
        }
    }

    /**
     * verify all permissions
     * @param permissions
     */
    public void verifyPermission(String[] permissions){
        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_REQUEST_PERMISSIONS
        );
    }

    /**
     * return the current tab number of item
     * 0 ---> GALLERY
     * 1 ---> PHOTO
     * @return
     */
    public int getCurrentTabNumber(){
        return viewPager.getCurrentItem();
    }

    /**
     * setup viewpager for the manager tabs
     */
    private void setUpViewPager(){
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.addFragment(new GalleryFragment());
        sectionsPagerAdapter.addFragment(new PhotoFragment());
        viewPager = findViewById(R.id.viewpager_container);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));

    }

    public int getTask(){
        return getIntent().getFlags();
    }

    private void setupBottomNavigationViewEx() {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewEx);
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
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_search:
                        startActivity(new Intent(context, SearchActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_circle:
                        startActivity(new Intent(context, ShareActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_alert:
                        startActivity(new Intent(context, LikesActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_profile:
                        startActivity(new Intent(context, ProfileActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }

}
