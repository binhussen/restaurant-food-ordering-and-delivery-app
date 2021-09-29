package com.group_7.mhd.managerapp.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.group_7.mhd.managerapp.R;

public class NotificationHelper extends ContextWrapper {

    private static final String MHD_CHANCEL_ID = "com.group_7.mhd.managerapp.MHD";
    private static final String MHD_CHANCEL_NAME = "MHD";

    private NotificationManager manager;

    @TargetApi(Build.VERSION_CODES.O)
    public NotificationHelper(Context base)
    {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel mhdchannel = new NotificationChannel(MHD_CHANCEL_ID,MHD_CHANCEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        mhdchannel.enableLights(false);
        mhdchannel.enableVibration(true);
        mhdchannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(mhdchannel);
    }

    public NotificationManager getManager(){
        if (manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getMHDChannelNotification(String title, String body, PendingIntent contentIntent, Uri soungUri)
    {
        return new Notification.Builder(getApplicationContext(),MHD_CHANCEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soungUri)
                .setAutoCancel(false);
    }
    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getMHDChannelNotification(String title, String body, Uri soungUri)
    {
        return new Notification.Builder(getApplicationContext(),MHD_CHANCEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soungUri)
                .setAutoCancel(false);
    }
}