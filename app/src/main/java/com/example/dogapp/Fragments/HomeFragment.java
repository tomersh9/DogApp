package com.example.dogapp.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogapp.Adapters.PostAdapter;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class HomeFragment extends Fragment implements PostAdapter.OnPostListener {

    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private List<ModelPost> postList;
    private PostAdapter postAdapter;


    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser fUser;
    private DatabaseReference userDbRef;

    private String uid, name, location;
    private ProgressDialog progressDialog;


    //posts and comments bottom sheet
    private TextInputLayout postEt;
    private TextInputLayout commentEt;
    private RelativeLayout bottomSheetPost;
    private BottomSheetBehavior bottomSheetPostBehavior;
    private RelativeLayout bottomSheetComment;
    private BottomSheetBehavior bottomSheetCommentBehavior;
    private Button postBtn;
    private Button commentBtn;
    private ImageButton arrowPostBtn;
    private ImageButton arrowCommentBtn;
    private ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(true);

        View rootView =  inflater.inflate(R.layout.home_fragment,container,false);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.home_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        progressBar = rootView.findViewById(R.id.home_progress_bar);
        postList = new ArrayList<>();
        loadPosts();

        progressDialog = new ProgressDialog(getContext());
        firebaseAuth = FirebaseAuth.getInstance();
        fUser = firebaseAuth.getCurrentUser();
        uid = fUser.getUid();
        userDbRef = FirebaseDatabase.getInstance().getReference("users");

        userDbRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    //Toast.makeText(getActivity(), "Succeed!!!!", Toast.LENGTH_SHORT).show();

                    //update things from user data
                    User user = dataSnapshot.getValue(User.class);
                    name = user.getFullName();
                    location = user.getLocation();

                } else {
                    Toast.makeText(getActivity(), "DataSnapShot doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //post bottom sheet
        postEt = rootView.findViewById(R.id.post_et);
        bottomSheetPost = rootView.findViewById(R.id.bottom_sheet_post);
        postBtn = rootView.findViewById(R.id.post_btn);
        arrowPostBtn = rootView.findViewById(R.id.arrow_post);
        bottomSheetPostBehavior = BottomSheetBehavior.from(bottomSheetPost);
        bottomSheetPostBehavior.setHideable(true);
        bottomSheetPostBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        //comment bottom sheet
        commentEt = rootView.findViewById(R.id.comment_et);
        commentBtn = rootView.findViewById(R.id.comment_btn);
        arrowCommentBtn = rootView.findViewById(R.id.arrow_comment);
        bottomSheetComment = rootView.findViewById(R.id.bottom_sheet_comment);
        bottomSheetCommentBehavior = BottomSheetBehavior.from(bottomSheetComment);
        bottomSheetCommentBehavior.setHideable(true);
        bottomSheetCommentBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //add new comment
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), commentEt.getEditText().getText().toString(), Toast.LENGTH_SHORT).show();
                commentEt.getEditText().setText("");
                bottomSheetCommentBehavior.setHideable(true);
                bottomSheetCommentBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                fab.show();
            }
        });

        arrowCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetCommentBehavior.setHideable(true);
                bottomSheetCommentBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                commentEt.getEditText().setText("");
                fab.show();
            }
        });

        //add new post
        fab = rootView.findViewById(R.id.home_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetPostBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                fab.hide();
                bottomSheetPostBehavior.setHideable(false);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = postEt.getEditText().getText().toString();
                uploadPost(description);
                postEt.getEditText().setText("");
                bottomSheetPostBehavior.setHideable(true);
                bottomSheetPostBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                fab.show();
            }
        });

        arrowPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetPostBehavior.setHideable(true);
                bottomSheetPostBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                postEt.getEditText().setText("");
                fab.show();
            }
        });

        return rootView;
    }

    @Override
    public void onCommentClicked() {
        bottomSheetCommentBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        fab.hide();
        bottomSheetCommentBehavior.setHideable(false);
    }

    @Override
    public void onLikeClicked() {
        Toast.makeText(getActivity(), "Like!", Toast.LENGTH_SHORT).show();
    }

    private void loadPosts()
    {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);

                    postAdapter = new PostAdapter(getActivity(),postList);
                    postAdapter.setOnPostListener(HomeFragment.this);
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

    private void searchPosts(final String searchQuery)
    {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if(modelPost.getpDesc().toLowerCase().contains(searchQuery.toLowerCase()))
                    {
                        postList.add(modelPost);
                    }


                    postAdapter = new PostAdapter(getActivity(),postList);
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
    private void uploadPost(String description)
    {
        progressDialog.setMessage("Publishing post...");
        progressDialog.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;

        HashMap<Object,String> hashMap = new HashMap<>();

        hashMap.put("uLoc", location);
        hashMap.put("uName", name);
        hashMap.put("pId", timeStamp);
        hashMap.put("pDesc", description);
        hashMap.put("pTime", timeStamp);
        hashMap.put("uId", uid);
        hashMap.put("uPic", firebaseAuth.getCurrentUser().getPhotoUrl().toString());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Post Published", Toast.LENGTH_SHORT).show();


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
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
                public boolean onQueryTextSubmit(String query)
                {
                    if(!TextUtils.isEmpty(query)) {
                        searchPosts(query);
                    }
                    else {
                        loadPosts();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(!TextUtils.isEmpty(newText)) {
                        searchPosts(newText);
                    }
                    else {
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