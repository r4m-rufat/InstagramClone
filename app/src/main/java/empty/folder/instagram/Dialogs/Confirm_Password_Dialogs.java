package empty.folder.instagram.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import empty.folder.instagram.R;

public class Confirm_Password_Dialogs extends DialogFragment {

    public interface OnConfirmPasswordListener{

        public void onConfirmPassword(String Password);

    }

    OnConfirmPasswordListener onConfirmPasswordListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);

        final EditText ePassword = view.findViewById(R.id.txtConfirmPassword);
        TextView confirm = view.findViewById(R.id.txtDialogConfirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String password = ePassword.getText().toString();
                if (!password.equals("")){
                    onConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }else{
                    Toast.makeText(getActivity(), "You must enter the password!!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        TextView cancel = view.findViewById(R.id.txtDialogCancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {

            onConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();

        }catch (ClassCastException e){

        }
    }
}
