package com.example.dogapp.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManager manager;
    private final int NOTIF_ID = 1;

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

            Toast.makeText(this, remoteMessage.getData().get("message"), Toast.LENGTH_SHORT).show();
            System.out.println(remoteMessage.getData().get("message")+" 555555555555555555555555555");

            // when the user is OUT OF THE APP!!!!
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ID");
            builder.setContentText(remoteMessage.getData().get("message"))
                    .setSmallIcon(R.drawable.ic_explore_black_24dp)
                    .setContentTitle("New message");

            NotificationManager manager = getNotificationManager();
            manager.notify(NOTIF_ID,builder.build());

            //When user is in the application (broadcast receiver)
            Intent intent = new Intent("action_msg_receive");
            intent.putExtra("message", remoteMessage.getData().get("message"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

    }

    private NotificationManager getNotificationManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel("ID", "NAME", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
        }
        return manager;
    }

}
