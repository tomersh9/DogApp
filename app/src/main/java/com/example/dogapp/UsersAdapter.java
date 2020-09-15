package com.example.dogapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.User;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements Filterable {

    private List<User> users;
    private List<User> usersFull;
    private MyUserListener listener;

    public interface MyUserListener {
        void onUserClicked(int pos, View v);
    }

    public void setMyUserListener(MyUserListener listener) {
        this.listener = listener;
    }

    public UsersAdapter(List<User> users) {
        this.users = users;
        usersFull = new ArrayList<>(users); //for filtering (copy of list)
    }

    //inner class
    public class UserViewHolder extends RecyclerView.ViewHolder
    {
        TextView usernameTv;
        TextView typeTv;
        TextView ageGenderTv;
        TextView locationTv;
        ImageView profileIv;

        //constructor
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTv = itemView.findViewById(R.id.friend_cell_username);
            typeTv = itemView.findViewById(R.id.friend_cell_type);
            ageGenderTv = itemView.findViewById(R.id.friend_cell_age_gender);
            locationTv = itemView.findViewById(R.id.friend_cell_location);
            profileIv = itemView.findViewById(R.id.friend_cell_img);

            //on on cell click event
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null)
                    {
                        listener.onUserClicked(getAdapterPosition(),v);
                    }
                }
            });
        }
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card_view,parent,false);
        UserViewHolder userViewHolder = new UserViewHolder(view);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameTv.setText(user.getFullName());
        holder.typeTv.setText(user.getTitle());
        holder.ageGenderTv.setText(user.getGender());

        try {
            Glide.with(holder.itemView).asBitmap().load(user.getPhotoUri()).placeholder(R.drawable.account_icon).into(holder.profileIv);
        } catch (Exception ex) {

        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { //back thread automatically

            List<User> filteredList = new ArrayList<>(); //only filtered items

            System.out.println(usersFull.toString() + " !!!!!!!!!!!!!!!");
            if(constraint == null || constraint.length() == 0) {
                filteredList.addAll(usersFull); //return full list if has no filter!
            }
            else {
                String filterPattern = constraint.toString().toLowerCase().trim(); //the filter
                for (User user : usersFull) { //adding matching items to the filtered list
                    if(user.getFullName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            //assign the final filtered list to the result and return them
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override //publish results on the UI
        protected void publishResults(CharSequence constraint, FilterResults results) {
            users.clear();
            users.addAll((List)results.values); //changing original list
            notifyDataSetChanged();
        }
    };
}
