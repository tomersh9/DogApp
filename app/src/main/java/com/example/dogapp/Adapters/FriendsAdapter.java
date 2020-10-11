package com.example.dogapp.Adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.UserViewHolder> implements Filterable {

    private final int FRIEND_TYPE = 0;
    private final int NON_FRIEND_TYPE = 1;
    private final int MYSELF_TYPE = 2;

    private List<User> users;
    private List<User> usersFull;
    private MyUserListener listener;
    private boolean isMyFriend;
    private boolean isMe;
    private Context context;


    //location
    private Geocoder geocoder;
    private Address bestAddress;
    private List<Address> addresses;

    public interface MyUserListener {
        void onFriendClicked(int pos, View v);

        void onFriendChatClicked(int pos, View v);

        void onFriendFollowClicked(int pos, View v);

        void onFriendDeleteClicked(int pos, View v);
    }

    public void setMyUserListener(MyUserListener listener) {
        this.listener = listener;
    }

    public FriendsAdapter(List<User> users, boolean isMyFriend, boolean isMe, Context context) {
        this.users = users;
        this.isMyFriend = isMyFriend;
        this.isMe = isMe;
        this.context = context;
        usersFull = new ArrayList<>(users); //for filtering (copy of list)
        if (context != null) {
            geocoder = new Geocoder(context);
        }
    }

    //inner class
    public class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTv;
        TextView typeTv;
        TextView ageGenderTv;
        TextView locationTv;
        ImageView profileIv;
        ImageButton followBtn;
        ImageButton chatBtn;
        ImageView deleteBtn;
        long lastClickTime = System.currentTimeMillis();
        final long CLICK_TIME_INTERVAL = 300; //to prevent clicking fast
        long currTime;

        //constructor
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTv = itemView.findViewById(R.id.friend_cell_username);
            typeTv = itemView.findViewById(R.id.friend_cell_type);
            ageGenderTv = itemView.findViewById(R.id.friend_cell_age_gender);
            locationTv = itemView.findViewById(R.id.friend_cell_location);
            profileIv = itemView.findViewById(R.id.friend_cell_img);
            followBtn = itemView.findViewById(R.id.friend_cell_add_btn);
            chatBtn = itemView.findViewById(R.id.friend_cell_chat_btn);
            deleteBtn = itemView.findViewById(R.id.friend_cell_delete_btn);

            //on on cell click event
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        currTime = System.currentTimeMillis();
                        if (currTime - lastClickTime < CLICK_TIME_INTERVAL) {
                            return;
                        }
                        listener.onFriendClicked(getAdapterPosition(), v);
                    }
                }
            });

            if (!isMe) {

            } else if (isMe && isMyFriend) { //can chat and delete

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            currTime = System.currentTimeMillis();
                            /*System.out.println("curr = " + currTime + " last = " + lastClickTime);
                            System.out.println((currTime - lastClickTime) + "");*/
                            if (currTime - lastClickTime < CLICK_TIME_INTERVAL) {
                                return;
                            }
                            lastClickTime = currTime;
                            listener.onFriendDeleteClicked(getAdapterPosition(), v);
                        }
                    }
                });

                chatBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onFriendChatClicked(getAdapterPosition(), v);
                        }
                    }
                });

            } else { //can only follow
                followBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            long currTime = System.currentTimeMillis();
                            if (currTime - lastClickTime < CLICK_TIME_INTERVAL) {
                                return;
                            }
                            lastClickTime = currTime;
                            listener.onFriendFollowClicked(getAdapterPosition(), v);
                        }
                    }
                });
            }
        }
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (!isMe) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.myself_card_view, parent, false);
            UserViewHolder userViewHolder = new UserViewHolder(view);
            return userViewHolder;
        } else if (!isMyFriend) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card_view, parent, false);
            UserViewHolder userViewHolder = new UserViewHolder(view);
            return userViewHolder;

        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.following_card_view, parent, false);
            UserViewHolder userViewHolder = new UserViewHolder(view);
            return userViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, int position) {

        final User user = users.get(position);
        holder.usernameTv.setText(user.getFullName());
        if (user.getType()) { //walker
            holder.typeTv.setText(R.string.dog_walker);
        } else {
            holder.typeTv.setText(R.string.dog_owner);
        }

        int age = user.getAge();
        //holder.locationTv.setText(user.getLocation());

        //gender rtl
        if (user.getGender() == 0) {
            holder.ageGenderTv.setText(context.getString(R.string.male) + ", " + age);
        } else if (user.getGender() == 1) {
            holder.ageGenderTv.setText(context.getString(R.string.female) + ", " + age);
        } else {
            holder.ageGenderTv.setText(context.getString(R.string.other) + ", " + age);
        }

        holder.locationTv.setText(user.getLocation());
        /*final android.os.Handler handler = new android.os.Handler();
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    addresses = geocoder.getFromLocationName(user.getLocation(), 1);
                    if (!addresses.isEmpty()) {
                        bestAddress = addresses.get(0);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.locationTv.setText(bestAddress.getLocality() + ", " + bestAddress.getCountryName());
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.locationTv.setText(user.getLocation());
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();*/

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

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { //back thread automatically

            List<User> filteredList = new ArrayList<>(); //only filtered items

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(usersFull); //return full list if has no filter!
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim(); //the filter
                for (User user : usersFull) { //adding matching items to the filtered list
                    if (user.getFullName().toLowerCase().contains(filterPattern)) {
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
            users.addAll((List) results.values); //changing original list
            notifyDataSetChanged();
        }
    };


}