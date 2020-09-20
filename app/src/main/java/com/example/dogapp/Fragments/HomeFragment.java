package com.example.dogapp.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.dogapp.Activities.LoginActivity;
import com.example.dogapp.Adapters.PostAdapter;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements PostAdapter.OnPostListener, SwipeRefreshLayout.OnRefreshListener {

    //list of posts
    private RecyclerView recyclerView;
    private List<ModelPost> postList;
    private PostAdapter postAdapter;
    private List<String> followingList = new ArrayList<>();

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following");
    private DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");

    private String uid, name, location;

    //posts and comments bottom sheet
    private EditText homeEt;
    private ImageView homeIv;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private AlertDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(true);

        final View rootView = inflater.inflate(R.layout.home_fragment, container, false);

        //views
        progressBar = rootView.findViewById(R.id.home_progress_bar);
        coordinatorLayout = rootView.findViewById(R.id.home_frag_coordinator_layout);
        homeEt = rootView.findViewById(R.id.home_et);
        homeIv = rootView.findViewById(R.id.home_profile_img);
        swipeRefreshLayout = rootView.findViewById(R.id.home_swiper);
        swipeRefreshLayout.setOnRefreshListener(this);

        //listeners
        homeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildPostSheetDialog();
            }
        });

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.home_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postList = new ArrayList<>();
        //loadPosts();
        loadFollowing();

        //get my own details to put on the post
        uid = fUser.getUid();
        userDbRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //update things from user data
                    User user = dataSnapshot.getValue(User.class);
                    name = user.getFullName();
                    location = user.getLocation();
                    try {
                        Glide.with(rootView).load(user.getPhotoUri()).placeholder(R.drawable.account_icon).into(homeIv);
                    } catch (Exception ex) {

                    }

                } else {
                    Toast.makeText(getActivity(), "DataSnapShot doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //******************ADD NEW POST BUTTONS****************************//
        /*fab = rootView.findViewById(R.id.home_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildPostSheetDialog();
            }
        });*/

        return rootView;
    }

    private void buildPostSheetDialog() {

        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_add_post, null);

        final TextInputLayout postEt = bottomSheetView.findViewById(R.id.post_et);
        Button postBtn = bottomSheetView.findViewById(R.id.post_btn);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = postEt.getEditText().getText().toString();
                uploadPost(description);
                postEt.getEditText().setText("");
                dialog.dismiss();
            }
        });
        dialog.setContentView(bottomSheetView);
        dialog.show();
    }

    @Override
    public void onCommentClicked() {
        buildCommentSheetDialog();
    }

    private void buildCommentSheetDialog() {
        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_add_comment, null);

        final TextInputLayout commentEt = bottomSheetView.findViewById(R.id.comment_et);
        Button commentBtn = bottomSheetView.findViewById(R.id.comment_btn);
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), commentEt.getEditText().getText().toString(), Toast.LENGTH_SHORT).show();
                commentEt.getEditText().setText("");
                dialog.dismiss();
            }
        });
        dialog.setContentView(bottomSheetView);
        dialog.show();
    }

    @Override
    public void onLikeClicked() {
        Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), "LIKE", Snackbar.LENGTH_LONG).show();
    }

    //get all of your followers
    private void loadFollowing() {
        progressBar.setVisibility(View.VISIBLE);
        followingRef.child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        followingList.add(ds.getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        loadPosts();
    }

    //load posts of your followers
    private void loadPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if (followingList.contains(modelPost.getuId()) || modelPost.getuId().equals(fUser.getUid())) {
                        postList.add(modelPost);
                    }
                    postAdapter = new PostAdapter(getActivity(), postList);
                    postAdapter.setOnPostListener(HomeFragment.this);
                    recyclerView.setAdapter(postAdapter);
                }
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        loadFollowing();
    }

    private void searchPosts(final String searchQuery) {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if (modelPost.getpDesc().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(modelPost);
                    }
                    postAdapter = new PostAdapter(getActivity(), postList);
                    recyclerView.setAdapter(postAdapter);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void uploadPost(String description) {

        buildLoaderDialog(getString(R.string.upload_post));

        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;

        HashMap<Object, String> hashMap = new HashMap<>();

        hashMap.put("uLoc", location);
        hashMap.put("uName", name);
        hashMap.put("pId", timeStamp);
        hashMap.put("pDesc", description);
        hashMap.put("pTime", timeStamp);
        hashMap.put("uId", uid);
        hashMap.put("uPic", fUser.getPhotoUrl().toString());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.post_published, Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void buildLoaderDialog(String body) {
        final View dialogView;
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        dialogView = getLayoutInflater().inflate(R.layout.loader_dialog, null);
        TextView bodyTv = dialogView.findViewById(R.id.loader_tv);
        bodyTv.setText(body);
        progressDialog = builder1.setView(dialogView).setCancelable(false).show();
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

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
                    if (!TextUtils.isEmpty(query)) {
                        searchPosts(query);
                    } else {
                        loadPosts();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!TextUtils.isEmpty(newText)) {
                        searchPosts(newText);
                    } else {
                        loadPosts();
                    }
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
}