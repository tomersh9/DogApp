package com.example.dogapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.dogapp.Adapters.WalkerAdapter;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WalkerBoardFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, WalkerAdapter.MyWalkerAdapterListener {

    //List
    private RecyclerView recyclerView;
    private WalkerAdapter adapter;
    private List<User> walkersList;

    //UI
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    //firebase
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

    public interface MyWalkerBoardFragmentListener {
        void onWalkerClicked(String userID, String imgURL);
    }

    private MyWalkerBoardFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MyWalkerBoardFragmentListener) context; //the activity is the callback
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement MyWalkerBoardFragmentListener interface");
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

        View rootView = inflater.inflate(R.layout.walker_board_fragment_layout, container, false);
        progressBar = rootView.findViewById(R.id.discover_walkers_progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.discover_walkers_swiper);
        swipeRefreshLayout.setOnRefreshListener(this);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.discover_walkers_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

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
                adapter = new WalkerAdapter(walkersList, getActivity(),fUser.getUid());
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
        User user = walkersList.get(pos);
        listener.onWalkerClicked(user.getId(), user.getPhotoUrl());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.walker_board_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.walker_menu_item_search:
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

            case R.id.price_low_to_high_item_sub_menu:
                Collections.sort(walkersList, new Comparator<User>() {
                    @Override
                    public int compare(User user1, User user2) {
                        if (user1.getPaymentPerWalk() > user2.getPaymentPerWalk()) {
                            return 1;
                        } else if (user1.getPaymentPerWalk() < user2.getPaymentPerWalk()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                adapter.notifyItemRangeChanged(0, walkersList.size());
                Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.pricing_low_to_high, Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.price_high_to_low_item_sub_menu:
                Collections.sort(walkersList, new Comparator<User>() {
                    @Override
                    public int compare(User user1, User user2) {
                        if (user1.getPaymentPerWalk() < user2.getPaymentPerWalk()) {
                            return 1;
                        } else if (user1.getPaymentPerWalk() > user2.getPaymentPerWalk()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                adapter.notifyItemRangeChanged(0, walkersList.size());
                Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.pricing_high_to_low, Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.rating_high_to_low_item_sub_menu:
                Collections.sort(walkersList, new Comparator<User>() {
                    @Override
                    public int compare(User user1, User user2) {
                        if (user1.getRating() < user2.getRating()) {
                            return 1;
                        } else if (user1.getRating() > user2.getRating()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                adapter.notifyItemRangeChanged(0, walkersList.size());
                Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.rating_high_to_low, Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.rating_low_to_high_item_sub_menu:
                Collections.sort(walkersList, new Comparator<User>() {
                    @Override
                    public int compare(User user1, User user2) {
                        if (user1.getRating() > user2.getRating()) {
                            return 1;
                        } else if (user1.getRating() < user2.getRating()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                adapter.notifyItemRangeChanged(0, walkersList.size());
                Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.rating_low_to_high, Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.exp_high_to_low_item_sub_menu:
                Collections.sort(walkersList, new Comparator<User>() {
                    @Override
                    public int compare(User user1, User user2) {

                        if (user1.getExperience() < user2.getExperience()) {
                            return 1;
                        } else if (user1.getExperience() > user2.getExperience()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                adapter.notifyItemRangeChanged(0, walkersList.size());
                Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.exp_high_to_low, Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.exp_low_to_high_item_sub_menu:
                Collections.sort(walkersList, new Comparator<User>() {
                    @Override
                    public int compare(User user1, User user2) {
                        if (user1.getExperience() > user2.getExperience()) {
                            return 1;
                        } else if (user1.getExperience() < user2.getExperience()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                adapter.notifyItemRangeChanged(0, walkersList.size());
                Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.exp_low_to_high, Snackbar.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
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
