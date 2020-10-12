package com.example.dogapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;

import java.util.ArrayList;
import java.util.List;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.UserLikesViewHolder> {

    private List<User> users;
    private MyLikesUsersListener listener;
    private Context context;

    public interface MyLikesUsersListener {
        void onUserClicked(int position);
    }

    public void setMyLikesUsersListener(MyLikesUsersListener listener) {
        this.listener = listener;
    }

    public LikesAdapter(List<User> users, Context context) {
        this.users = users;
        this.context = context;
    }

    //inner class
    public class UserLikesViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTv;
        ImageView profileIv;

        //constructor
        public UserLikesViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTv = itemView.findViewById(R.id.likes_cell_name);
            profileIv = itemView.findViewById(R.id.like_cell_img);

            //on on cell click event
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onUserClicked(getAdapterPosition());
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public UserLikesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.likes_cell_card_view, parent, false);
        UserLikesViewHolder userLikesViewHolder = new UserLikesViewHolder(view);
        return userLikesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserLikesViewHolder holder, int position) {

        final User user = users.get(position);

        holder.usernameTv.setText(user.getFullName());

        try {
            Glide.with(holder.itemView).asBitmap().load(user.getPhotoUrl()).placeholder(R.drawable.user_drawer_icon_256).into(holder.profileIv);
        } catch (Exception ex) {
            ex.getMessage();
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}