package com.example.dogapp.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.Review;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter  extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;
    private Context context;

    public ReviewAdapter(List<Review> reviews, Context context) {
        this.reviews = reviews;
        this.context = context;
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        ImageView profileIv;
        ImageView star1, star2, star3, star4, star5;
        TextView locationTv, timeTv, nameTv, contentTv;
        List<ImageView> starList;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.review_img);
            locationTv = itemView.findViewById(R.id.location_review_tv);
            timeTv = itemView.findViewById(R.id.time_review_tv);
            nameTv = itemView.findViewById(R.id.name_review_tv);
            contentTv = itemView.findViewById(R.id.content_review_tv);
            star1 = itemView.findViewById(R.id.star_1_cell);
            star2 = itemView.findViewById(R.id.star_2_cell);
            star3 = itemView.findViewById(R.id.star_3_cell);
            star4 = itemView.findViewById(R.id.star_4_cell);
            star5 = itemView.findViewById(R.id.star_5_cell);

            starList = new ArrayList<>();
            starList.add(star1);
            starList.add(star2);
            starList.add(star3);
            starList.add(star4);
            starList.add(star5);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_cell_card_view,null);
        ReviewViewHolder viewHolder = new ReviewViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.nameTv.setText(review.getFullName());
        holder.locationTv.setText(review.getLocation());
        holder.contentTv.setText(review.getDescription());
        holder.timeTv.setText(review.getTimeStamp());

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(review.getTimeStamp()));
        String pTime = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
        //review.setTimeStamp(pTime);
        holder.timeTv.setText(pTime);

        try {
            Glide.with(holder.itemView).asBitmap().load(review.getProfileUrl()).into(holder.profileIv);
        } catch (Exception ex) {
            ex.getMessage();
        }

        int rateNum = review.getRateNumber();
        for(int i = 0 ; i < rateNum ; i ++) {
            holder.starList.get(i).setImageResource(R.drawable.star_full_128); //make 64 !!!!!!!!
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }
}
