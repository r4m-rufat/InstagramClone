package empty.folder.instagram.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.R;

public class LoginActivity extends AppCompatActivity {

    EditText Email,Password;
    TextView signUp;
    ProgressBar progressBar;
    Button buttonLogin;
    Context context;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById();

        progressBar.setVisibility(View.GONE);

        setupFireBase();
        clickedButton();

    }

    public void findViewById(){
        Email = findViewById(R.id.loginName);
        Password = findViewById(R.id.loginPassword);
        progressBar = findViewById(R.id.login_ProgressBar);
        buttonLogin = findViewById(R.id.buttonLogin);
        signUp = findViewById(R.id.txtSignUp);
        context = LoginActivity.this;
    }

    private void clickedButton(){

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getEmail = Email.getText().toString().trim();
                String getPassword = Password.getText().toString().trim();

                if (TextUtils.isEmpty(getEmail)){
                    Email.setError("Filled Correctly Email Gap!!!");
                }
                if (TextUtils.isEmpty(getPassword)){
                    Password.setError("Filled Correctly Password Gap!!!");
                }
//                if (getPassword.length() < 6){
//                    Password.setError("Password must be more than 6 character!");
//                }

                if (!TextUtils.isEmpty(getEmail) && !TextUtils.isEmpty(getPassword)){
                    progressBar.setVisibility(View.VISIBLE);


                    firebaseAuth.signInWithEmailAndPassword(getEmail, getPassword)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                                    if (!task.isSuccessful()){
                                        Toast.makeText(LoginActivity.this, "Error!" + task.getException(), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    else{
                                        try {
                                            if (firebaseUser.isEmailVerified()){
                                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                progressBar.setVisibility(View.GONE);
                                            }else{
                                                Toast.makeText(context, "Email is not verified, please check your email messages!", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }catch (NullPointerException e){
                                            Log.d(TAG, "Error!" + e.getMessage());
                                        }
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }

            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        /*
        if the user logged in, the user enter the Home Activity
         */


    }

    private void setupFireBase(){
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


            }
        };
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
