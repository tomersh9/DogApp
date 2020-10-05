package com.example.dogapp.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.example.dogapp.Activities.MainActivity;
import com.example.dogapp.R;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private final int NOTIFICATION_ID = 7;

    @Override
    public void onReceive(Context context, Intent intent) {
        buildNotification(context);
    }

    void buildNotification(Context context) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 9, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ID");
        builder.setContentText("ALARM mANAGER")
                .setSmallIcon(R.drawable.ic_chat_black_24dp)
                .setContentTitle("MANAGERRRR")
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());

    }
}
