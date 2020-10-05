package com.example.dogapp.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.dogapp.Activities.InChatActivity;
import com.example.dogapp.Adapters.FriendsAdapter;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends Fragment implements FriendsAdapter.MyUserListener {

    private final String FOLLOWERS_FRAGMENT_TAG = "followers_tag";
    private final String PROFILE_FRAGMENT_TAG = "profile_fragment_tag";

    //List
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private List<User> users;
    private List<String> followingList = new ArrayList<>();
    private List<String> followersList = new ArrayList<>();

    //firebase
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following");
    private DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers");
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private String userID;
    private boolean isMe;

    //UI
    private ProgressBar progressBar;
    //private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isRefreshing = false;
    private TextView noFollowersTv;

    public static FollowersFragment newInstance(String userID) {
        FollowersFragment fragment = new FollowersFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userID", userID);
        fragment.setArguments(bundle);
        return fragment; //holds the bundle
    }

    public interface MyFollowersFragmentListener {
        void onFollowersFragmentBackPress();
    }

    private MyFollowersFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MyFollowersFragmentListener) context; //the activity is the callback
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement MyFollowersFragmentListener interface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        View rootView = inflater.inflate(R.layout.friends_fragment_layout, container, false);
        noFollowersTv = rootView.findViewById(R.id.no_friends_msg_tv);
        noFollowersTv.setText(R.string.no_followers_yet);

        //get other users to user this fragment
        userID = getArguments().getString("userID");
        if (userID.equals(fUser.getUid())) {
            isMe = true;
        } else {
            isMe = false;
        }

        progressBar = rootView.findViewById(R.id.friends_fragment_progress_bar);
        //swipeRefreshLayout = rootView.findViewById(R.id.friends_swiper);
        //swipeRefreshLayout.setOnRefreshListener(this);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.friends_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init friends list
        users = new ArrayList<>();
        getAllFollowers(); //get all users and create the adapter and assign to recyclerview

        return rootView;
    }

    private void getAllFollowers() {

        /*if (!isRefreshing) { //swiper already refreshing
            progressBar.setVisibility(View.VISIBLE);
        }*/

        followersRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    followersList.add(ds.getValue(String.class)); //filter only the ones i follow
                }
                getUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getUsers() {

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        if (followersList.contains(user.getId())) {
                            users.add(user);
                        }
                    }
                    //adapter
                    adapter = new FriendsAdapter(users, true, isMe, getActivity());
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapter);
                    //adapter click events
                    adapter.setMyUserListener(FollowersFragment.this);
                    adapter.notifyDataSetChanged();

                    if(users.isEmpty()) {
                        noFollowersTv.setVisibility(View.VISIBLE);
                    } else {
                        noFollowersTv.setVisibility(View.GONE);
                    }
                }
                progressBar.setVisibility(View.GONE);
                //swipeRefreshLayout.setRefreshing(false);
                isRefreshing = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                //swipeRefreshLayout.setRefreshing(false);
                isRefreshing = false;
            }
        });
    }

    //Adapter events to handle outside
    @Override
    public void onFriendClicked(int pos, View v) {
        User user = users.get(pos);
        ProfileFragment profileFragment = ProfileFragment.newInstance(user.getId(), user.getPhotoUrl());
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment, PROFILE_FRAGMENT_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onFriendChatClicked(int pos, View v) {
        //go to chat (activity)
        Intent intent = new Intent(getActivity(), InChatActivity.class);
        intent.putExtra("userID", users.get(pos).getId());
        startActivity(intent);
    }

    @Override
    public void onFriendFollowClicked(int pos, View v) {
        //nothing
    }

    //close fragments by tag
    private void closeFragment(String tag) {
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getActivity().getSupportFragmentManager().popBackStack(); //remove from back stack
        }
    }

    @Override
    public void onFriendDeleteClicked(int pos, View v) {
        final User user = users.get(pos);
        //remove from following list
        Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), user.getFullName() + " " + getString(R.string.was_removed_from_followers), Snackbar.LENGTH_LONG).show();

        //remove him from my followers list
        if (followersList.contains(user.getId())) {
            followersList.remove(user.getId());
            users.remove(user);
            adapter.notifyItemRemoved(pos);
            followersRef.child(userID).setValue(followersList);
        }

        //remove myself from his following list
        followingRef.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        followingList.add(ds.getValue(String.class));
                    }

                    if (followingList.contains(fUser.getUid())) {
                        followingList.remove(fUser.getUid());
                        followingRef.child(user.getId()).setValue(followingList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.follow_frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.follow_menu_search:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adapter.getFilter().filter(newText);
                        return false;
                    }
                });
                break;

            case R.id.follow_menu_back:
                listener.onFollowersFragmentBackPress();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    /*@Override
    public void onRefresh() {
        isRefreshing = true;
        getAllFollowers();
    }*/
}
