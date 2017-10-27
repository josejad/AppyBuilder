package com.google.appinventor.components.runtime.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.appinventor.components.runtime.PushNotification;

@SuppressWarnings("deprecation")
public class PushNotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "PushNotificationBroadcastReceiver";
    public static final int NOTIFICATION_ID = 8001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");

        // this is implicit. Not good
//        Intent intentService = new Intent(PushNotificationService.class.getName());
//        context.startService(intentService);

        // Make it explicit

        Intent serviceIntent  = new Intent(context, PushNotification.PushNotificationService.class);
        context.startService(serviceIntent );

        /*ComponentName comp = new ComponentName(context.getPackageName(), PushNotificationService.class.getName());

        intent.setComponent(comp);
        context.startService(intent);
        setResultCode(Activity.RESULT_OK);*/

    }

}
