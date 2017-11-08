// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;
// See/download all the google play services versions: http://www.smorgasbork.com/2015/01/05/google-play-services-sdk-version-history
// Use this to package the app and to get the google-play-service from target or from .apk
// https://github.com/googleads/googleads-mobile-android-examples/releases/tag/3.1

// Got sdk downloaded from here: http://www.smorgasbork.com/2015/01/05/google-play-services-sdk-version-history/#comment-89

import android.content.Context;
import android.graphics.Color;
import android.provider.Settings;
//import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AdMobUtil;

import java.util.Calendar;
import java.util.Date;

//https://developers.google.com/mobile-ads-sdk/docs/admob/intermediate
//https://github.com/googleads/googleads-mobile-android-examples/blob/master/admob/banner-adlistener/src/com/google/example/gms/ads/banneradlistener/BannerAdListener.java
// good examples: https://github.com/googleads/googleads-mobile-android-examples/releases/tag/3.1
@DesignerComponent(category = ComponentCategory.MONETIZE, description =
        "AdMob component allows you to monetize your app. You must have a valid AdMob account and AdUnitId " +
        "that can be obtained from http://www.google.com/AdMob . If your id is invalid, the " +
        "AdMob banner will not display on the emulator or the device." +
        "Warning: Make sure you're in test mode during development to avoid being disabled for clicking your own ads." +
                "<p>NOTE: YOU MUST SET SCREEN1 SIZING PROPERTY TO RESPONSIVE ",
        version = YaVersion.ADMOB_COMPONENT_VERSION)
@SimpleObject
@UsesLibraries(libraries = "google-play-services.jar")
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.ACCESS_NETWORK_STATE"
//        + ",android.permission.ACCESS_FINE_LOCATION,android.permission.ACCESS_COARSE_LOCATION,android.permission.ACCESS_WIFI_STATE"
    )
