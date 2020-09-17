package com.example.dogapp;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    Context context;
    List<ModelPost> postList;

    public PostAdapter(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.post_card_view,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String uId = postList.get(position).getuId();
        String uName = postList.get(position).getuName();
        String pId = postList.get(position).getpId();
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

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder
    {

        ImageView postIv;
        TextView nameTv, timeTv, descTv, likesTv, comTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            postIv = itemView.findViewById(R.id.post_img);
            nameTv = itemView.findViewById(R.id.tv_name);
            timeTv = itemView.findViewById(R.id.tv_time);
            descTv = itemView.findViewById(R.id.tv_description);
            likesTv = itemView.findViewById(R.id.tv_likes);
            comTv = itemView.findViewById(R.id.tv_comments);
        }
    }
}