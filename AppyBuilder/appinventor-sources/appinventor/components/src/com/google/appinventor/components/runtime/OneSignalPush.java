package com.google.appinventor.components.runtime;

import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;
import org.json.JSONObject;
// http://romannurik.github.io/AndroidAssetStudio/icons-notification.html#source.type=image&source.space.trim=1&source.space.pad=0&name=ic_stat_onesignal_default
// https://documentation.onesignal.com/docs/customize-notification-icons
// https://documentation.onesignal.com/docs/android-sdk-setup
// https://onesignal.com/apps/ec0af76c-a131-420e-97e8-fff215e7b9e7/notifications/3dda4b90-2adc-48e8-8091-642f68a27ebf
// https://console.firebase.google.com/project/onesignal-e7ed6/settings/general/
// for permissions: https://github.com/OneSignal/OneSignal-Android-SDK/blob/master/Examples/Eclipse/OneSignalExample/AndroidManifest.xml
// https://github.com/OneSignal/OneSignal-Android-SDK/blob/master/OneSignalSDK/onesignal/src/main/java/com/onesignal/PushRegistratorGPS.java

@DesignerComponent(category=ComponentCategory.ADVANCED,
        description="change this xxxxxxxxxxxxxxxxxxxxxxxx Non-visible component that provides push notification using the OneSignal service. Please refer to the <a href=\"http://onesignal.com/\">OneSignal</a> for more information.",
        iconName="images/onesignal.png", nonVisible=true, version=1)
@SimpleObject
@UsesLibraries(libraries="google-play-services.jar,onesignal.jar")
@UsesPermissions(permissionNames = "com.google.android.c2dm.permission.RECEIVE, android.permission.WAKE_LOCK, android.permission.VIBRATE, android.permission.ACCESS_NETWORK_STATE, android.permission.RECEIVE_BOOT_COMPLETED, com.sec.android.provider.badge.permission.READ, com.sec.android.provider.badge.permission.WRITE, com.htc.launcher.permission.READ_SETTINGS, com.htc.launcher.permission.UPDATE_SHORTCUT, com.sonyericsson.home.permission.BROADCAST_BADGE, com.sonymobile.home.permission.PROVIDER_INSERT_BADGE, com.anddoes.launcher.permission.UPDATE_COUNT, com.majeur.launcher.permission.UPDATE_BADGE, com.huawei.android.launcher.permission.CHANGE_BADGE, com.huawei.android.launcher.permission.READ_SETTINGS, com.huawei.android.launcher.permission.WRITE_SETTINGS")
public class OneSignalPush extends AndroidNonvisibleComponent
        implements Component
{

    private final ComponentContainer container;

    // https://github.com/OneSignal/OneSignal-Android-SDK/blob/master/Examples/Eclipse/OneSignalExample/src/com/onesignal/example/ExampleApplication.java
   // https://documentation.onesignal.com/v3.0/docs/android-sdk-setup
    public OneSignalPush(ComponentContainer container)
    {
        super(container.$form());
        this.container = container;
        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.WARN);


        OneSignal.startInit(container.$context())
                .autoPromptLocation(false) // default call promptLocation later
                .setNotificationReceivedHandler(new ExampleNotificationReceivedHandler())
//                .setNotificationOpenedHandler(new ExampleNotificationOpenedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
//                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    @DesignerProperty(editorType= PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "OneSignal App ID")
    @SimpleProperty(userVisible=false)
    public void AppId(String appId)
    {
//        OneSignal.init(container.$context(), null, appId, NotificationOpenedHandler, NotificationReceivedHandler)

        //todo: pass this app id into android manifest using compiler
    }

    private class ExampleNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            JSONObject data = notification.payload.additionalData;
            String notificationID = notification.payload.notificationID;
            String title = notification.payload.title;
            String body = notification.payload.body;
            String smallIcon = notification.payload.smallIcon;
            String largeIcon = notification.payload.largeIcon;
            String bigPicture = notification.payload.bigPicture;
            String smallIconAccentColor = notification.payload.smallIconAccentColor;
            String sound = notification.payload.sound;
            String ledColor = notification.payload.ledColor;
            int lockScreenVisibility = notification.payload.lockScreenVisibility;
            String groupKey = notification.payload.groupKey;
            String groupMessage = notification.payload.groupMessage;
            String fromProjectNumber = notification.payload.fromProjectNumber;
            //BackgroundImageLayout backgroundImageLayout = notification.payload.backgroundImageLayout;
            String rawPayload = notification.payload.rawPayload;

            String customKey;

            Log.d("OneSignalPush", "NotificationID received: " + notificationID);

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.d("OneSignalPush", "customkey set with value: " + customKey);
            }
        }
    }

//    private class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
//        // This fires when a notification is opened by tapping on it.
//        @Override
//        public void notificationOpened(OSNotificationOpenResult result) {
//            OSNotificationAction.ActionType actionType = result.action.type;
//            JSONObject data = result.notification.payload.additionalData;
//            String launchUrl = result.notification.payload.launchURL; // update docs launchUrl
//
//            String customKey;
//            String openURL = null;
//            Object activityToLaunch = MainActivity.class;
//
//            if (data != null) {
//                customKey = data.optString("customkey", null);
//                openURL = data.optString("openURL", null);
//
//                if (customKey != null)
//                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
//
//                if (openURL != null)
//                    Log.i("OneSignalExample", "openURL to webview with URL value: " + openURL);
//            }
//
//            if (actionType == OSNotificationAction.ActionType.ActionTaken) {
//                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);
//                if (result.action.actionID.equals("id1")) {
//                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
//                    activityToLaunch = GreenActivity.class;
//                } else {
//                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
//                }
//
//            }
//            // The following can be used to open an Activity of your choice.
//            // Replace - getApplicationContext() - with any Android Context.
//            // Intent intent = new Intent(getApplicationContext(), YourActivity.class);
//            Intent intent = new Intent(container.$context(). getApplicationContext(), (Class<?>) activityToLaunch);
//            // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("openURL", openURL);
//            Log.i("OneSignalExample", "openURL = " + openURL);
//            // startActivity(intent);
//             container.$form().startActivity(intent);
//
//            // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
//            //   if you are calling startActivity above.
//     /*
//        <application ...>
//          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
//        </application>
//     */
//
//
//
//
//        }
//    }
}
