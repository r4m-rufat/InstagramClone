package empty.folder.instagram.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import empty.folder.instagram.Models.User;
import empty.folder.instagram.Models.UserAccountSettings;
import empty.folder.instagram.R;
import empty.folder.instagram.Utils.StringManipulation;

public class RegisterActivity extends AppCompatActivity {

    EditText Username, Email, Password;
    String username, email, password;
    Button buttonRegister;
    ProgressBar progressBar;
    Context context;
    private String userID;
    private String TAG = "RegisterActivity";
    private boolean forCheckIfUsername;

    // Firebase
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViewById();
        context = this;

        progressBar.setVisibility(View.GONE);

        clickedButton();
        setupFireBase();


    }

    private void findViewById(){
        Username = findViewById(R.id.registerName);
        Email = findViewById(R.id.registerEmail);
        Password = findViewById(R.id.registerPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.register_ProgressBar);
    }

    private void clickedButton(){

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = Username.getText().toString().trim();
                email = Email.getText().toString().trim();
                password = Password.getText().toString().trim();

                if (!TextUtils.isEmpty(username) && (!TextUtils.isEmpty(password) && (password.length()>=6)) && !TextUtils.isEmpty(email)){
                    progressBar.setVisibility(View.VISIBLE);

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        sendEmailVerificatioin();
                                        startActivity(new Intent(context, LoginActivity.class));
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    else{
                                        Toast.makeText(context, "Register is failed", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                }else{
                    if (TextUtils.isEmpty(username)){
                        Username.setError("Please filled the Username Gap");
                    }
                    if (TextUtils.isEmpty(email)){
                        Email.setError("Please filled the Email Gap");
                    }
                    if (password.length() < 6){
                        Password.setError("Password must be 6 and more character");
                    }
                }

            }
        });

    }

    /** -------------------------------------- FireBase Methods ------------------------------------------ **/


    /**
     * check if username exists in the FireBase Database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {

        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Query query = databaseReference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if (dataSnapshot.exists()){
                        forCheckIfUsername = true;
                    }
                }

                /**
                 * check if username is existed or no
                 */

                if (forCheckIfUsername){
                    Toast.makeText(RegisterActivity.this, "this username already exists.You don't use your profile in this statement!", Toast.LENGTH_LONG).show();
                }else{
                    addNewUser(email, username, "", "", "");
                }

                firebaseAuth.signOut();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupFireBase(){
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            checkIfUsernameExists(username);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }else{

                }


            }
        };

        firebaseAuth.signOut();

    }

//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
//        User user = new User();
//
//        for (DataSnapshot ds: dataSnapshot.child(userID).getChildren()){
//            user.setUsername(ds.getValue(User.class).getUsername());
//
//            if (StringManipulation.expandUsername(user.getUsername()).equals(username)){
//                return true;
//            }
//        }
//        return false;
//    }

    private void sendEmailVerificatioin(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){
            firebaseUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(context, "is sent the email verification your email", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(context, "couldn't send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * is added user informations to the user's private node in node
     * is added user_account_settings informations to the user's private node in node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    private void addNewUser(String email, String username, String description, String website, String profile_photo){

        userID = firebaseAuth.getCurrentUser().getUid();

        User user = new User(email, 0, userID, username);

        databaseReference.child(context.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings userAccountSettings = new UserAccountSettings(
                description,
                "",
                0,
                0,
                0,
                profile_photo,
                username,
                website,
                userID
        );

        databaseReference.child(context.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(userAccountSettings);

    }



    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

}
