// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import com.google.android.gms.ads.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import android.content.Context;
import android.util.Log;
import com.google.appinventor.components.runtime.util.AdMobUtil;

import java.util.Calendar;
import java.util.Date;


//Good reading: http://appflood.com/blog/interstitial-ads-generate-the-highest-earnings
@DesignerComponent(version = YaVersion.ADMOB_INTERSTITIAL_COMPONENT_VERSION,
                   description = "An interstitial ad is a full-page ad. "
                                 + "AdMobInterstitial component allows you to monetize your app. You must have a valid AdMob account and AdUnitId "
                                 + "that can be obtained from http://www.google.com/AdMob . If your id is invalid, the "
                                 + "AdMobInterstitial will not display on the emulator or the device. "
                                 + "Warning: Make sure you're in test mode during development to avoid being disabled for clicking your own ads. ",
                   category = ComponentCategory.MONETIZE,
                   nonVisible = true,
                   iconName = "images/admobInterstitial.png")
@SimpleObject
@UsesLibraries(libraries = "google-play-services.jar")
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.ACCESS_NETWORK_STATE"
//        + ",android.permission.ACCESS_FINE_LOCATION,android.permission.ACCESS_COARSE_LOCATION,android.permission.ACCESS_WIFI_STATE"
)
public class AdMobInterstitial extends AndroidNonvisibleComponent implements Component {

  public String adFailedToLoadMessage;
  public String adUnitId;
  private InterstitialAd interstitialAd;
  private boolean enableTesting = false;
  public Context onAdLoadedMsg;
  public int targetAge = 0;
  private String targetGender = "ALL";
  private boolean adEnabled = true;

  private static final String LOG_TAG = "AdMobInterstitial";
  private boolean targetForChildren = false;
  protected final ComponentContainer container;

  public AdMobInterstitial(ComponentContainer container) {
    super(container.$form());
    this.container = container;

    interstitialAd = new InterstitialAd(container.$context());
    interstitialAd.setAdListener(new AdListenerPage(container.$context()));

    this.adEnabled = true;
//    AdUnitID(adUnitID);
  }


  @SimpleEvent(description = "Called when an ad request failed. message will display the reason for why the ad failed")
  public void AdFailedToLoad(String error, String message) {
    EventDispatcher.dispatchEvent(this, "AdFailedToLoad", error, message);
  }

  @SimpleEvent(description = "Called when an an attempt was made to display the ad, but the ad was not ready to display")
  public void AdFailedToShow(String message) {
    EventDispatcher.dispatchEvent(this, "AdFailedToShow", message);
  }

  @SimpleEvent(description = "Called when the user is about to return to the application after clicking on an ad")
  public void AdClosed() {
    EventDispatcher.dispatchEvent(this, "AdClosed");
  }

  @SimpleEvent(
      description = "Called when an ad leaves the application (e.g., to go to the browser). ")
  public void AdLeftApplication() {
    EventDispatcher.dispatchEvent(this, "AdLeftApplication");
  }

  @SimpleEvent(description = "Called when an ad is received")
  public void AdLoaded() {
    EventDispatcher.dispatchEvent(this, "AdLoaded");
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false) //we don't want the blocks for this
  public String AdUnitID() {
    return this.adUnitId;
    }

