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

import com.example.dogapp.R;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordFragment extends Fragment {

    Button sendBtn;
    TextInputLayout emailEt;
    ImageButton backBtn;

    public interface OnForgotPasswordListener {
        void sendEmail(String emailToSend);
        void onForgotBack();
    }

    private OnForgotPasswordListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnForgotPasswordListener)context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement OnForgotPasswordListener interface");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.forgot_password_fragment_layout,container,false);
        sendBtn = rootView.findViewById(R.id.send_email_btn);
        emailEt = rootView.findViewById(R.id.forgot_email_input);
        backBtn = rootView.findViewById(R.id.back_frag_btn_3);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailEt.getEditText().getText().toString().isEmpty()) {
                    emailEt.setError(getString(R.string.field_empty_error));
                }
                else {
                    emailEt.setError(null);
                    listener.sendEmail(emailEt.getEditText().getText().toString());
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onForgotBack();
            }
        });

        return rootView;
    }
}
