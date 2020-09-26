package com.example.dogapp.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
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

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.dogapp.Activities.InChatActivity;
import com.example.dogapp.Activities.LoginActivity;
import com.example.dogapp.Adapters.CommentAdapter;
import com.example.dogapp.Adapters.PostAdapter;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.Models.ModelComment;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private FirebaseMessaging firebaseMessaging;

    private String uid, name, location;

    //posts and comments bottom sheet
    private EditText homeEt;
    private ImageView homeIv;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private AlertDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    //comment references
    TextView name_comment;
    TextView description_comment;
    TextView time_comment;
    EditText commentEt;
    ImageView picture_comment;
    View bottomSheetView1;
    ProgressDialog progressBarComment;
    RecyclerView recyclerViewComments;
    List<ModelComment> commentList;
    CommentAdapter commentAdapter;

    //PUSH NOTIFICATION
    private final String SERVER_KEY = "AAAAsSPUwiM:APA91bF5T2kokP05wtjBjEwMiUXAuB9OXF4cCSgqf4HV9ST1kzKuD9w3ncboYoGTZxMQbBSv0EocqTcycHE4gGzFDDeGIYkyLolsd3W1gY1ZPu5qCHjpNAh-H3g0Y-JvNUIZ1iOm8uOW";
    private final String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    boolean s;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(true);

        firebaseMessaging = FirebaseMessaging.getInstance();

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

        //*******ADD NEW POST BUTTONS*********//
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

        final EditText postEt = bottomSheetView.findViewById(R.id.post_et);
        ImageButton postBtn = bottomSheetView.findViewById(R.id.post_btn);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = postEt.getText().toString();
                uploadPost(description);
                postEt.setText("");
                dialog.dismiss();
            }
        });
        dialog.setContentView(bottomSheetView);
        dialog.show();
    }

    @Override
    public void onCommentClicked(String pId) {
        buildCommentSheetDialog(pId);
    }

    private void buildCommentSheetDialog(final String pId) {

        s = isMyPost(pId);


        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        bottomSheetView1 = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_add_comment, null);

        recyclerViewComments = bottomSheetView1.findViewById(R.id.bottom_comments_recycler);

        commentEt = bottomSheetView1.findViewById(R.id.comment_et);
        ImageButton commentBtn = bottomSheetView1.findViewById(R.id.comment_btn);
        name_comment = bottomSheetView1.findViewById(R.id.owner_post_name_tv);
        description_comment = bottomSheetView1.findViewById(R.id.owner_post_tv);
        time_comment = bottomSheetView1.findViewById(R.id.owner_time);
        picture_comment = bottomSheetView1.findViewById(R.id.owner_post_img);

        loadPostInfo(pId);

        //users info - already exists;

        loadComments(pId);

//        commentAdapter = new CommentAdapter(getActivity(),commentList);
//
//        recyclerViewComments.setAdapter(commentAdapter);


        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(pId);
                commentEt.setText("");
                //dialog.dismiss();
            }
        });
        dialog.setContentView(bottomSheetView1);
        dialog.show();
    }

    private void loadComments(String pId) {

        //recyclerViewComments = getActivity().findViewById(R.id.bottom_comments_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewComments.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(pId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    commentAdapter = new CommentAdapter(bottomSheetView1.getContext(), commentList);

                    recyclerViewComments.setAdapter(commentAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment(final String pId) {
        progressBarComment = new ProgressDialog(getActivity());
        progressBarComment.setMessage("Adding Comment...");


        String comment = commentEt.getText().toString();

        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(getActivity(), "Comment is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String myUid = fUser.getUid();
        String myUserPic = fUser.getPhotoUrl().toString();
        String myName = fUser.getDisplayName();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts").child(pId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("uId", myUid);
        hashMap.put("uPic", myUserPic);
        hashMap.put("uName", myName);

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        progressBarComment.dismiss();
                        commentEt.setText("");
                        updateCommentCount(pId);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarComment.dismiss();
                    }
                });

        if(s == true) {
            firebaseMessaging.subscribeToTopic(pId);
            sendToToken(pId, comment,true);
            Toast.makeText(getActivity(), "MINE!!!!", Toast.LENGTH_SHORT).show();
        }

        else {
            Toast.makeText(getActivity(), "NOT MINE!!!!", Toast.LENGTH_SHORT).show();
            firebaseMessaging.subscribeToTopic(pId + "COMMENTS");
            sendToToken(pId + "COMMENTS", comment,false);
            sendToToken(pId, comment,true);
        }

    }

    boolean mProcessComment = false;

    private void updateCommentCount(String pId) {

        mProcessComment = true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(pId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment) {
                    String comments = "" + snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue("" + newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        hashMap.put("pLikes", "0");
        hashMap.put("pComments", "0");

        firebaseMessaging.subscribeToTopic(hashMap.get("pId"));
        firebaseMessaging.subscribeToTopic(hashMap.get("pId") + "Likes");


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

    private void loadPostInfo(String pId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String pDesc = ds.child("pDesc").getValue(String.class);
                    String pId = "" + ds.child("pId").getValue();
                    String pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = ds.child("pTime").getValue(String.class);
                    String uId = "" + ds.child("uId").getValue();
                    String uLoc = "" + ds.child("uLoc").getValue();
                    String uName = ds.child("uName").getValue(String.class);
                    String uPic = "" + ds.child("uPic").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();


                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm  aa", calendar).toString();

                    //set data
                    description_comment.setText(pDesc);
                    name_comment.setText(uName);
                    time_comment.setText(pTime);
                    try {
                        Glide.with(bottomSheetView1).asBitmap().load(Uri.parse(uPic)).into(picture_comment);
                    } catch (Exception ex) {
                        ex.getMessage();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void sendNotification(final String pId) {
//
//        //getting the user's token
//        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens");
//        tokenRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    hisToken = snapshot.child("token").getValue(String.class);
//                    sendToToken(msg);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(), "NO TOKEN FOUND!", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }

    private void sendToToken(String pId, String comment, boolean isMyComment) {

        //setting data with JSON objects to get the children
        final JSONObject rootJson = new JSONObject(); //we put here "data" and "to"
        final JSONObject dataJson = new JSONObject();

        try {
            if (pId != null) {

                dataJson.put("message", comment);
                if(isMyComment) {
                    dataJson.put("isCom", "check");
                }
                else {
                    dataJson.put("isComFriend", "check");
                }

                dataJson.put("nameComment", name_comment.getText().toString());
                dataJson.put("fullName", fUser.getDisplayName());
//                dataJson.put("uID", fUser.getUid());
                rootJson.put("to", "/topics/" + pId);
                rootJson.put("data", dataJson);

            } else {
                return; //no token found
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        //create POST request
        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, BASE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) { //POST REQUEST class implementation

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + SERVER_KEY);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return rootJson.toString().getBytes(); //return the root object with data inside
            }
        };

        //sending the actual request
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(stringRequest);
    }
    private boolean isMyPost(String pId)
    {

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts").child(pId).child("uId");
        final String isMyPost;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    s = true;
                    //Toast.makeText(getActivity(), s + "%%111111%%", Toast.LENGTH_SHORT).show();
                }
                else
                    s = false;


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Toast.makeText(getActivity(), s + "%%%%", Toast.LENGTH_SHORT).show();
        return s;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }
}