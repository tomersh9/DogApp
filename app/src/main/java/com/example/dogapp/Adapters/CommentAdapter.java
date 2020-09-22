package com.example.dogapp.Adapters;

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
import com.example.dogapp.Models.ModelComment;
import com.example.dogapp.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.myHolder> {

    Context context;
    List<ModelComment> commentList;

    public CommentAdapter(Context context, List<ModelComment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_comment,parent,false);

        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {

        //get the data

        String uId = commentList.get(position).getuId();
        String uName = commentList.get(position).getuName();
        String uPic = commentList.get(position).getuPic();
        String cId = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timeStamp = commentList.get(position).getTimeStamp();

        //set the data




        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm  aa", calendar).toString();

        holder.nameTv.setText(uName);
        holder.descTv.setText(comment);
        holder.timeTv.setText(pTime);
        Glide.with(holder.itemView).asBitmap().load(Uri.parse(uPic)).placeholder(R.drawable.account_icon).into(holder.picIv);



    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class myHolder extends RecyclerView.ViewHolder

    {
        ImageView picIv;
        TextView nameTv, descTv, timeTv;

        public myHolder(@NonNull View itemView) {
            super(itemView);

            picIv = itemView.findViewById(R.id.comment_img);
            nameTv = itemView.findViewById(R.id.name_comment_tv);
            descTv = itemView.findViewById(R.id.description_comment_tv);
            timeTv = itemView.findViewById(R.id.time_comment_tv);


        }

    }

}
