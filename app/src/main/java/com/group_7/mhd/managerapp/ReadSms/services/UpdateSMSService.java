package com.group_7.mhd.managerapp.ReadSms.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;


public class UpdateSMSService extends IntentService {

    public UpdateSMSService() {
        super("UpdateSMSReceiver");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        markSmsRead(intent.getLongExtra("id", -123));
    }

    public void markSmsRead(long messageId) {

        try {
            ContentValues cv = new ContentValues();
            cv.put("read", "1");
            getContentResolver().update(Uri.parse("content://sms/" + messageId), cv, null, null);
        } catch (Exception e) {

        }


    }

}
