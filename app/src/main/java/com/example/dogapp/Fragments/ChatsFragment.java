package com.example.dogapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogapp.Adapters.ChatUsersAdapter;
import com.example.dogapp.Enteties.ChatList;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatsFragment extends Fragment implements ChatUsersAdapter.MyChatUserListener {

    public interface OnChatClickListener {
        void onChatClicked(String userID);
    }

    private OnChatClickListener listener;

    //List
    private RecyclerView recyclerView;
    private ChatUsersAdapter userAdapter;
    private List<User> users;
    private List<ChatList> allChatsList;

    //firebase
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference databaseReference;

    //UI
    private ProgressBar progressBar;

    //MainActivity must implement first
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnChatClickListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement OnChatClickListener interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        View rootView = inflater.inflate(R.layout.chats_page_fragment, container, false);
        progressBar = rootView.findViewById(R.id.chat_fragment_progress_bar);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.chat_fragment_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init friends list
        allChatsList = new ArrayList<>();
        //load all users with whom i chatted
        loadAllMyChats();

        return rootView;
    }

    //pulling all the users id's that i chatted with and then create user list with recyclerview of those users in the chats page
    private void loadAllMyChats() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference = FirebaseDatabase.getInstance().getReference("chatList").child(fUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                allChatsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatList chatList = ds.getValue(ChatList.class);
                    allChatsList.add(chatList);
                }
               // updateUserWithTimeStamp();
                createChatList(); //create users list with recyclerview
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateUserWithTimeStamp() {
        users = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());

        String timeStamp = String.valueOf(System.currentTimeMillis());

        databaseReference.child("timeStamp").setValue(timeStamp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //Toast.makeText(getActivity(), "YESSSS", Toast.LENGTH_SHORT).show();

            }
        });

//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for (DataSnapshot ds : snapshot.getChildren()) {
//
//                    if()
//
//                    String timeStamp = String.valueOf(System.currentTimeMillis());
//                    databaseReference.setValue(timeStamp);
//                    Toast.makeText(getActivity(), "success!!!!", Toast.LENGTH_SHORT).show();
//
////                    User user = ds.getValue(User.class);
////                    for (ChatList chatList : allChatsList) {
////                        if (user.getId().equals(chatList.getId())) {
////
////                            users.add(user);
////                        }
////                    }
//                }
//                //Toast.makeText(getActivity(), "success!!!!", Toast.LENGTH_SHORT).show();
//                userAdapter = new ChatUsersAdapter(users, getActivity());
//                userAdapter.setMyChatUserListener(ChatsFragment.this);
//                recyclerView.setAdapter(userAdapter);
//                progressBar.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                progressBar.setVisibility(View.GONE);
//                Toast.makeText(getActivity(), "cancel", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void createChatList() {
        users = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                users.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    for (ChatList chatList : allChatsList) {
                        if (user.getId().equals(chatList.getId())) {

                            users.add(user);
//                            Collections.sort(users);
//                            Toast.makeText(getActivity(), users.size() + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                Collections.sort(users);
                //Toast.makeText(getActivity(), users.size() + "", Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(), users.size()+"", Toast.LENGTH_SHORT).show();
                userAdapter = new ChatUsersAdapter(users, getActivity());
                userAdapter.setMyChatUserListener(ChatsFragment.this);
                recyclerView.setAdapter(userAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                //Toast.makeText(getActivity(), "cancel", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChatUserClicked(int pos, View v) {
        listener.onChatClicked(users.get(pos).getId());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }
}
