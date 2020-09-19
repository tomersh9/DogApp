package com.example.dogapp.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dogapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PostBottomSheetDialog extends BottomSheetDialogFragment {

    public interface PostBottomSheetDialogListener {
        void onPostClicked();
    }

    private PostBottomSheetDialogListener listener;

    public PostBottomSheetDialog() {} //empty ctor

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (PostBottomSheetDialogListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("Must implement PostBottomSheetDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_post, container, false);
        Button postBtn = view.findViewById(R.id.post_btn);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPostClicked();
                dismiss();
            }
        });
        return view;
    }
}
