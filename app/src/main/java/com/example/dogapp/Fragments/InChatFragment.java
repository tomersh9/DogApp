package com.example.dogapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InChatFragment extends Fragment {

    private ImageView profileIv;
    private TextView usernameTv;

    //firebase
    private FirebaseUser fUser;
    private DatabaseReference databaseReference;

    private Toolbar toolbar;

    public interface OnInChatListener {
        void changeInChatToolbar(Toolbar toolbar);
    }

    private OnInChatListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnInChatListener) context; //the activity is the callback
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement OnInChatListener interface");
        }
    }

    //to get data from activity
    public static InChatFragment getInstance(String userID) {
        InChatFragment chatFragment = new InChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userID", userID);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.in_chat_fragment_layout,container,false);

        toolbar = rootView.findViewById(R.id.in_chat_toolbar);
        toolbar.setTitle("");
        listener.changeInChatToolbar(toolbar); //change toolbar when fragment created

        profileIv = rootView.findViewById(R.id.in_chat_profile_img);
        usernameTv = rootView.findViewById(R.id.in_chat_username);

        String userID = getArguments().getString("userID");

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User user = snapshot.getValue(User.class); //getting user im clicking on
                    usernameTv.setText(user.getFullName());
                    //set icon image of user
                    try {
                        Glide.with(InChatFragment.this).load(user.getPhotoUri()).placeholder(R.drawable.account_icon).into(profileIv);
                    } catch (Exception ex) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return rootView;
    }

}
