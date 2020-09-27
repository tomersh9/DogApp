package com.example.dogapp.Adapters;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    Context context;
    List<ModelPost> postList;
    String myUid;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    boolean mProcessLike = false;
    boolean isLike;

    private final String SERVER_KEY = "AAAAsSPUwiM:APA91bF5T2kokP05wtjBjEwMiUXAuB9OXF4cCSgqf4HV9ST1kzKuD9w3ncboYoGTZxMQbBSv0EocqTcycHE4gGzFDDeGIYkyLolsd3W1gY1ZPu5qCHjpNAh-H3g0Y-JvNUIZ1iOm8uOW";
    private final String BASE_URL = "https://fcm.googleapis.com/fcm/send";


    public interface OnPostListener {
        void onCommentClicked(String pId);

        void onLikeClicked();
    }

    private OnPostListener listener;

    public void setOnPostListener(OnPostListener listener) {
        this.listener = listener;
    }

    public PostAdapter(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.post_card_view, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        final String uId = postList.get(position).getuId();
        final String uName = postList.get(position).getuName();
        final String pId = postList.get(position).getpId();
        String pDesc = postList.get(position).getpDesc();
        String pTimeStamp = postList.get(position).getpTime();
        String pLoc = postList.get(position).getuLoc();
        String uPic = postList.get(position).getuPic();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();


        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm  aa", calendar).toString();

        holder.nameTv.setText(uName);
        holder.timeTv.setText(pTime);
        holder.descTv.setText(pDesc);
        Glide.with(holder.itemView).asBitmap().load(Uri.parse(uPic)).placeholder(R.drawable.account_icon).into(holder.postIv);
        holder.likesTv.setText(pLikes + " " + context.getString(R.string.likes));
        holder.comTv.setText(pComments + " " + context.getString(R.string.comments));

        setLikes(holder, pId);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uId, myUid, pId,holder.getAdapterPosition());
            }
        });

        holder.commentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCommentClicked(pId);
            }
        });

        holder.likesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //listener.onLikeClicked();

                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                final String postId = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike) {
                            if (snapshot.child(postId).hasChild(myUid)) {
                                postsRef.child(postId).child("pLikes").setValue("" + (pLikes - 1));
                                likesRef.child(postId).child(myUid).removeValue();
                                mProcessLike = false;
                            } else {
                                postsRef.child(postId).child("pLikes").setValue("" + (pLikes + 1));
                                likesRef.child(postId).child(myUid).setValue("Liked");
                                mProcessLike = false;
                                sendToToken(postId,FirebaseAuth.getInstance().getCurrentUser().getDisplayName() );
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    private void setLikes(final MyHolder holder, final String postKey) {

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(postKey).hasChild(myUid)) {
                    holder.likeBtn.setImageResource(R.drawable.ic_baseline_thumb_up_black_24);
                    holder.likeBtn.setColorFilter(Color.parseColor("#fe8c00"));
                    holder.likeBtnTv.setTextColor(Color.parseColor("#fe8c00"));
                } else {
                    holder.likeBtn.setImageResource(R.drawable.ic_baseline_thumb_up_black_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uId, String myUid, final String pId,final int pos) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option only in the posts of the current user
        if (uId.equals(myUid)) {
            // adding items in the menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.delete_post);
        }

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    //delete is clicked
                    beginDelete(pId,pos);
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void beginDelete(final String pId, final int pos) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts");
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("pId").getValue() == pId) {
                        ds.getRef().removeValue();
                        //notifyItemRemoved(pos);
                    }

                    //Toast.makeText(context, ds.child("pId").getValue() + "", Toast.LENGTH_SHORT).show();
                    //if(ds.child("pId").getKey() == pId)
                    //Toast.makeText(context, "Have To delete", Toast.LENGTH_SHORT).show();
                    //ds.getRef().removeValue();
                }
                //deleted
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView postIv, likeBtn;
        TextView nameTv, timeTv, descTv, likesTv, comTv, likeBtnTv;
        ImageButton moreBtn;
        LinearLayout commentsLayout, likesLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            postIv = itemView.findViewById(R.id.post_img);
            nameTv = itemView.findViewById(R.id.tv_name);
            timeTv = itemView.findViewById(R.id.tv_time);
            descTv = itemView.findViewById(R.id.tv_description);
            likesTv = itemView.findViewById(R.id.tv_likes);
            comTv = itemView.findViewById(R.id.tv_comments);
            moreBtn = itemView.findViewById(R.id.more_btn);
            commentsLayout = itemView.findViewById(R.id.comment_btn_layout);
            likesLayout = itemView.findViewById(R.id.like_btn_layout);
            likeBtn = itemView.findViewById(R.id.like_btn);
            likeBtnTv = itemView.findViewById(R.id.like_btn_tv);
        }
    }

    private void sendToToken(String pId, String name) {

        //setting data with JSON objects to get the children
        final JSONObject rootJson = new JSONObject(); //we put here "data" and "to"
        final JSONObject dataJson = new JSONObject();

        try {
            if (pId != null) {

                dataJson.put("message", "check");
                dataJson.put("isLike", "check");
                dataJson.put("fullName", name);
//                dataJson.put("uID", fUser.getUid());
                rootJson.put("to", "/topics/" + pId + "Likes");
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
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }
}