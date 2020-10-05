package com.example.dogapp.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.Chat;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatUsersAdapter extends RecyclerView.Adapter<ChatUsersAdapter.ChatUserViewHolder> implements Filterable {

    private List<User> users;
    private List<User> usersFull;
    private MyChatUserListener listener;
    private Context context;
    private String lastMessage;
    private String lastTime;
    private boolean isNew;

    public interface MyChatUserListener {
        void onChatUserClicked(int pos, View v);
    }

    public void setMyChatUserListener(MyChatUserListener listener) {
        this.listener = listener;
    }

    public ChatUsersAdapter(List<User> users, Context context) {
        this.users = users;
        this.context = context;
        usersFull = new ArrayList<>(users); //for filtering (copy of list)
    }

    //inner class
    public class ChatUserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTv;
        TextView lastMessageTv;
        TextView timeTv;
        ImageView profileIv;
        ImageView onlineIv, offlineIv;
        ImageView isNewIv;

        //constructor
        public ChatUserViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTv = itemView.findViewById(R.id.chat_cell_username);
            lastMessageTv = itemView.findViewById(R.id.chat_cell_last_msg);
            timeTv = itemView.findViewById(R.id.chat_cell_time);
            profileIv = itemView.findViewById(R.id.chat_cell_image);
            onlineIv = itemView.findViewById(R.id.chat_cell_status_online);
            offlineIv = itemView.findViewById(R.id.chat_cell_status_offline);
            isNewIv = itemView.findViewById(R.id.chat_cell_new_msg_icon);

            //on on cell click event
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onChatUserClicked(getAdapterPosition(), v);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ChatUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_cell_card_view, parent, false);
        ChatUserViewHolder chatUserViewHolder = new ChatUserViewHolder(view);
        return chatUserViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameTv.setText(user.getFullName());
        try {
            Glide.with(holder.itemView).asBitmap().load(user.getPhotoUrl()).placeholder(R.drawable.user_drawer_icon_256).into(holder.profileIv);
        } catch (Exception ex) {

        }

        //set status symbol
        if (user.getStatus()) {
            holder.onlineIv.setVisibility(View.VISIBLE);
            holder.offlineIv.setVisibility(View.GONE);
        } else {
            holder.onlineIv.setVisibility(View.GONE);
            holder.offlineIv.setVisibility(View.VISIBLE);
        }

        //set last message appear
        setLastMessage(user.getId(), holder.lastMessageTv, holder.timeTv, holder.isNewIv);
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

    private void setLastMessage(final String userID, final TextView lastMessageTv, final TextView timeTv, final ImageView iconIv) {
        lastMessage = "default";
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser(); //me
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
                    if (fUser != null) {

                        if (chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userID)
                                || chat.getReceiver().equals(userID) && chat.getSender().equals(fUser.getUid())) {
                            lastMessage = chat.getMessage();
                            lastTime = chat.getTime();
                        }
                        //indicate new message
                        if ((chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userID))) {

                            if (!chat.getIsSeen().equals("true")) {
                                iconIv.setVisibility(View.VISIBLE);
                                lastMessageTv.setTypeface(null, Typeface.BOLD);
                            } else {
                                iconIv.setVisibility(View.GONE);
                                lastMessageTv.setTypeface(null, Typeface.NORMAL);
                            }
                        }
                    }
                }

                if (lastMessage.equals("default")) {
                    lastMessageTv.setText(R.string.no_msgs);
                } else {
                    lastMessageTv.setText(lastMessage);
                }
                lastMessage = "default"; //back to default
                timeTv.setText(lastTime);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
