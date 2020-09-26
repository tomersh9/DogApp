package com.example.dogapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;

import java.util.List;

public class WalkerAdapter extends RecyclerView.Adapter<WalkerAdapter.WalkerViewHolder> {

    private List<User> walkersList;
    private MyWalkerAdapterListener listener;

    public interface MyWalkerAdapterListener {
        void onWalkerClicked(int pos);
    }

    public WalkerAdapter(List<User> walkersList) {
        this.walkersList = walkersList;
    }

    public void setWalkerAdapterListener(MyWalkerAdapterListener listener) {
        this.listener = listener;
    }

    public class WalkerViewHolder extends RecyclerView.ViewHolder {

        ImageView profileIv;
        TextView nameTv, locationTv, ageGenderTv;
        ImageView star1, star2, star3, star4, star5;

        public WalkerViewHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.walker_cell_image);
            nameTv = itemView.findViewById(R.id.walker_cell_name_tv);
            locationTv = itemView.findViewById(R.id.walker_cell_location_tv);
            ageGenderTv = itemView.findViewById(R.id.walker_cell_gender_age_tv);
            star1 = itemView.findViewById(R.id.star_1);
            star2 = itemView.findViewById(R.id.star_2);
            star3 = itemView.findViewById(R.id.star_3);
            star4 = itemView.findViewById(R.id.star_4);
            star5 = itemView.findViewById(R.id.star_5);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onWalkerClicked(getAdapterPosition());
                }
            });
        }
    }

    @NonNull
    @Override
    public WalkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.walker_card_view, null);
        WalkerViewHolder viewHolder = new WalkerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WalkerViewHolder holder, int position) {
        //get walker instance
        User walkerUser = walkersList.get(position);

        //assign views with his data
        holder.nameTv.setText(walkerUser.getFullName());
        holder.locationTv.setText(walkerUser.getLocation());
        holder.ageGenderTv.setText(walkerUser.getGender());

        //assign profile image with Glide
        try {
            Glide.with(holder.itemView).load(walkerUser.getPhotoUri()).placeholder(R.drawable.account_icon).into(holder.profileIv);
        } catch (Exception ex) {
            ex.getMessage();
        }
    }

    @Override
    public int getItemCount() {
        return walkersList.size();
    }
}
