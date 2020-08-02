package empty.folder.instagram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.Login.LoginActivity;
import empty.folder.instagram.R;

public class SignOutFragment extends Fragment {

    // Firebase
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;

    // layout components
    ProgressBar progressBar;
    TextView txtSigningOut;
    Button buttonSignUp;

    public SignOutFragment() {
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_out, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        txtSigningOut = view.findViewById(R.id.txtSigningOut);
        progressBar = view.findViewById(R.id.progressSignOut);
        buttonSignUp = view.findViewById(R.id.buttonSignOut);

        txtSigningOut.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtSigningOut.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.signOut();

                getActivity().finish();
            }
        });

        setupFireBase();

        return view;

    }

    private void setupFireBase(){

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){

                }else{
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        };
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
