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

import com.bumptech.glide.Glide;
import com.example.dogapp.Models.ModelPost;
import com.example.dogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    Context context;
    List<ModelPost> postList;
    String myUid;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    boolean mProcessLike = false;


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
        String uName = postList.get(position).getuName();
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
        holder.likesTv.setText(pLikes + " " + "Likes");
        holder.comTv.setText(pComments + " " + "Comments");

        setLikes(holder, pId);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uId, myUid, pId);
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
                        if(mProcessLike)
                        {
                            if(snapshot.child(postId).hasChild(myUid))
                            {
                                postsRef.child(postId).child("pLikes").setValue("" + (pLikes-1));
                                likesRef.child(postId).child(myUid).removeValue();
                                mProcessLike = false;
                            }
                            else
                            {
                                postsRef.child(postId).child("pLikes").setValue("" + (pLikes+1));
                                likesRef.child(postId).child(myUid).setValue("Liked");
                                mProcessLike = false;
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

                if(snapshot.child(postKey).hasChild(myUid))
                {
                    //holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_blue_24,0,0,0);
                    holder.likeBtn.setImageResource(R.drawable.ic_baseline_thumb_up_black_24);
                    holder.likeBtn.setColorFilter(Color.parseColor("#fe8c00"));
                    holder.likeBtnTv.setText("Liked");
                    holder.likeBtnTv.setTextColor(Color.parseColor("#fe8c00"));
                }
                else
                {
                    //holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_black_24,0,0,0);
                    holder.likeBtn.setImageResource(R.drawable.ic_baseline_thumb_up_black_24);
                    holder.likeBtnTv.setText("Like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void showMoreOptions(ImageButton moreBtn, String uId, String myUid, final String pId) {
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
                    beginDelete(pId);
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void beginDelete(final String pId) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts");
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Toast.makeText(context, pId, Toast.LENGTH_SHORT).show();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("pId").getValue() == pId)
                        ds.getRef().removeValue();
                    //Toast.makeText(context, ds.child("pId").getValue() + "", Toast.LENGTH_SHORT).show();
                    //if(ds.child("pId").getKey() == pId)
                    //Toast.makeText(context, "Have To delete", Toast.LENGTH_SHORT).show();
                    //ds.getRef().removeValue();

                }
                //deleted
                Toast.makeText(context, "Posts Deleted", Toast.LENGTH_SHORT).show();
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
        TextView nameTv, timeTv, descTv, likesTv, comTv,likeBtnTv;
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
}