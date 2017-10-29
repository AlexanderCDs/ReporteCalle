package com.example.alexander.reportecalle;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Alexander on 28/10/2017.
 */


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "FROM : " + remoteMessage.getFrom() );

        if(remoteMessage.getData().size() > 0)
        {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
        }

        if(!remoteMessage.getNotification().equals(null))
        {
            Log.d(TAG, "Message body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }


    }

    /**
     * Display the notifiaction
     * @param body
     */
    private void sendNotification(String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.cartelescalle)
                .setContentTitle("Reporte calle")
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pIntent);

        NotificationManager nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(0, notification.build());

    }
}