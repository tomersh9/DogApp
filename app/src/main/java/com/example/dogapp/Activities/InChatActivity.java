package com.example.dogapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private String tempStamp;

    //server
    private final String SERVER_KEY = "AAAAsSPUwiM:APA91bF5T2kokP05wtjBjEwMiUXAuB9OXF4cCSgqf4HV9ST1kzKuD9w3ncboYoGTZxMQbBSv0EocqTcycHE4gGzFDDeGIYkyLolsd3W1gY1ZPu5qCHjpNAh-H3g0Y-JvNUIZ1iOm8uOW";
    private final String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    private String hisToken;

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
                Intent intent = new Intent(InChatActivity.this, MainActivity.class);
                startActivity(intent);
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
                    sendNotification(msg);
                    messageEt.setText("");
                }
            }
        });

        //load user conversation
        loadUserMessages();
    }

    private void sendNotification(final String msg) {

        //getting the user's token
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens");
        tokenRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    hisToken = snapshot.child("token").getValue(String.class);
                    sendToToken(msg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InChatActivity.this, "NO TOKEN FOUND!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendToToken(String msg) {

        //setting data with JSON objects to get the children
        final JSONObject rootJson = new JSONObject(); //we put here "data" and "to"
        final JSONObject dataJson = new JSONObject();

        try {
            if (hisToken != null) {

                dataJson.put("message", msg);
                dataJson.put("fullName", fUser.getDisplayName());
                dataJson.put("uID", fUser.getUid());
                rootJson.put("to", hisToken);
                rootJson.put("data", dataJson);

            } else {
                return; //no token found
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        //create POST request
        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, BASE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) { //POST REQUEST class implementation

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + SERVER_KEY);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return rootJson.toString().getBytes(); //return the root object with data inside
            }
        };

        //sending the actual request
        RequestQueue queue = Volley.newRequestQueue(InChatActivity.this);
        queue.add(stringRequest);
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
    private void sendMessage(String sender, String receiver, final String message) {

        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        //is seen default to false
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("time", currentTime);
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

                tempStamp = timeStamp;

                databaseReference.child("timeStamp").setValue(timeStamp).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

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

                databaseReference.child("timeStamp").setValue(tempStamp).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

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
        setOtherUserStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(isSeenListener);
    }

    private void setOtherUserStatus() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    statusTv.setText(user.getStatus());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUserStatus(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        databaseReference.updateChildren(hashMap);
    }
}
