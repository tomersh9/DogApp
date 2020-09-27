package com.example.dogapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.dogapp.Adapters.WalkerAdapter;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WalkerBoardFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, WalkerAdapter.MyWalkerAdapterListener {

    //List
    private RecyclerView recyclerView;
    private WalkerAdapter adapter;
    private List<User> walkersList;

    //UI
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    //Firebase
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        View rootView = inflater.inflate(R.layout.walker_board_fragment_layout, container, false);
        progressBar = rootView.findViewById(R.id.discover_walkers_progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.discover_walkers_swiper);
        swipeRefreshLayout.setOnRefreshListener(this);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.discover_walkers_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init list of walkers
        walkersList = new ArrayList<>();
        getAllWalkers();

        return rootView;
    }

    private void getAllWalkers() {

        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                walkersList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    //get only walkers
                    if (user.getType()) {
                        walkersList.add(user);
                    }
                }

                //assign list to recyclerview
                adapter = new WalkerAdapter(walkersList);
                recyclerView.setAdapter(adapter);
                adapter.setWalkerAdapterListener(WalkerBoardFragment.this);
                adapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onWalkerClicked(int pos) {
        //move to walker's profile fragment
    }

    @Override
    public void onRefresh() {
        getAllWalkers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }
}
