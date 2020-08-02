package empty.folder.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

import empty.folder.instagram.Models.Photo;
import empty.folder.instagram.Models.User;
import empty.folder.instagram.R;
import empty.folder.instagram.Utils.ViewCommentsFragment;
import empty.folder.instagram.Utils.ViewPostFragment;
import empty.folder.instagram.Utils.ViewProfileFragment;


public class ProfileActivity extends AppCompatActivity implements
ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener{

    @Override
    public void onCommentSelectedListener(Photo photo) {

        ViewCommentsFragment viewCommentsFragment = new ViewCommentsFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(getString(R.string.photo), photo);
        viewCommentsFragment.setArguments(arguments);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, viewCommentsFragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        ViewPostFragment viewPostFragment = new ViewPostFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.photo), photo);
        bundle.putInt(getString(R.string.activity_number), activityNumber);

        viewPostFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, viewPostFragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    public ProfileActivity() {
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();

    }

    private void init(){

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity))){
            if (intent.hasExtra(getString(R.string.intent_user))){
                User user = intent.getParcelableExtra(getString(R.string.intent_user));

                if (!user.getUserid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(getString(R.string.intent_user), intent.getParcelableExtra(getString(R.string.intent_user)));
                    viewProfileFragment.setArguments(arguments);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, viewProfileFragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                }else{
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction fragmentTransaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container, profileFragment);
                    fragmentTransaction.addToBackStack(getString(R.string.profile_fragment));
                    fragmentTransaction.commit();
                }
            }else{
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        }else{
            ProfileFragment profileFragment = new ProfileFragment();
            FragmentTransaction fragmentTransaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, profileFragment);
            fragmentTransaction.addToBackStack(getString(R.string.profile_fragment));
            fragmentTransaction.commit();
        }
    }


}
