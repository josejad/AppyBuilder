// See commit I6eda0a2a7ad95e3257cb820acb6609e76fc4c124 for adding annotaions

package com.google.appinventor.components.runtime;
// BroadcastReceiver: http://codetheory.in/android-broadcast-receivers/
//http://codetheory.in/android-intent-filters/
// services: http://codetheory.in/?s=service
// ** Firebase background service code: https://gist.github.com/vikrum/6170193
// Use Google Cloud Platform Live?? https://www.youtube.com/embed/aCK_1sq6Dl0?start=15414
// search: https://www.google.com/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=android%20firebase%20service
//     https://www.firebase.com/blog/2015-10-01-firebase-android-app-engine-tutorial.html

// http://www.vogella.com/tutorials/AndroidServices/article.html
// http://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
// See this: https://www.simplifiedcoding.net/firebase-cloud-messaging-tutorial-android/


import android.app.*;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.TextViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// https://github.com/firebase/quickstart-android/tree/master/messaging

@DesignerComponent(version = YaVersion.PUSH_NOTIFICATION_COMPONENT_VERSION,
        description = "This is a messaging solution that lets you reliably deliver messages to all client apps. " +
                "Using this component, you can notify a client app that new email or other data is available to sync. " +
                "You can send notification messages to drive user reengagement and retention.",
        category = ComponentCategory.ADVANCED,
        nonVisible = true,
        iconName = "images/push.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.RECEIVE_BOOT_COMPLETED")
@UsesLibraries(libraries = "firebase.jar")
@SimpleService(
        className = "com.google.appinventor.components.runtime.util.PushNotificationBroadcastReceiver",
        actions = "android.intent.action.BOOT_COMPLETED,android.intent.action.QUICKBOOT_POWERON")
public class PushNotification extends AndroidNonvisibleComponent implements Component {
    private static final String LOG_TAG = "PushNotification";
    public static final String TOPIC_PATH = "TOPIC_PATH";
    public static final String DATABASE_URL = "DATABASE_URL";
//    public static final String PUSH_TITLE = "PUSH_TITLE";

    private final Activity activity;
    private String pushTitle = "Notification";
//    private String MY_FIREBASE_URL = "https://Amerkashi.firebaseio.com/";
    private String MY_FIREBASE_URL = "https://appybuilder-5762b.firebaseio.com/";
    private String listenToPushUrl;
    private String listenToPushPath;
    private int vCode=1;

    /**
     * Creates a new PushNotification component.
     *
     * @param container the Form that this component is contained in.
     */
    public PushNotification(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
        listenToPushPath = ""; // given a dynamic default value in the Designer
        listenToPushUrl = MY_FIREBASE_URL;
        FirebaseURL(listenToPushUrl);
//        VersionCode(vCode);
//        container.$form().registerForOnPause(this);

        // Start the background Firebase activity
    }


    //todo: Should I somehow pass the firebase url with bucket information?
    // how will different users use different buckets for unique pushNotif id???
    @SimpleFunction(description = "Starts subscribing (listening) to the topic in the database")
    public boolean StartSubscription() {

        // implicit referencing; not recommended
//        Intent intentService = new Intent(PushNotificationService.class.getName());
//        Log.i(LOG_TAG, "Trying to pass TopicPath as:" + TopicPath());

        //using explicit referencing
        Intent intentService = new Intent(this.activity, PushNotificationService.class);

        //We need to restart the service IF running
        stopService();
//        boolean isStopped = this.activity.stopService(intentService);


        // Pass the push path and title. The PushPath is basically a display-only entry that is:
        //      userEmail + projectName (+ version code - to be added)
        // PushTitle is user-editable proeprty
        intentService.putExtra(DATABASE_URL, FirebaseURL());
        intentService.putExtra(TOPIC_PATH, TopicPath());

//        intentService.putExtra(PUSH_TITLE, PushTitle());

        ContextWrapper contextWrapper = new ContextWrapper(this.activity.getBaseContext());
        contextWrapper.startService(intentService);

        //Now start the service
//        this.activity.startService(intentService);
        return isServiceRunning();
    }

	private boolean stopService() {
        boolean isStopped = false;
        if (isServiceRunning()) {
            Intent serviceIntent = new Intent(this.activity, PushNotificationService.class);
            ContextWrapper contextWrapper = new ContextWrapper(this.activity.getBaseContext());
            isStopped = contextWrapper.stopService(serviceIntent);
        }
        Log.i(LOG_TAG, "Is service now stopped?" + isStopped);
        return isStopped;
	}

	@SimpleFunction(description = "Returns true if app is listening to topics, else false")
    public boolean IsListening() {
		return isServiceRunning();
	}