  //NOTE: DO NOT allow setting in the blocks-editor. It can be set ONLY ONCE
  @DesignerProperty(defaultValue = "AD-UNIT-ID", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(userVisible = false)  //we can't keep setting adUnitId into ad. Therefore, i have disabled the block.
  public void AdUnitID(String adUnitId) {
    this.adUnitId = adUnitId;

    //NOTE: The ad unit ID can only be set once on InterstitialAd. Therefore, we don't allow it in designer property
    interstitialAd.setAdUnitId(adUnitId);
    LoadAd();   //NOTE: setAdUnitId has to be done first. If we load ad, it will cause ambigeous runtime exception. DO NOT LoadAd here
  }

  // Turning off the designer because its causing issues and users are getting confused
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(userVisible = true, description = "For debugging / development purposes flag all ad requests as tests, " +
          "but set to false for production builds. Will take effect when you use LoadAd block.")
  public void TestMode(boolean enabled) {
    this.enableTesting = enabled;
    Log.d(LOG_TAG, "flipping the test mode to: " + this.enableTesting);

  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean TestMode() {
    return enableTesting;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int TargetAge() {
    return targetAge;
  }

  @DesignerProperty(defaultValue = "0",
                    editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER)
  @SimpleProperty(description = "Leave 0 for targeting ALL ages")
  public void TargetAge(int targetAge) {
    this.targetAge = targetAge;
  }

  @DesignerProperty(defaultValue = "ALL",
                    editorType = PropertyTypeConstants.PROPERTY_TYPE_GENDER_OPTIONS)
  @SimpleProperty
  public void TargetGender(String gender) {
    targetGender = gender;
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


  @SimpleFunction(description = "Loads a new ad.")
  public void LoadAd() {
    if (!adEnabled) {
      return;
    }
    Log.d(LOG_TAG, "The test mode status is: " + this.enableTesting);

    if (this.enableTesting) {
//            adView.loadAd(new AdRequest.Builder().addTestDevice(testUnitID).build());
      Log.d(LOG_TAG, "Test mode");
      String device = AdMobUtil.guessSelfDeviceId(container.$context());

      interstitialAd.loadAd(new AdRequest.Builder().addTestDevice(device).build());
      return;
    }

    Log.d(LOG_TAG, "Serving real ads; production non-Test mode");

    AdRequest.Builder builder = new AdRequest.Builder();

    if (targetForChildren) {
      builder = builder.tagForChildDirectedTreatment(true);
    }

    //target for gender, if any
    if (targetGender.equalsIgnoreCase("female")) {
      builder.setGender(AdRequest.GENDER_FEMALE);
      Log.d(LOG_TAG, "Targeting females");
    } else if ("gender".equalsIgnoreCase("male")) {
      Log.d(LOG_TAG, "Targeting males");
      builder.setGender(AdRequest.GENDER_MALE);
    }

    //target for age, if any
    if (targetAge > 0) {
      Log.d(LOG_TAG, "Targeting calendar age of: " + getDateBasedOnAge(targetAge));
      builder.setBirthday(getDateBasedOnAge(targetAge));
    }

    if (interstitialAd.isLoaded()) {
      //If ad is already loaded, we don't continue loading another ad. Just trigger the LoadAd block
      return;
    }

    //otherwise, we now load the ad
    interstitialAd.loadAd(builder.build());   //this will trigger the LoadAd block

  }


  /**
   * Given age, it calculates the calendar year
   */
  private Date getDateBasedOnAge(int age) {
    //get current time, age years from it, then convert to date and return
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, age * -1);

    Date date = new Date(cal.getTimeInMillis());

    Log.d(LOG_TAG, "The calculated date based on age of " + age + " is " + date);

    return date;
  }

  /**
   * Gets a string error reason from an error code.
   */
  private String getErrorReason(int errorCode) {
    String errorReason = "";
    switch (errorCode) {
      case AdRequest.ERROR_CODE_INTERNAL_ERROR:
        errorReason =
            "Something happened internally; for instance, an invalid response was received from the ad server.";
        break;
      case AdRequest.ERROR_CODE_INVALID_REQUEST:
        errorReason = "The ad request was invalid; for instance, the ad unit ID was incorrect";
        break;
      case AdRequest.ERROR_CODE_NETWORK_ERROR:
        errorReason = "The ad request was unsuccessful due to network connectivity";
        break;
      case AdRequest.ERROR_CODE_NO_FILL:
        errorReason =
            "The ad request was successful, but no ad was returned due to lack of ad inventory";
        break;
    }

    Log.d(LOG_TAG, "Got add error reason of: " + errorReason);

    return errorReason;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty(description = "If true, device that will receive test ads. " +
                                "You should utilize this property during development to avoid generating false impressions")
  public void AdEnabled(boolean enabled) {
    this.adEnabled = enabled;
  }

  /**
   * Returns status of AdEnabled
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

    public void onAdFailedToLoad(int error) {
      Log.d("AdMobListener", "onAdFailedToLoad: " + getErrorReason(error));
      adFailedToLoadMessage = getErrorReason(error);
      AdFailedToLoad(error+"", getErrorReason(error));
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
    }
  }

  @SimpleFunction(description = "It will show the Interstitial Ad")
  public void ShowInterstitialAd() {
    if (interstitialAd.isLoaded()) {
      interstitialAd.show();
    } else {
      adFailedToLoadMessage = "Interstitial ad was not ready to be shown. Make sure you have set AdUnitId and you invoke this after LoadAd";
      Log.d(LOG_TAG, adFailedToLoadMessage);
      AdFailedToShow(adFailedToLoadMessage);
    }
  }
}