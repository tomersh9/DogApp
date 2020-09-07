package com.example.dogapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dogapp.R;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {

    private boolean isValid;
    private TextInputLayout fullNameEt, emailEt, usernameEt, passwordEt;

    public interface OnRegisterFragmentListener {
        void onRegister(String fullName, String email, String username, String password);
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
        usernameEt = rootView.findViewById(R.id.user_input);
        passwordEt = rootView.findViewById(R.id.pass_input);

        Button regBtn = rootView.findViewById(R.id.reg_btn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = fullNameEt.getEditText().getText().toString().trim();
                String email = emailEt.getEditText().getText().toString().trim();
                String user = usernameEt.getEditText().getText().toString().trim();
                String pass = passwordEt.getEditText().getText().toString().trim();

                isValid = verifyFields(name,email,user,pass); //check all fields validation

                if (isValid) {
                    listener.onRegister(name, email, user, pass);
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

    private boolean verifyFields(String name,String email,String user,String pass) {
        //pass all tests
        if(!validateName(name) | !validateEmail(email) | !validateUsername(user) | !validatePass(pass)) {
            return false;
        }
        return true;
    }

    private boolean validatePass(String pass) {
        //password validate
        if (pass.isEmpty()) {
            passwordEt.setError("Field cannot be empty");
            return false;

        } else {
            passwordEt.setError(null);
            return true;
        }
    }

    private boolean validateUsername(String user) {
        //username validate
        if (user.isEmpty()) {
            usernameEt.setError("Field cannot be empty");
            return false;

        } else {
            usernameEt.setError(null);
            return true;
        }
    }

    private boolean validateEmail(String email) {
        //email validate
        if (email.isEmpty()) {
            emailEt.setError("Field cannot be empty");
            return false;

        } else {
            emailEt.setError(null);
            return true;
        }
    }

    private boolean validateName(String name) {
        //name validate
        if (name.isEmpty()) {
            fullNameEt.setError("Field cannot be empty");
            return false;

        } else {
            fullNameEt.setError(null);
            return true;
        }
    }
}
