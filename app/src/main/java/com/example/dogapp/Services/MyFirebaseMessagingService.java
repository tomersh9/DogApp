package com.example.dogapp.Services;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.dogapp.Activities.InChatActivity;
import com.example.dogapp.Activities.MainActivity;
import com.example.dogapp.Fragments.HomeFragment;
import com.example.dogapp.Fragments.ProfileFragment;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final int NOTIF_ID = 1;
    private final int NOTIF_ID1 = 2;
    private final int NOTIF_ID2 = 3;
    private final int NOTIF_ID3 = 4;
    private final int NOTIF_ID4 = 5;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        /*FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    Toast.makeText(MyFirebaseMessagingService.this, token.toString(), Toast.LENGTH_SHORT).show();
                    sendRegistrationToServer(token);
                } else {
                    Toast.makeText(MyFirebaseMessagingService.this, "NO TOKEN", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    private void sendRegistrationToServer(String token) {

        /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("token", token);

        if (firebaseUser != null) {
            reference.child(firebaseUser.getUid()).updateChildren(hashMap);
        }*/
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        if (remoteMessage.getData().size() > 0) {

            Map<String, String> data = remoteMessage.getData();
            String msg = data.get("message");
            String from = data.get("fullName");
            String uid = data.get("uID");
            String imgURL = data.get("imgURL");
            String isLike = data.get("isLike");
            String isCom = data.get("isCom");
            String isMsg = data.get("isMsg");
            String isFollow = data.get("isFollow");
            String nameComment = data.get("nameComment");
            String isComFriend = data.get("isComFriend");


            //when click notification, get to chat activity
            Intent chatIntent = new Intent(MyFirebaseMessagingService.this, InChatActivity.class);
            chatIntent.putExtra("userID", uid);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 0, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //for likes and comments
            Intent mainIntent = new Intent(MyFirebaseMessagingService.this, MainActivity.class);
            PendingIntent mainPendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 1, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //for following notifications
            Intent followIntent = new Intent(MyFirebaseMessagingService.this, MainActivity.class);
            followIntent.putExtra("pendingUserID", uid);
            followIntent.putExtra("pendingImgURL", imgURL);
            PendingIntent followPendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 2, followIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // when the user is OUT OF THE APP!!!!
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ID");
            builder.setContentText(msg)
                    .setSmallIcon(R.drawable.ic_chat_black_24dp)
                    .setContentTitle(getString(R.string.new_msg_from) + " - " + from)
                    .setContentIntent(pendingIntent);

            NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, "ID");
            builder1.setContentText(msg)
                    .setSmallIcon(R.drawable.ic_explore_black_24dp)
                    .setContentTitle(from + " " + getString(R.string.commented_on_your_post))
                    .setContentIntent(mainPendingIntent);

            NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, "ID");
            builder2
                    .setSmallIcon(R.drawable.ic_baseline_thumb_up_black_24)
                    .setContentTitle(from + " " + getString(R.string.liked_your_post))
                    .setContentIntent(mainPendingIntent);

            NotificationCompat.Builder builder3 = new NotificationCompat.Builder(this, "ID");
            builder3.setContentText(msg)
                    .setSmallIcon(R.drawable.ic_add_black_24dp)
                    .setContentTitle(from + " " + getString(R.string.also_commented_on) + " " + getString(R.string.a_post_you_comm))
                    .setContentIntent(mainPendingIntent);

            NotificationCompat.Builder builder4 = new NotificationCompat.Builder(this, "ID");
            builder4.setSmallIcon(R.drawable.ic_person_add_black_24dp)
                    .setContentTitle(from + " " + getString(R.string.started_following_you))
                    .setContentIntent(followPendingIntent);

            if (!checkApp()) {
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (isCom != null) {
                    manager.notify(NOTIF_ID, builder1.build());
                } else if (isComFriend != null) {
                    manager.notify(NOTIF_ID4, builder3.build());
                } else if (isMsg != null)
                    manager.notify(NOTIF_ID1, builder.build());
                else if (isLike != null) {
                    manager.notify(NOTIF_ID3, builder2.build());
                } else if (isFollow != null) {
                    manager.notify(NOTIF_ID2, builder4.build());
                }

            }
//            if (!checkApp()) {
//                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                //manager.notify(NOTIF_ID, builder.build());
//                manager.notify(NOTIF_ID,builder1.build());
//            }

            //When user is in the application (broadcast receiver)
            if (isMsg != null && !uid.equals(FirebaseAuth.getInstance().getUid())) {
                Intent intent = new Intent("action_msg_receive");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }


        if (remoteMessage.getNotification() != null) {


        }

    }

    public boolean checkApp() {
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);

        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equalsIgnoreCase("com.example.dogapp")) {
            return true;
        } else {
            return false;
        }
    }

}