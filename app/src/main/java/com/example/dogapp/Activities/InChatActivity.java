package com.example.dogapp.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogapp.Enteties.Chat;
import com.example.dogapp.Enteties.User;
import com.example.dogapp.Adapters.MessageAdapter;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InChatActivity extends AppCompatActivity {

    //Views
    private ImageView profileIv;
    private TextView usernameTv;
    private ImageButton sendBtn;
    private EditText messageEt;
    private TextView statusTv;

    //chat messages
    private MessageAdapter messageAdapter;
    private List<Chat> chats;
    private RecyclerView recyclerView;

    //firebase
    private FirebaseUser fUser;
    private DatabaseReference databaseReference;
    private String userID; //to whom i send messages
    private ValueEventListener isSeenListener;
    String tempStemp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_chat_layout);

        //setup chat toolbar
        Toolbar toolbar = findViewById(R.id.in_chat_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //init views
        profileIv = findViewById(R.id.in_chat_profile_img);
        usernameTv = findViewById(R.id.in_chat_username);
        messageEt = findViewById(R.id.in_chat_et);
        sendBtn = findViewById(R.id.chat_send_btn);
        statusTv = findViewById(R.id.in_chat_status_tv);

        //init recyclerview
        recyclerView = findViewById(R.id.in_chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true); //show last first
        recyclerView.setLayoutManager(manager);

        //sender and receiver
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = getIntent().getStringExtra("userID");


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = messageEt.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(fUser.getUid(), userID, msg);
                    messageEt.setText("");
                }
            }
        });

        //load user conversation
        loadUserMessages();
    }

    private void loadUserMessages() {
        //firebase pull to assign views to chat page with data (Sender name and imgUrl)
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //if(snapshot.exist)
                User user = snapshot.getValue(User.class); //getting user im clicking on
                usernameTv.setText(user.getFullName());
                //set icon image of user
                try {
                    Glide.with(InChatActivity.this).load(user.getPhotoUri()).placeholder(R.drawable.account_icon).into(profileIv);
                } catch (Exception ex) {

                }
                //reading messages with the Sender details to populate
                readMessages(fUser.getUid(), userID, user.getPhotoUri());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //update if user seen message
        seenMessage(userID);
    }

    private void readMessages(final String myID, final String userID, final String imgUrl) {

        chats = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    chats.clear(); //clear first, then load

                    for (DataSnapshot ds : snapshot.getChildren()) {

                        Chat chat = ds.getValue(Chat.class);
                        //cover all logical cases
                        if (chat.getReceiver().equals(myID) && chat.getSender().equals(userID)
                                || chat.getReceiver().equals(userID) && chat.getSender().equals(myID)) {
                            chats.add(chat); //add the message to chat
                        }
                    }
                    messageAdapter = new MessageAdapter(chats, imgUrl); //create new adapter with loaded list
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage(final String userID) {
        databaseReference = FirebaseDatabase.getInstance().getReference("chats");
        isSeenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);

                    //if i (fUser) seen the message and the other user (sender) sent me, so iv'e seen it
                    if (chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userID)) {
                        Map<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", "true");
                        ds.getRef().updateChildren(hashMap); //update children with field
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //collect data to send and push to the database in table "chats"
    private void sendMessage(String sender, String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        //is seen default to false
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isSeen", "false");
        reference.child("chats").push().setValue(hashMap);

        //add user to the ChatFragment (new table of "chatList")
        final DatabaseReference senderChatRef = FirebaseDatabase.getInstance().getReference("chatList")
                .child(fUser.getUid())
                .child(userID);
        senderChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {

                    String timeStamp = String.valueOf(System.currentTimeMillis());
                    senderChatRef.child("id").setValue(userID); //putting a uid field to whom i send once
                    //senderChatRef.child("timeStamp").setValue(timeStamp);
                }

                databaseReference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());

                String timeStamp = String.valueOf(System.currentTimeMillis());

                tempStemp = timeStamp;

                databaseReference.child("timeStamp").setValue(timeStamp).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //Toast.makeText(getActivity(), "YESSSS", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //create chat list for the receiver
        final DatabaseReference receiverChatRef = FirebaseDatabase.getInstance().getReference("chatList")
                .child(userID)
                .child(fUser.getUid());
        receiverChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    receiverChatRef.child("id").setValue(fUser.getUid()); //putting a uid field to whom i send once
                }

                databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userID);

                //String timeStamp = String.valueOf(System.currentTimeMillis());

                databaseReference.child("timeStamp").setValue(tempStemp).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //Toast.makeText(getActivity(), "YESSSS", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus(getString(R.string.online));
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(isSeenListener);
        //setUserStatus(getString(R.string.offline));
    }

    private void setUserStatus(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        databaseReference.updateChildren(hashMap);
    }
}