 /*   @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Indicates if device will be listening to topic, " +
            "even after a device reboot")
    public boolean ListenOnReboot() {
        return listenOnReboot;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Use this to indicate if you want to listen to topics even after a device reboot!")
    public void ListenOnReboot(boolean listenOnReboot) {
        this.listenOnReboot = listenOnReboot;
    }
*/
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(service.service.getClassName().toLowerCase().contains("pushnotificationservice")) {
                Log.d(LOG_TAG, "Running service name:" + service.service.getClassName());
                return true;
            }
        }
        return false;
    }

    @SimpleFunction(description = "Stops subscription (listening) to the topic")
    public boolean StopSubscription() {

        //using explicit referencing
        Intent intentService = new Intent(this.activity, PushNotificationService.class);

        //We need to restart the service IF running
        boolean isStopped = this.activity.stopService(intentService);
        Log.d(LOG_TAG, "Service stopped?" + isStopped);
        return isStopped;
    }

    /**
     * Getter for the PushPath.
     *
     * @return the PushPath for this Notification
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Gets the TopicPath for this PushNotification.")
    public String TopicPath() {
        return listenToPushPath;
    }

    /**
     * Specifies the path for the project bucket of the Firebase.
     *
     * @param topicPath the name of the project's bucket
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA, defaultValue = "")
//    @SimpleProperty(description = "The unique path for your Push Notification. NOTE: CAN NOT BE EDITED!")
    public void TopicPath(String topicPath) {
        if (!this.listenToPushPath.equals(topicPath)) {
            this.listenToPushPath = topicPath;
        }
        Log.i(LOG_TAG, "topicPath was set as: " + topicPath);
    }

 /*   @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "1")
    public int VersionCode() {
        // we get this from form so that we can use it for our push notification
      return form.VersionCode();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "1")
    @SimpleProperty(userVisible = false,
            description = "An integer value which must be incremented each time a new Android "
                    +  "Application Package File (APK) is created for the Google Play Store.")
    public void VersionCode(int vCode) {
        // We don't actually need to do anything.
        // This vCode will be used by PushNotification
        this.vCode = vCode;
    }
*/

    /**
     * Getter for the Firebase URL.
     *
     * @return the URL for this Firebase
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Gets the URL for this Push DB.")
    public String FirebaseURL() {
        return listenToPushUrl;
    }

    /**
     * Specifies the URL of the firebase.
     * The default value is currently my private firebase url //TODO: this should be changed
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA,
            defaultValue = "https://appybuilder-5762b.firebaseio.com/")
    @SimpleProperty(description = "Returns the URL for this FirebaseDB.")
    public void FirebaseURL(String url) {
        // Firebase requires slash at the end; validate and fix if needed
        url = url.trim();
        url = url.endsWith("/") ? url : url + "/";
        listenToPushUrl = url;
        Log.i(LOG_TAG, "FirebaseURL was set as: " + listenToPushUrl);

    }

//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXT, defaultValue = "Notification")
//    @SimpleProperty(description = "Sets the title to be displayed in the notification bar")
//    public void NotificationTitle(String pushTitle) {
//        this.pushTitle = pushTitle;
//    }
//
//    @SimpleProperty(description = "Gets the current Push Notification Title")
//    public String PushTitle() {
//        return this.pushTitle;
//    }

//    @Override
//    public void onPause() {
////        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
////        isReceiverRegistered = false;
//        super.onPause();
//    }

    // ==========. Make sure you use static, otherwise, runtime will throw exception because this is inner class
    public static class PushNotificationService extends Service {
        private static final String LOG_TAG = "PushNotificationService";
        private SharedPreferences sharedPreferences;
        private String LOCAL_DEVICE_DB_NAME = "";
        private Context context;
        private String pushTitle = "";
        private String DEFAULT_TITLE = "Default title";
        private String TOPIC_PATH = "TOPIC_PATH";
        private String LISTEN_AFTER_REBOOT = "LISTEN_AFTER_REBOOT";
        private String PUSH_TITLE = "PUSH_TITLE";
        private static final String CURRENT_PUSHES = "CURRENT_PUSHES";
        private ChildEventListener childListener;
        private NotificationManager mNotificationManager;
        private Firebase myFirebase;
//        private int vCode = 0;
        private String ROOT_PATH = ""; //"_topic_";
        private String servicePushPath = "";
        private String servicePushUrl = "";
//        private String listenAfterReboot = "";

//        public PushNotificationService() {
//            Log.d(LOG_TAG, "PushNotificationService 0 arg constructor called" );
//        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        // called when the Service object is instantiated (ie: when the service is created).
        // You should do things in this method that you need to do only once (ie: initialize some
        // variables, etc.). onCreate() will only ever be called once per instantiated object.
        @Override
        public void onCreate() {
            context = getApplicationContext();

            // Note: don't do anything here. logic will be in onStartCommand
        }

        @Override
        public void onStart(Intent intent, int startId) {
            Log.d(LOG_TAG, "onStart Started");
        }

        // called every time a client starts the service using startService(Intent intent).
        // This means that onStartCommand() can get called multiple times. You should do the things in this
        // method that are needed each time a client requests something from your service. This depends a lot on
        // what your service does and how it communicates with the clients (and vice-versa).
        // If you don't implement onStartCommand() then you won't be able to get any information from the
        // Intent that the client passes to onStartCommand() and your service might not be able to do any useful work.
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // Let it continue running until it is stopped.
            Log.d(LOG_TAG, "onStartCommand Started");

            // setup shared preferences
            ApplicationInfo applicationInfo = getApplicationInfo();
//            Log.d(LOG_TAG, "applicationInfo,packageName:" + applicationInfo.packageName);
//            Log.d(LOG_TAG, "applicationInfo.dataDir:" + applicationInfo.dataDir);
//            Log.d(LOG_TAG, "applicationInfo.icon:" + applicationInfo.icon);

            PackageManager manager = context.getPackageManager();
            PackageInfo packageInfo = null;
            try {
                packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
                //This version code isn't correct because it'll give the companion vcode
//                vCode = packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
//                vCode = 0;
            }

//            Log.d(LOG_TAG, "vCode:" + vCode);

            LOCAL_DEVICE_DB_NAME = applicationInfo.packageName;// + vCode;
            Log.d(LOG_TAG, "LOCAL_DEVICE_DB_NAME:" + LOCAL_DEVICE_DB_NAME);

            sharedPreferences = context.getSharedPreferences(LOCAL_DEVICE_DB_NAME, Context.MODE_PRIVATE);

            if (intent != null) {
                // this happens on 1st call when activity starts up this service
                Log.d(LOG_TAG, "intent was not null, onStartCommand: intent was not null. Setting up data");
                Bundle extras = intent.getExtras();
                if (extras != null && extras.containsKey(PushNotification.TOPIC_PATH)) {
                    servicePushPath = intent.getStringExtra(PushNotification.TOPIC_PATH);
                    servicePushUrl = intent.getStringExtra(PushNotification.DATABASE_URL);
//                    Log.d(LOG_TAG, String.format("onStartCommand: pushUrl (%s), path (%s), listenOnReboot (%s)"
//                            , servicePushUrl, servicePushPath, (listenOnReboot+"").toLowerCase()));

                    storeValue(TOPIC_PATH, servicePushPath);
                    storeValue(DATABASE_URL, servicePushUrl);
//                    storeValue(LISTEN_AFTER_REBOOT, (listenOnReboot+"").toLowerCase());

//            Log.d(LOG_TAG, "storage for servicePushPath:" + ROOT_PATH + getValue(servicePushPath, ""));

//            pushTitle = intent.getStringExtra(PushNotification.PUSH_TITLE);
//            pushTitle = pushTitle == null || pushTitle.trim().equals("") ? DEFAULT_TITLE : pushTitle;
//            storeValue(PUSH_TITLE, pushTitle);
                } else {
                    // Got intent, but extra didn't exist. This may be situation on device reboot
                    getDataFromDb();
                }

//            Log.d(LOG_TAG, "storage for title:" + getValue(pushTitle, ""));
            } else {
                Log.d(LOG_TAG, "intent  WAS null");
                getDataFromDb();
            }

//            if (TextUtils.equals(listenAfterReboot.toLowerCase(), "true")) {
                setupFirebase();
                startListening();
//            }

            return START_STICKY;
        }
// http://www.vogella.com/tutorials/android.html
        private void setupFirebase() {

            String isInitialized = (String) getValue("isInitialized", "false");
            Log.d(LOG_TAG, "onCreate: is DB initialized: " + isInitialized);
            if (isInitialized.equals("false")) {
                Log.d(LOG_TAG, "Initializing local device db");
                storeValue("isInitialized", "true");
            }

            Firebase.setAndroidContext(context);

            childListener = new ChildEventListener() {
                // Retrieve new posts as they are added to the Firebase.
                @Override
                public void onChildAdded(final DataSnapshot snapshot, String previousChildKey) {
                    postNotif(snapshot.getValue().toString());
                }

                @Override
                public void onCancelled(final FirebaseError error) {
                    //
                }

                @Override
                public void onChildChanged(final DataSnapshot snapshot, String previousChildKey) {
                    postNotif(snapshot.getValue().toString());
                }

                /*
                public void onChildChanged(final DataSnapshot snapshot, String previousChildKey) {
                                androidUIHandler.post(new Runnable() {
                                    public void run() {
                                        // Signal an event to indicate that the child data was changed.
                                        // We post this to run in the Application's main UI thread.
                                        DataChanged(snapshot.getKey(), snapshot.getValue());
                                    }
                                });
                            }
                 */
                @Override
                public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {
                    // check this to see how to find out number of connected users:
                    // https://www.firebase.com/blog/2013-06-17-howto-build-a-presence-system.html
                }

                @Override
                public void onChildRemoved(final DataSnapshot snapshot) {
                    //
                }
            };
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(LOG_TAG, "Service Destroyed");
        }


        private void storeValue(final String tag, final String valueToStore) {
            final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
            try {
                sharedPrefsEditor.putString(tag, valueToStore);
                sharedPrefsEditor.commit();
            } catch (Exception e) {
                Log.d(LOG_TAG, "error is:" + e.getMessage());
            }
        }

        private Object getValue(final String tag, final String valueIfTagNotThere) {
            String value = "not Found";
            try {
                value = sharedPreferences.getString(tag, "");
                // If there's no entry with tag as a key then return the empty string.
                //    was  return (value.length() == 0) ? "" : JsonUtil.getObjectFromJson(value);
                return (value.length() == 0) ? valueIfTagNotThere : value;
            } catch (Exception e) {
                value = valueIfTagNotThere;
                Log.d(LOG_TAG, "error is:" + e.getMessage());
            }

            return value;
        }

        private void getDataFromDb() {
            // Get data from sharedpreferences
            Log.d(LOG_TAG, "setupDataAndConnect: intent WAS null. ");
            servicePushPath = (String) getValue(TOPIC_PATH, "");
            servicePushUrl = (String) getValue(DATABASE_URL, "");
//            listenAfterReboot = (String) getValue(LISTEN_AFTER_REBOOT, "false");

//                pushTitle = (String) getValue(PUSH_TITLE, "");
        }

        private void startListening() {
            if (SdkLevel.getLevel() < SdkLevel.LEVEL_GINGERBREAD_MR1) {
                Log.d(LOG_TAG, "The version of Android on this device is too old to use Firebase.");
                return;
            }
            // Now listen to db
            // Here we need ROOT_PATH because we didn't want to write the ROOT to our DB
            Log.d(LOG_TAG, "Firebase: listening to: " + servicePushUrl + ROOT_PATH + servicePushPath);

            myFirebase = new Firebase(servicePushUrl + ROOT_PATH + servicePushPath);
            myFirebase.addChildEventListener(childListener);
        }

        public Object getTags() {
            List<String> keyList = new ArrayList<String>();
            Map<String, ?> keyValues = sharedPreferences.getAll();
            // here is the simple way to get keys
            keyList.addAll(keyValues.keySet());
            java.util.Collections.sort(keyList);
            return keyList;
        }

        private void postNotif(String notifMsg) {
            String msg[] = notifMsg.split("=");
            pushTitle = "Notification"; //a default notification title
            if (msg.length > 1) {
                pushTitle = msg[0];
                notifMsg = msg[1];
            }
            Log.d(LOG_TAG, "postNotif: Received title,message of:[" + pushTitle + "]    [" + notifMsg + "]");
            notifMsg = notifMsg.trim();

            // Check to see if this message was already displayed
            String csvPushMsgs = (String) getValue(CURRENT_PUSHES, "_stub_");
            for (String aMessage : csvPushMsgs.split(",")) {
                if (aMessage.toLowerCase().equals(notifMsg.toLowerCase())) {
                    Log.d(LOG_TAG, "message already displayed; won't display this:" + notifMsg);
                    return;
                }
            }

            Log.d(LOG_TAG, "Notification should display this:" + notifMsg);

            // Message hasn't been displayed before. Add it to top of list, store it and then display
            // this newNotification in notification bar
            csvPushMsgs = notifMsg + "," + csvPushMsgs;
            storeValue(CURRENT_PUSHES, csvPushMsgs);

            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int icon = getApplicationInfo().icon;
//            Notification notification = new Notification(icon, "Firebase" + Math.random(), System.currentTimeMillis());
            Notification notification = new Notification();
            notification.icon = icon;

            // http://stackoverflow.com/questions/10308710/opening-activity-after-clicking-push-notification-android

            //adding LED lights to notification
            notification.defaults |= Notification.DEFAULT_LIGHTS;

            //add sound and hide notif after its selected
		    notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;

            // http://docs.pushwoosh.com/docs/android-faq#custom-intent-receiver
            //Get default launcher intent for clarity
            Intent notificationIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            notificationIntent.addCategory("android.intent.category.LAUNCHER");
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);


//        CharSequence contentTitle = "Background" + Math.random();
//        Intent notificationIntent = new Intent(context, PushNotification.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            notification.setLatestEventInfo(context, pushTitle, notifMsg, contentIntent);
            mNotificationManager.notify(1, notification);

        }
    }
}
