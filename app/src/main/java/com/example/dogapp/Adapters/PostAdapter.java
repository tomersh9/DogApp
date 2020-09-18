package com.example.dogapp.Adapters;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
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



    public PostAdapter(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.post_card_view,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        final String uId = postList.get(position).getuId();
        String uName = postList.get(position).getuName();
        final String pId = postList.get(position).getpId();
        String pDesc = postList.get(position).getpDesc();
        String pTimeStamp = postList.get(position).getpTime();
        String pLoc = postList.get(position).getuLoc();
        String uPic = postList.get(position).getuPic();


        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm  aa", calendar).toString();

        holder.nameTv.setText(uName);
        holder.timeTv.setText(pTime);
        holder.descTv.setText(pDesc);
        //holder.postIv.setImageURI(Uri.parse(uPic));
        Glide.with(holder.itemView).asBitmap().load(Uri.parse(uPic)).placeholder(R.drawable.account_icon).into(holder.postIv);
        //holder.likesTv.setText(pLikes);
        //holder.comTv.setText(pCom);
        //holder.locTv.setText(pLoc);

        //Toast.makeText(context, postList.get(position).getuLoc(), Toast.LENGTH_SHORT).show();


        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uId, myUid,pId);
            }
        });

    }

    private void showMoreOptions(ImageButton moreBtn, String uId, String myUid, final String pId)
    {
        PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);

        //show delete option only in the posts of the current user
        if(uId.equals(myUid))
        {
            // adding items in the menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
        }


        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0)
                {
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

                for(DataSnapshot ds : snapshot.getChildren())
                {
                    if(ds.child("pId").getValue() == pId)
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

    class MyHolder extends RecyclerView.ViewHolder
    {

        ImageView postIv;
        TextView nameTv, timeTv, descTv, likesTv, comTv;
        ImageButton moreBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            postIv = itemView.findViewById(R.id.post_img);
            nameTv = itemView.findViewById(R.id.tv_name);
            timeTv = itemView.findViewById(R.id.tv_time);
            descTv = itemView.findViewById(R.id.tv_description);
            likesTv = itemView.findViewById(R.id.tv_likes);
            comTv = itemView.findViewById(R.id.tv_comments);
            moreBtn = itemView.findViewById(R.id.more_btn);

        }
    }
}