package com.example.dogapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogapp.ChatUsersAdapter;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.example.dogapp.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ChatUsersAdapter.MyChatUserListener {

    public interface OnChatClickListener {
        void onChatClicked(String userID);
    }

    private OnChatClickListener listener;

    //List
    private RecyclerView recyclerView;
    private ChatUsersAdapter adapter;
    private List<User> users;

    //firebase
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

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

        View rootView = inflater.inflate(R.layout.chat_fragment, container, false);
        progressBar = rootView.findViewById(R.id.chat_fragment_progress_bar);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.chat_fragment_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init friends list
        users = new ArrayList<>();
        getAllUsers(); //get all users and create the adapter and assign to recyclerview

        return rootView;
    }

    private void getAllUsers() {
        progressBar.setVisibility(View.VISIBLE);
        //get path of database named "users" contains users info
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        //get all data from path
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        //get all users except the logged in (you)
                        if (!user.getEmail().equals(fUser.getEmail())) {
                            users.add(user);
                        }
                    }
                    //adapter
                    adapter = new ChatUsersAdapter(users);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapter);
                    //adapter click events
                    adapter.setMyChatUserListener(ChatFragment.this);
                    adapter.notifyDataSetChanged();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onChatUserClicked(int pos, View v) {
        listener.onChatClicked(users.get(pos).getId());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }
}