public final class AdMob extends AndroidViewComponent
        implements OnDestroyListener, OnResumeListener, OnPauseListener {
    private static final String LOG_TAG = "AdMob";
    public String adFailedToLoadCode;
    public String adFailedToLoadMessage;
    public String adUnitID;
    private AdView adView;
    protected final ComponentContainer container;
//    private boolean enableAdTargeting;
    private boolean isTestMode = false;
//    private boolean enableLog;
//    private boolean geoLocationEnabled;
    public Context onAdLoadedMsg;
//    private boolean pauseAAd = false;
    public int targetAge=0;
//    public String testUnitID;
    private String targetGender="ALL";
    private boolean adEnabled=true;

    private String TAG= "AdMob";
  private boolean targetForChildren = false;

    public AdMob(ComponentContainer container) {
        super(container);
        this.container = container;
        container.$form().registerForOnDestroy(this);
        container.$form().registerForOnResume(this);
        container.$form().registerForOnPause(this);
        adView = new AdView(container.$context());
        adView.setAdListener(new AdListenerPage(container.$context()));
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setBackgroundColor(Color.BLACK);
//        Width(320);  //todo: use AdSize banners ad
//        Height(50); //todo: use AdSize banners ad

        android.widget.LinearLayout.LayoutParams localLayoutParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,      //Other person has MATCH_PARENT and I had WRAP_CONTENT
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        this.adView.setLayoutParams(localLayoutParams);
        container.$add(this);
        this.adEnabled = true;
//        AdUnitID(adUnitID);
    }

    @SimpleEvent
    public void AdCollapsed() {
        EventDispatcher.dispatchEvent(this, "AdCollapsed");
    }

    @SimpleEvent
    public void AdExpanded() {
        EventDispatcher.dispatchEvent(this, "AdExpanded");
    }

    @SimpleEvent
    public void AdFailedToLoad(String message) {
        EventDispatcher.dispatchEvent(this, "AdFailedToLoad", message);
    }

    @SimpleEvent(description = "Called when the user is about to return to the application after clicking on an ad")
    public void AdClosed() {
        EventDispatcher.dispatchEvent(this, "AdClosed");
    }

    @SimpleEvent(description = "Called when an ad leaves the application (e.g., to go to the browser)")
    public void AdLeftApplication() {
        EventDispatcher.dispatchEvent(this, "AdLeftApplication");
    }

    @SimpleEvent(description="Called when an ad is received")
    public void AdLoaded() {
        EventDispatcher.dispatchEvent(this, "AdLoaded");
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String AdUnitID() {
        return this.adUnitID;
    }

    @DesignerProperty(defaultValue = "AD-UNIT-ID", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
//    @SimpleProperty(description = "we can't keep setting adUnitId into adView. Therefore, i have disabled the block.")
    public void AdUnitID(String adUnitId) {
        this.adUnitID = adUnitId;
        adView.setAdUnitId(adUnitID);
        LoadAd();
    }

    @SimpleFunction(description = "Destroys the ad")
    public void DestroyAd() {
        onDestroy();
    }

//    @DesignerProperty(defaultValue = "True", editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)
//    @SimpleProperty(userVisible = true)
//    public void EnableAdTargeting(boolean enabled) {
//        this.enableAdTargeting = enabled;
//    }
//
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public boolean EnableAdTargeting() {
//        return this.enableAdTargeting;
//    }

    // Turning off the designer because its causing issues and users are getting confused
//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(userVisible = true,description = "Use this to enable test mode. Will take effect when you use LoadAd block")
    public void TestMode(boolean enabled) {
        this.isTestMode = enabled;
        Log.d(TAG, "flipping the test mode to: " + this.isTestMode );

    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean TestMode() {
        return isTestMode;
    }

//    @DesignerProperty(defaultValue = "True", editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)
//    @SimpleProperty(userVisible = true)
//    public void EnableGeoLocationTargeting(boolean enabled) {
//        this.geoLocationEnabled = enabled;
//    }
//
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public boolean EnableGeoLocationTargeting() {
//        return this.geoLocationEnabled;
//    }

//    @DesignerProperty(defaultValue = "True", editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)
//    @SimpleProperty(userVisible = true)
//    public void EnableLog(boolean enabled) {
//        this.enableLog = enabled;
//    }
//
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public boolean EnableLog() {
//        return this.enableLog;
//    }

    @SimpleFunction(description = "Pauses delivery of ads")
    public void PauseAd() {
        onPause();
    }

    @SimpleFunction(description = "Resumes delivery of ads")
    public void ResumeAd() {
        onResume();
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public int TargetAge() {
        return targetAge;
    }

    @DesignerProperty(defaultValue = "0", editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER)
    @SimpleProperty(description = "Leave 0 for targeting ALL ages")
    public void TargetAge(int targetAge) {
        this.targetAge = targetAge;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean TargetForChildren() {
        return targetForChildren;
    }

    @DesignerProperty(defaultValue = "False", editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)
    @SimpleProperty(description = "Indicate whether you want Google to treat your content as child-directed when you make an ad request. " +
            "Info here: https://developers.google.com/mobile-ads-sdk/docs/admob/android/targeting#child-directed_setting")
    public void TargetForChildren(boolean enabled) {
        this.targetForChildren = enabled;
    }

//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public String TestUnitID() {
//        return testUnitID;
//    }
//
//    @DesignerProperty(defaultValue = "Enter Test unit ID", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
//    @SimpleProperty
//    public void TestUnitID(String testUnitID) {
//        this.testUnitID = testUnitID;
//    }

    @DesignerProperty(defaultValue = "ALL", editorType = PropertyTypeConstants.PROPERTY_TYPE_GENDER_OPTIONS)
    @SimpleProperty
    public void TargetGender(String gender) {
        targetGender = gender;
    }

    public View getView() {
        return this.adView;
    }

    @SimpleFunction(description = "Loads a new ad.")
    public void LoadAd() {
        if (!adEnabled) return;
        Log.d(TAG, "The test mode status is: " + this.isTestMode );

        //We can't keep resetting ad unintid into the view. It will throw exception
//        adView.setAdUnitId(adUnitID);

        if (this.isTestMode) {
//            String aid = Settings.Secure.getString(container.$form().getContentResolver(), Settings.Secure.ANDROID_ID);

//            adView.loadAd(new AdRequest.Builder().addTestDevice(testUnitID).build());
            Log.d(TAG, "Test mode" );
//            final TelephonyManager tm =(TelephonyManager) container.$form().getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//            String deviceid = tm.getDeviceId();
            String deviceId = AdMobUtil.guessSelfDeviceId(container.$context());
//            AdRequest adrequest = new AdRequest.Builder().addTestDevice(adUnitID).build();
            AdRequest request = new AdRequest.Builder()
//                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                    .addTestDevice(deviceId)
//                    .addTestDevice(aid)
                    .build();
            adView.loadAd(request);
            return;
        }

        Log.d(TAG, "Serving real ads; production non-Test mode" );

        AdRequest.Builder builder = new AdRequest.Builder();
      if (targetForChildren) {
        builder = builder.tagForChildDirectedTreatment(true);
      }

        //target for gender, if any
        if (targetGender.equalsIgnoreCase("female")) {
            builder.setGender(AdRequest.GENDER_FEMALE);
            Log.d(TAG, "Targeting females" );
        } else if ("gender".equalsIgnoreCase("male")) {
            Log.d(TAG, "Targeting males" );
            builder.setGender(AdRequest.GENDER_MALE);
        }

        //target for age, if any
        if (targetAge >0) {
            Log.d(TAG, "Targeting calendar age of: " + getDateBasedOnAge(targetAge));
            builder.setBirthday(getDateBasedOnAge(targetAge));
        }

        //now load the ad
        adView.loadAd(builder.build());

    }

    public void onDestroy() {
        Log.i("AdMobListener", "AdMob is onDestroy()");
        if (adView != null) {
            adView.destroy();
            adEnabled = false;
        }
    }

    public void onPause() {
        Log.i("AdMobListener", "AdMob is onPause()");
        if (adView != null) {
            adView.pause();
//            adEnabled = false;
        }
    }

    public void onResume() {
        Log.i("AdMobListener", "onResume()");
        if (adView != null) {
            adView.resume();
//            adEnabled = true;
//            this.pauseAAd = false;
        }
//        this.pauseAAd = true;
//        this.pauseAAd = false;
    }

//    @SimpleFunction(description = "Reloads the ad")
//    public void reloadAd() {
//        if (!adEnabled) return;
//
//        if (isTestMode) {
////            adView.loadAd(new AdRequest.Builder().addTestDevice(testUnitID).build());
//            Log.d(TAG, "Test mode" );
//            adView.loadAd(new AdRequest.Builder().addTestDevice(adUnitID).build());
//            return;
//        }
//
//        AdRequest.Builder builder = new AdRequest.Builder();
//
//        //target for gender, if any
//        if (targetGender.equalsIgnoreCase("female")) {
//            builder.setGender(AdRequest.GENDER_FEMALE);
//            Log.d(TAG, "Targeting females" );
//        } else if ("gender".equalsIgnoreCase("male")) {
//            Log.d(TAG, "Targeting males" );
//            builder.setGender(AdRequest.GENDER_MALE);
//        }
//
//        //target for age, if any
//        if (targetAge >0) {
//            Log.d(TAG, "Targeting calendar age of: " + getDateBasedOnAge(targetAge));
//            builder.setBirthday(getDateBasedOnAge(targetAge).getTime());
//        }
//
//        //now load the ad
//        adView.loadAd(builder.build());
//    }

    /**
     * Given age, it calculates the calendar year
     * @param age
     * @return
     */
    private Date getDateBasedOnAge(int age) {
        //get current time, age years from it, then convert to date and return
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, age * -1);

        Date date = new Date(cal.getTimeInMillis() );

        Log.d(TAG, "The calculated date based on age of " + age + " is " + date);

        return date;
    }

    /**
     * Gets a string error reason from an error code.
     */
    private String getErrorReason(int errorCode) {
        String errorReason = "";
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Something happened internally; for instance, an invalid response was received from the ad server.";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "The ad request was invalid; for instance, the ad unit ID was incorrect";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "The ad request was unsuccessful due to network connectivity";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "The ad request was successful, but no ad was returned due to lack of ad inventory";
                break;
        }

        Log.d(TAG, "Got add error reason of: " + errorReason);

        return errorReason;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "AD_UNIT_ID")
    @SimpleProperty(description = "Sets the AdMob Publisher Id", userVisible =false)
    public void PublisherId(String publisherId) {
        AdUnitID(publisherId);
//        throw new IllegalArgumentError("Deprecated. Please use AdUnitID property");
    }

    /**
     * Get the current publisher id
     *
     * @return AdMob publisher id
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible =false)
    public String PublisherId() {
        return AdUnitID();
//        throw new IllegalArgumentError("Deprecated. Please use AdUnitID property");
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "If true, device that will receive test ads. " +
            "You should utilize this property during development to avoid generating false impressions")
    public void AdEnabled(boolean enabled) {
        this.adEnabled = enabled;
        if (enabled) onResume();
        else onPause();
    }

    /**
     * Returns status of AdEnabled
     *
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean AdEnabled() {
        return adEnabled;
    }

    public class AdListenerPage extends AdListener {
        private Context mContext;

        public AdListenerPage(Context arg2) {
            this.mContext = arg2;
        }

        public void onAdClosed() {
            Log.d("AdMobListener", "onAdClosed");
            AdClosed();
        }

        public void onAdFailedToLoad(int paramInt) {
            Log.d("AdMobListener", "onAdFailedToLoad: " + getErrorReason(paramInt));
            adFailedToLoadMessage = getErrorReason(paramInt);
            AdFailedToLoad(adFailedToLoadMessage);
        }

        public void onAdLeftApplication() {
            AdLeftApplication();
        }

        public void onAdLoaded() {
            Log.d("AdMobListener", "onAdLoaded");
            onAdLoadedMsg = this.mContext;
//            AdMob.this.AdLoaded(AdMob.this.onAdLoadedMsg);
            AdLoaded();
        }

        public void onAdOpened() {
            Log.d("AdMobListener", "onAdOpened");
            AdExpanded();

        }
    }
}