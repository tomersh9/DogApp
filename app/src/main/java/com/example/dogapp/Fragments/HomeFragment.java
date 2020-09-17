package com.example.dogapp.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogapp.Enteties.User;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.PostAdapter;
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

public class HomeFragment extends Fragment {

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


    //post bottom sheet
    private RelativeLayout bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextInputLayout postEt;
    private Button postBtn;
    private ImageButton arrowBtn;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        View rootView =  inflater.inflate(R.layout.home_fragment,container,false);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.home_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        progressBar = rootView.findViewById(R.id.home_progress_bar);

        //init post list
        postList = new ArrayList<>();
        loadPosts();

        progressDialog = new ProgressDialog(getContext());
        firebaseAuth = FirebaseAuth.getInstance();
        fUser = firebaseAuth.getCurrentUser();
        uid = firebaseAuth.getUid();
        userDbRef = FirebaseDatabase.getInstance().getReference("users");

        userDbRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Toast.makeText(getActivity(), "Succeed!!!!", Toast.LENGTH_SHORT).show();

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

        bottomSheet = rootView.findViewById(R.id.bottom_sheet_post);
        postEt = rootView.findViewById(R.id.post_et);
        postBtn = rootView.findViewById(R.id.post_btn);
        arrowBtn = rootView.findViewById(R.id.arrow_post);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        fab = rootView.findViewById(R.id.home_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                fab.hide();
                bottomSheetBehavior.setHideable(false);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), postEt.getEditText().getText().toString(), Toast.LENGTH_SHORT).show();
                String description = postEt.getEditText().getText().toString();
                uploadPost(description);
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                postEt.getEditText().setText("");
                //postAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), postList.size() + "", Toast.LENGTH_SHORT).show();
                fab.show();
            }
        });

        arrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                postEt.getEditText().setText("");
                fab.show();
            }
        });

        return rootView;
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

        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("pid",timeStamp);
        hashMap.put("pDesc", description);
        hashMap.put("pTime", timeStamp);
        hashMap.put("uLoc", location);
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
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }
}