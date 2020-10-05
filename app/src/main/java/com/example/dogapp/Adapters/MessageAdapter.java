package com.example.dogapp.Adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.Chat;
import com.example.dogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ChatViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private List<Chat> chats;
    private String imgUrl;

    FirebaseUser fUser;

    public MessageAdapter(List<Chat> chats, String imgUrl) {
        this.chats = chats;
        this.imgUrl = imgUrl;
    }

    //holds inflated views
    public class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView messageTv;
        ImageView userProfileIv;
        ImageView isSeenIv;
        TextView timeTv;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            //generics views (can be changes to right or left)
            messageTv = itemView.findViewById(R.id.chat_message);
            userProfileIv = itemView.findViewById(R.id.chat_item_profile);
            isSeenIv = itemView.findViewById(R.id.chat_seen_iv);
            timeTv = itemView.findViewById(R.id.chat_item_time_tv);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Sender
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new ChatViewHolder(view);
        } else { //Receiver
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new ChatViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        Chat chat = chats.get(position);

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(chat.getTimeStamp()));
        String chatTime = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();

        holder.timeTv.setText(chatTime);

        holder.messageTv.setText(chat.getMessage());
        try {
            Glide.with(holder.itemView).asBitmap().load(imgUrl).placeholder(R.drawable.user_drawer_icon_64).into(holder.userProfileIv);
        } catch (Exception ex) {

        }

        //is seen indicator
        if (chat.getIsSeen().equals("true")) {
            holder.isSeenIv.setImageResource(R.drawable.blue_vv);
        } else {
            holder.isSeenIv.setImageResource(R.drawable.grey_vv);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    //to define between sender and receiver
    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chats.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT; //I'm sending
        } else {
            return MSG_TYPE_LEFT; //I'm receiving
        }
    }
}
