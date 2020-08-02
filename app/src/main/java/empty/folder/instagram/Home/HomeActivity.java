package empty.folder.instagram.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import empty.folder.instagram.Likes.LikesActivity;
import empty.folder.instagram.Login.LoginActivity;
import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.Models.UserAccountSettings;
import empty.folder.instagram.Profile.ProfileActivity;
import empty.folder.instagram.R;
import empty.folder.instagram.Search.SearchActivity;
import empty.folder.instagram.Share.ShareActivity;
import empty.folder.instagram.Utils.HomeItemsListAdapter;
import empty.folder.instagram.Utils.SectionsPagerAdapter;
import empty.folder.instagram.Utils.UniversalImageLoader;
import empty.folder.instagram.Utils.ViewCommentsFragment;

public class HomeActivity extends AppCompatActivity implements HomeItemsListAdapter.OnLoadMoreItemsListener {

    @Override
    public void onLoadMoreItemsListener() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + viewPager.getCurrentItem());
        if (homeFragment != null){
            homeFragment.displayMorePhotos();
        }
    }

    Context context = this;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    // widgets
    private RelativeLayout relativeLayout;
    private FrameLayout frameLayout;
    private ViewPager viewPager;

    // variables
    private static final int HOME_FRAGMENT = 1;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.rlayoutParent);
        frameLayout = findViewById(R.id.container);
        viewPager = findViewById(R.id.viewpager_container);

        initImageLoader();

        setupBottomNavigationViewEx();

        setupViewPager();

        setupFireBase();
    }

    public void onCommentThreadSelected(Photo photo, String callingActivity){

        ViewCommentsFragment viewCommentsFragment = new ViewCommentsFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(getString(R.string.photo), photo);
        arguments.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        viewCommentsFragment.setArguments(arguments);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, viewCommentsFragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }

    public void hideLayout(){
        relativeLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout(){
        relativeLayout.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (frameLayout.getVisibility() == View.VISIBLE){
            showLayout();
        }
    }

    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(this.getSupportFragmentManager());
        adapter.addFragment(new CameraFragment());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new MessagesFragment());
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setText("Instagram");
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_message);
    }

    /**
     * This method is for adjusting the Animation of Bottom Naviagtion View
     */

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

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(context);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setupFireBase(){

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                // check it in here
                checkCurrentUser(user);



            }
        };
    }

    /**
     * check the user who is logged
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user){
        if (user == null){
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        viewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(firebaseAuth.getCurrentUser());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

}
