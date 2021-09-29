package com.group_7.mhd.managerapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Helper.NotificationHelper;
import com.group_7.mhd.managerapp.MainActivity;
import com.group_7.mhd.managerapp.MainActivity;
import com.group_7.mhd.managerapp.OrderStatus;
import com.group_7.mhd.managerapp.R;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNotificationAPI(remoteMessage);
            else
                sendNotification(remoteMessage);
        }
    }

    private void sendNotificationAPI(RemoteMessage remoteMessage) {

        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        //here we will fix to cliick to notification go to ordedr list
        PendingIntent pendingIntent;
        NotificationHelper helper;
        Notification.Builder builder;

        if (Common.currentUser != null){

            /*
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String title = notification.getTitle();
        String content = notification.getBody();
*/
            Intent intent = new Intent(this, OrderStatus.class);
            intent.putExtra(Common.PHONE_TEXT, Common.currentUser);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.getMHDChannelNotification(title,message,pendingIntent,defaultSoundUri);

            helper.getManager().notify(new Random().nextInt(),builder.build());

        }
        else//fix cresh if notification send from new system commpn.current user == null
        {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            helper = new NotificationHelper(this);
            builder = helper.getMHDChannelNotification(title,message,defaultSoundUri);

            helper.getManager().notify(new Random().nextInt(),builder.build());

        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
    }
}
