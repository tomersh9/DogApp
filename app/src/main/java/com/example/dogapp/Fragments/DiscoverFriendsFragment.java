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

public class DiscoverFriendsFragment extends Fragment implements FriendsAdapter.MyUserListener, SwipeRefreshLayout.OnRefreshListener {

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

    //UI
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

   /* public interface MyDiscoverFriendsFragmentListener {
        void onDiscoverFriendPress();
    }

    private MyDiscoverFriendsFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MyDiscoverFriendsFragmentListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("MainActivity must implement MyDiscoverFriendsFragmentListener interface");
        }
    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        View rootView = inflater.inflate(R.layout.discover_friends_fragment_layout, container, false);
        progressBar = rootView.findViewById(R.id.discover_friends_progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.discover_friends_swiper);
        swipeRefreshLayout.setOnRefreshListener(this);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.discover_friends_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init friends list
        users = new ArrayList<>();
        getFollowingList(); //get all users and create the adapter and assign to recyclerview

        return rootView;
    }

    private void getFollowingList() {
        progressBar.setVisibility(View.VISIBLE);

        followingRef.child(fUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    followingList.add(ds.getValue(String.class)); //filter only the ones i follow
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
        //get all data from path
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        //get all users except the logged in (you)
                        if (!followingList.contains(user.getId()) && !fUser.getUid().equals(user.getId())) {
                            users.add(user);
                        }
                    }
                    //adapter
                    adapter = new FriendsAdapter(users, false, true,getActivity());
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapter);
                    //adapter click events
                    adapter.setMyUserListener(DiscoverFriendsFragment.this);
                    adapter.notifyDataSetChanged();
                }
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //*************ADAPTER EVENTS***************//
    @Override
    public void onFriendClicked(int pos, View v) {
        //go to profile (activity)
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
        final User user = users.get(pos);
        followingList.add(user.getId());
        users.remove(user);
        adapter.notifyItemRemoved(pos);
        followingRef.child(fUser.getUid()).setValue(followingList);
        Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.you_now_follow) + " " + user.getFullName(), Snackbar.LENGTH_LONG).show();

        //get user's followers list
        followersRef.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followersList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    followersList.add(ds.getValue(String.class));
                }
                //add myself as a user
                if (!followersList.contains(fUser.getUid())) {
                    followersList.add(fUser.getUid());
                    followersRef.child(user.getId()).setValue(followersList);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onFriendDeleteClicked(int pos, View v) {
        //nothing
    }

    //*************OPTIONS MENU**********************//
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_item_search) {
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onRefresh() {
        getFollowingList();
    }

}
