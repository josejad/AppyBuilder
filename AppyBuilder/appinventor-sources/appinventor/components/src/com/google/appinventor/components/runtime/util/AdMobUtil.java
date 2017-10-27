/**
 * This file is part of AdMobPlugin
 * <p>
 * Copyright (c) 2013 Guillermo Calvo
 * <p>
 * AdMobPlugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * AdMobPlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License (http://www.gnu.org/copyleft/lesser.html)
 * for more details.
 */

// http://www.programcreek.com/java-api-examples/index.php?source_dir=admob-unity-plugin-master/EclipseProject/src/com/guillermonkey/unity/admob/AdMobUtil.java
package com.google.appinventor.components.runtime.util;

// Got it from here: http://www.programcreek.com/java-api-examples/index.php?source_dir=admob-unity-plugin-master/EclipseProject/src/com/guillermonkey/unity/admob/AdMobPlugin.java

import android.app.Activity;
import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;


public class AdMobUtil {
    private static String LOG_TAG = "AdMobUtil";
    
    static String getAndroidId(Context context) {

        String androidId;

        Log.d(LOG_TAG, "Getting Android ID...");

        androidId = Secure.getString(context.getContentResolver(), "android_id");

        Log.d(LOG_TAG, "Android ID: " + androidId);

        return (androidId);
    }

    static String getMD5(String string) throws NoSuchAlgorithmException {

        String hash = null;

        Log.d(LOG_TAG, "Getting MD5(" + string + ")...");

        if (string != null && string.length() > 0) {

            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");

            localMessageDigest.update(string.getBytes(), 0, string.length());

            hash = String.format(Locale.US, "%032X", new Object[]{new BigInteger(1, localMessageDigest.digest())});
        }

        Log.d(LOG_TAG, "MD5(" + string + "): " + hash);

        return (hash);
    }

    static String getDeviceId(String androidId) {

        String deviceId;

        Log.d(LOG_TAG, "Getting device ID(" + androidId + ")...");

        try {

            deviceId = AdMobUtil.getMD5(androidId);

        } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {

            deviceId = androidId.substring(0, 32);
        }

        Log.d(LOG_TAG, "Android ID(" + androidId + ") => " + deviceId);

        return (deviceId);
    }

    public static String guessSelfDeviceId(Context context) {

        Activity currentActivity;
        String androidId;
        String testDeviceId = null;

        Log.d(LOG_TAG, "Guessing self device id...");

        try {

//            currentActivity = UnityPlayer.currentActivity;
//            androidId = AdMobUtil.getAndroidId(currentActivity.getApplicationContext());
            androidId = AdMobUtil.getAndroidId(context);
            testDeviceId = AdMobUtil.getDeviceId(androidId);

            Log.i(LOG_TAG, "Guessed self device id: " + testDeviceId);

        } catch (Exception error) {

            Log.e(LOG_TAG, "Could not guess self device id: " + error);
        }

        return (testDeviceId);
    }
}