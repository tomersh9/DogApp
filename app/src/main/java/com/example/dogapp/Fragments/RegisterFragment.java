package com.example.dogapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {

    private boolean isValid;
    private TextInputLayout fullNameEt, emailEt, passwordValidEt, passwordEt;

    public interface OnRegisterFragmentListener {
        void onNext(String fullName,String email, String password);
        void onBack();
    }

    private OnRegisterFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //in case the Activity doesn't implements the interface
        try {
            listener = (OnRegisterFragmentListener) context; //the activity is the callback
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement OnRegisterFragmentListener interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.register_fragment_layout, container, false);

        fullNameEt = rootView.findViewById(R.id.full_name_input);
        emailEt = rootView.findViewById(R.id.email_input);
        //usernameEt = rootView.findViewById(R.id.user_input);
        passwordEt = rootView.findViewById(R.id.pass_input);
        passwordValidEt = rootView.findViewById(R.id.pass_valid_input);

        Button nextBtn = rootView.findViewById(R.id.next_register_frag_btn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = fullNameEt.getEditText().getText().toString().trim();
                String email = emailEt.getEditText().getText().toString().trim();
                //String username = usernameEt.getEditText().getText().toString().trim();
                String passValid = passwordValidEt.getEditText().getText().toString().trim();
                String pass = passwordEt.getEditText().getText().toString().trim();

                isValid = verifyFields(name, email, passValid, pass); //check all fields validation

                if (isValid) {
                    listener.onNext(name,email,pass);
                }
            }
        });

        ImageButton backBtn = rootView.findViewById(R.id.back_frag_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBack();
            }
        });

        return rootView;
    }

    private boolean verifyFields(String name, String email, String passValid, String pass) {
        if (!validateName(name) | !validateEmail(email) | !validatePass(pass) | !confirmPass(pass,passValid)) {
            return false;
        }
        return true;
    }

    private boolean validatePass(String pass) {
        if (pass.isEmpty()) {
            passwordEt.setError(getString(R.string.field_empty_error));
            return false;
        }
        /*else if(pass.length() < 6) {
            passwordEt.setError(getString(R.string.at_least_six));
            return false;
        }*/
        else {
            passwordEt.setError(null);
            return true;
        }
    }

    private boolean confirmPass(String pass, String passValid) {
        if (!pass.equals(passValid)) {
            passwordValidEt.setError(getString(R.string.pass_no_match_error));
            return false;
        }
        else {
            passwordValidEt.setError(null);
            return true;
        }
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailEt.setError(getString(R.string.field_empty_error));
            return false;

        } else {
            emailEt.setError(null);
            return true;
        }
    }

    private boolean validateName(String name) {
        if (name.isEmpty()) {
            fullNameEt.setError(getString(R.string.field_empty_error));
            return false;

        } else {
            fullNameEt.setError(null);
            return true;
        }
    }
}
