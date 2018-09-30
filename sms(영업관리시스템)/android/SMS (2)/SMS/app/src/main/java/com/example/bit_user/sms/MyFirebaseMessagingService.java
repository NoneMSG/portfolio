package com.example.bit_user.sms;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by bit-user on 2017-09-08.
 */

/**
 * Created by bit-user on 2017-09-08.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService{

    private static final String TAG = "fcm";

    private void sendNotification(String messageBody, String title, String index) {
        SharedPreferences preference = getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        String token = preference.getString("token","");
        Intent intent = new Intent(this, ReadDailyActivity.class);
        System.out.println("!!!!!!!!!"+index);
        intent.putExtra("index",index);
        intent.putExtra("token",token);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //   super.onMessageReceived(remoteMessage);

        String msg = remoteMessage.getFrom();
        Log.d(TAG, "From:"+ msg);

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String index = remoteMessage.getData().get("index");
            //String title = remoteMessage.getData().get("title");
            //String time = remoteMessage.getData().get("time");
            String time = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+index);
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.

                sendNotification(time,title, index);
            } else {
                // Handle message within 10 seconds
                sendNotification(time,title, index);
            }

        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }else{
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

}