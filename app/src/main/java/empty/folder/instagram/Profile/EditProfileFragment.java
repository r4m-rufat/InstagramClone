package empty.folder.instagram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;
import empty.folder.instagram.Dialogs.Confirm_Password_Dialogs;
import empty.folder.instagram.Models.User;
import empty.folder.instagram.Models.UserAccountSettings;
import empty.folder.instagram.Models.UserSettings;
import empty.folder.instagram.R;
import empty.folder.instagram.Share.ShareActivity;
import empty.folder.instagram.Utils.UniversalImageLoader;

public class EditProfileFragment extends Fragment implements Confirm_Password_Dialogs.OnConfirmPasswordListener{

    // Layout Widgets
    private EditText DisplayName, Username, Website, Description, Email, PhoneNumber;
    private TextView changePofileImage;
    private ImageView savedChanges;
    private static CircleImageView profileImage;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    UserSettings mUserSettings;
    String userid;


    public EditProfileFragment() {
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        DisplayName = view.findViewById(R.id.etxt_displayName);
        Username = view.findViewById(R.id.etxt_displayUsername);
        Website = view.findViewById(R.id.etxt_website);
        Description = view.findViewById(R.id.etxt_livearea);
        Email = view.findViewById(R.id.etxt_email);
        PhoneNumber = view.findViewById(R.id.etxt_Telephone);
        changePofileImage = view.findViewById(R.id.txt_change_photo);
        profileImage = view.findViewById(R.id.profile_circle_image);
        savedChanges = view.findViewById(R.id.ic_saved_changes);


        setupFireBase();
        userid = firebaseAuth.getCurrentUser().getUid();

        ImageView backArrow  = view.findViewById(R.id.back_arrow);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        savedChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileSettings();
            }
        });

        return view;
    }



    @Override
    public void onConfirmPassword(String Password) {

        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseAuth.getCurrentUser().getEmail(), Password);

        firebaseAuth.getCurrentUser().reauthenticate(authCredential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            firebaseAuth.fetchSignInMethodsForEmail(Email.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                                    try {
                                        if (task.getResult().getSignInMethods().size() == 1){
                                            Toast.makeText(getActivity(), "This email is already in use", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (task.getResult().getSignInMethods().size() == 0){

                                            firebaseAuth.getCurrentUser().updateEmail(Email.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            updateEmail(Email.getText().toString());
                                                            Toast.makeText(getActivity(), "Email is successfully updated", Toast.LENGTH_SHORT).show();

                                                        }
                                                    });

                                        }
                                    }catch (NullPointerException e){

                                    }

                                }
                            });

                        }

                    }
                });



    }

    /**
     * update email in 'users' node
     * @param email
     */
    private void updateEmail(String email){

        databaseReference.child(getContext().getString(R.string.dbname_users))
                .child(userid)
                .child(getContext().getString(R.string.field_email))
                .setValue(email);
    }



    // Enter Informations Which You Want To Change With New For Edit Profile

    private void saveProfileSettings(){

        final String displayName = DisplayName.getText().toString();
        final String username = Username.getText().toString();
        final String website = Website.getText().toString();
        final String description = Description.getText().toString();
        final String email = Email.getText().toString();
        final long phoneNumber = Long.parseLong(PhoneNumber.getText().toString());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {



                // The User Can't Change Your Username
                if (!mUserSettings.getUser().getUsername().equals(username)){
                    checkIfUsernameExists(username);
                }
                // If The User Made A Change To Their Email
                if (!mUserSettings.getUser().getEmail().equals(email)){

                    // case 1: Reauthenticate
                    //      ******* Confirm the password and email *******

                    Confirm_Password_Dialogs confirmPasswordDialogs = new Confirm_Password_Dialogs();
                    confirmPasswordDialogs.show(getFragmentManager(), getString(R.string._confirm_password_dialog_));
                    confirmPasswordDialogs.setTargetFragment(EditProfileFragment.this, 1);

                    //case 2: Check if the email already is registered
                    //      ******* fetchProvidersForEmail(String email) *******

                    // case 3: Change the email
                    //      ******* Submit the user's new email to the database and authentication *******

                }

                if (!mUserSettings.getUserAccountSettings().getDisplay_name().equals(displayName)){
                    // update displayName
                    updateUserAccountSettings(displayName, null, null, 0);
                }
                if (!mUserSettings.getUserAccountSettings().getDescription().equals(description)){
                    //update description
                    updateUserAccountSettings(null, description, null, 0);
                }
                if (!mUserSettings.getUserAccountSettings().getWebsite().equals(website)){
                    //update website
                    updateUserAccountSettings(null, null, website, 0);
                }
                if (mUserSettings.getUser().getPhone() != phoneNumber){
                    //update profileImage
                    updateUserAccountSettings(null, null, null, phoneNumber);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * check if username exists in the FireBase Database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getContext().getString(R.string.field_username))
                . equalTo(username);



        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    if (!snapshot.exists()){
                        // add channges for username
                        updateUsername(username);
                        Toast.makeText(getActivity(), "Username has added", Toast.LENGTH_LONG).show();

                    }
                }
                catch (NullPointerException e){

                }


                for (DataSnapshot singleSnapshot: snapshot.getChildren()){
                    if (singleSnapshot.exists()){
                        Toast.makeText(getActivity(), "Username already exists", Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUsername(String username){

        databaseReference.child(getContext().getString(R.string.dbname_user_account_settings))
                .child(userid)
                .child(getContext().getString(R.string.field_username))
                .setValue(username);

        databaseReference.child(getContext().getString(R.string.dbname_users))
                .child(userid)
                .child(getContext().getString(R.string.field_username))
                .setValue(username);

    }

    /**
     * update 'user_account_settings' node for the current user
     * @param displayName
     * @param description
     * @param webiste
     * @param phone_number
     */
    public void updateUserAccountSettings(String displayName, String description, String webiste, long phone_number ){

        if (displayName != null){
            databaseReference.child(getContext().getString(R.string.dbname_user_account_settings))
                    .child(userid)
                    .child(getContext().getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if (description != null){
            databaseReference.child(getContext().getString(R.string.dbname_user_account_settings))
                    .child(userid)
                    .child(getContext().getString(R.string.field_description))
                    .setValue(description);
        }

        if (webiste != null){
            databaseReference.child(getContext().getString(R.string.dbname_user_account_settings))
                    .child(userid)
                    .child(getContext().getString(R.string.field_website))
                    .setValue(webiste);
        }

        if (phone_number != 0){
            databaseReference.child(getContext().getString(R.string.dbname_users))
                    .child(userid)
                    .child(getContext().getString(R.string.field_phone))
                    .setValue(phone_number);
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

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // retrieve user informations from database

                setProfileWidgets(getUserSettings(snapshot));

                // retrieve user images from database


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * retrieve account settings for the user currently logged in
     * Database: user_account_settings node
     * @param dataSnapshot
     * @return
     */
    private UserSettings getUserSettings(DataSnapshot dataSnapshot){

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        userid = firebaseUser.getUid();

        UserAccountSettings userAccountSettings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()){

            // user_account_settings node

            if (ds.getKey().equals(getString(R.string.dbname_user_account_settings))){

                try {

                    userAccountSettings.setDisplay_name(ds.child(userid)
                            .getValue(UserAccountSettings.class)
                            .getDisplay_name());

                    userAccountSettings.setUsername(ds.child(userid)
                            .getValue(UserAccountSettings.class)
                            .getUsername());

                    userAccountSettings.setDescription(ds.child(userid)
                            .getValue(UserAccountSettings.class)
                            .getDescription());

                    userAccountSettings.setWebsite(ds.child(userid)
                            .getValue(UserAccountSettings.class)
                            .getWebsite());

                    userAccountSettings.setProfile_photo(ds.child(userid)
                            .getValue(UserAccountSettings.class)
                            .getProfile_photo());

                    userAccountSettings.setPosts(ds.child(userid)
                            .getValue(UserAccountSettings.class)
                            .getPosts());

                    userAccountSettings.setFollowers(ds.child(userid)
                            .getValue(UserAccountSettings.class)
                            .getFollowers());

                    userAccountSettings.setFollowing(ds.child(userid)
                            .getValue(UserAccountSettings.class)
                            .getFollowing());

                }catch (NullPointerException e){

                }
            }
            // users node

            if (ds.getKey().equals(getString(R.string.dbname_users))){

                user.setUsername(ds.child(userid)
                        .getValue(User.class)
                        .getUsername());

                user.setEmail(ds.child(userid)
                        .getValue(User.class)
                        .getEmail());

                user.setPhone(ds.child(userid)
                        .getValue(User.class)
                        .getPhone());

                user.setUserid(ds.child(userid)
                        .getValue(User.class)
                        .getUserid());

            }


        }

        return new UserSettings(userAccountSettings, user);

    }

    private void setProfileWidgets(UserSettings userSettings){


        UserAccountSettings userAccountSettings = userSettings.getUserAccountSettings();

        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), profileImage, null, "");
        mUserSettings = userSettings;

        DisplayName.setText(userAccountSettings.getDisplay_name());
        Username.setText(userAccountSettings.getUsername());
        Website.setText(userAccountSettings.getWebsite());
        Description.setText(userAccountSettings.getDescription());
        Email.setText(userSettings.getUser().getEmail());
        PhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone()));

        changePofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //7238726
                getActivity().startActivity(intent);
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
