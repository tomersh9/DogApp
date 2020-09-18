package com.example.dogapp.Fragments;

import androidx.fragment.app.Fragment;

public class InChatFragment extends Fragment {

    /*
    //Views
    private ImageView profileIv;
    private TextView usernameTv;
    private ImageButton sendBtn;
    private EditText messageEt;

    //chat messages
    MessageAdapter messageAdapter;
    List<Chat> chats;
    RecyclerView recyclerView;

    //firebase
    private FirebaseUser fUser;
    private DatabaseReference databaseReference;
    private String userID; //to whom i send messages

    public interface OnInChatListener {
        void changeInChatToolbar(Toolbar toolbar);
        void onBackInChat();
    }

    private OnInChatListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnInChatListener) context; //the activity is the callback
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement OnInChatListener interface");
        }
    }

    //to get data from activity
    public static InChatFragment getInstance(String userID) {
        InChatFragment chatFragment = new InChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userID", userID);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.in_chat_layout, container, false);

        //setup chat toolbar
        Toolbar toolbar = rootView.findViewById(R.id.in_chat_toolbar);
        toolbar.setTitle("");
        listener.changeInChatToolbar(toolbar); //change toolbar when fragment created

        //init views
        profileIv = rootView.findViewById(R.id.in_chat_profile_img);
        usernameTv = rootView.findViewById(R.id.in_chat_username);
        messageEt = rootView.findViewById(R.id.in_chat_et);
        sendBtn = rootView.findViewById(R.id.chat_send_btn);

        //init recyclerview
        recyclerView = rootView.findViewById(R.id.in_chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true); //show last first
        recyclerView.setLayoutManager(manager);

        //sender and receiver
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = getArguments().getString("userID");

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

        return rootView;
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
                    Glide.with(InChatFragment.this).load(user.getPhotoUri()).placeholder(R.drawable.account_icon).into(profileIv);
                } catch (Exception ex) {

                }
                //reading messages with the Sender details to populate
                readMessages(fUser.getUid(), userID, user.getPhotoUri());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //collect data to send and push to the database in table "chats"
    private void sendMessage(String sender, String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        reference.child("chats").push().setValue(hashMap);

        //add user to the ChatFragment (new table of "chatList")
        final DatabaseReference senderChatRef = FirebaseDatabase.getInstance().getReference("chatList")
                .child(fUser.getUid())
                .child(userID);
        senderChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    senderChatRef.child("id").setValue(userID); //putting a uid field to whom i send once
                }
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listener.changeInChatToolbar(null); //return original toolbar
        listener.onBackInChat();
    }

     */
}
