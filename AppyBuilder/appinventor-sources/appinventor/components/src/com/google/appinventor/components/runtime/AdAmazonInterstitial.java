package com.google.appinventor.components.runtime;

//See here: https://developer.amazon.com/public/apis/earn/mobile-ads/docs/event-tracking-and-errors
// https://developer.amazon.com/blog/tag/Interstitial+Ads.html

import android.util.Log;
import com.amazon.device.ads.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;


@DesignerComponent(version = 1,
                   description =  "An interstitial ad is a full-page ad. "
                           + "AdAmazonInterstitial component allows you to monetize your app. You must have a valid Amazon Application Key." +
                           " If your application key is invalid, the "
                           + "ad will not display on the emulator or the device. "
                           + "Warning: Make sure you're in test mode during development to avoid being disabled for clicking your own ads. ",
                   category = ComponentCategory.MONETIZE,
                   nonVisible = true,
                   iconName = "images/adamazoninterstitial.png")
@SimpleObject
@UsesLibraries(libraries = "amazon-ads-5.8.1.1" +
        ".jar")
@UsesPermissions(
    permissionNames = "android.permission.INTERNET,android.permission.ACCESS_COARSE_LOCATION,android.permission.ACCESS_FINE_LOCATION,android.permission.ACCESS_NETWORK_STATE,android.permission.ACCESS_WIFI_STATE")
public class AdAmazonInterstitial extends AndroidNonvisibleComponent implements Component {


  private static final String LOG_TAG = "AdAmazonInterstitial";
  private String appKey = "";
//  private boolean enableAdTargeting = false;
  private boolean enableDebug = true;
  private boolean enableTesting=true;
  private boolean isAdLoaded=false;
  private boolean geoLocationEnabled;
//  private boolean enableLog;
  private int targetAge;
  private InterstitialAd interstitialAd;

  public AdAmazonInterstitial(ComponentContainer container) {
    super(container.$form());

//    //================ start: remove if app doesn't load anymore
//    // For debugging purposes enable logging, but disable for production builds.
//    AdRegistration.enableLogging(enableLog);
//    // For debugging purposes flag all ad requests as tests, but set to false for production builds.
//    AdRegistration.enableTesting(enableTesting);
//
//    AdRegistration.setAppKey(appKey);
//    //================ end: remove if app doesn't load anymore

    interstitialAd = new InterstitialAd(container.$context());
    interstitialAd.setListener(new AmazonAdListener());

  }

  @SimpleEvent(description = "After a user clicks on the close ad button on an expanded ad, "
                             + "this callback is called immediately after collapsing the ad. "
                             + "This callback can be used to do things like resume your app or restart audio.")
  public void AdCollapsed() {
    EventDispatcher.dispatchEvent(this, "AdCollapsed");
  }

  @SimpleEvent(description = "This callback is called each time an ad is successfully loaded. You can use this to log metrics on ad views and assist with initial integration. Detailed information about the ad that loaded can be obtained from the AdProperties object.")
  public void AdExpanded() {
    EventDispatcher.dispatchEvent(this, "AdExpanded");
  }

  @SimpleEvent(description = "Whenever an ad fails to be retrieved, the event is called, returning the error message.")
  public void AdFailedToLoad(String error, String message) {
    //todo: check this to see how to fall back on AdMob if AdAmazonInterstatial fails to load
    //https://developer.amazon.com/public/apis/earn/mobile-ads/docs/with-other-sdks
    EventDispatcher.dispatchEvent(this, "AdFailedToLoad", error, message);
  }

  @SimpleEvent(description = "Called when an an attempt was made to display the ad, but the ad was not ready to display")
  public void AdFailedToShow(String message) {
    EventDispatcher.dispatchEvent(this, "AdFailedToShow", message);
  }

  @SimpleEvent(description = "Triggered when the close button of the interstitial ad is clicked. "
                             + "It's important to remember only one interstitial ad can be shown at a time. "
                             + "The previous ad has to be dismissed before a new ad can be shown.")
  public void AdClosed() {
    EventDispatcher.dispatchEvent(this, "AdClosed");
  }

  @SimpleEvent(description = "Triggered each time an ad is successfully loaded. But you don't have to display the ad right after it's loaded. "
                             + "For example, set a flag to true and then at a transition point, if flag=true, then display the ad.")
  public void AdLoaded() {
    EventDispatcher.dispatchEvent(this, "AdLoaded");
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)  //we don't want the blocks for this
  public String ApplicationKey() {
    return this.appKey;
  }

  //no blocks for this. We set visibility to false
  @DesignerProperty(defaultValue = "ApplicationKey", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(userVisible = false, description = "Enter Application Key. Go to Amazon Developer Portal and sign-in for your ApplicationKey")
  public void ApplicationKey(String appKey) {
    this.appKey = appKey;
  }

//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
//  @SimpleProperty(userVisible = true, description = "If true, then GeoTargeting or AgeTargeting can be used. If false, then no targeting will be set")
//  public void EnableAdTargeting(boolean enabled) {
//    this.enableAdTargeting = enabled;
//  }
//
//  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//  public boolean EnableAdTargeting() {
//    return this.enableAdTargeting;
//  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty(userVisible = true)
  public void EnableDebug(boolean enabled) {
    this.enableDebug = enabled;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean EnableDebug() {
    return this.enableDebug;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty(userVisible = true, description = "For debugging / development purposes flag all ad requests as tests, but set to false for production builds")
  public void EnableTesting(boolean enabled) {
    this.enableTesting = enabled;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean EnableTesting() {
    return this.enableTesting;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty(userVisible = true, description = "If set to true, uses latitude and longitude coordinates as part of an ad request")
  public void EnableGeoLocationTargeting(boolean enabled) {
    this.geoLocationEnabled = enabled;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean EnableGeoLocationTargeting() {
    return this.geoLocationEnabled;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
//  @SimpleProperty(userVisible = true, description = "For debugging purposes enable logging, but disable for production builds")
//  public void EnableLog(boolean enabled) {
//    this.enableLog = enabled;
//  }
//
//  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//  public boolean EnableLog() {
//    return this.enableLog;
//  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int TargetAge() {
    return this.targetAge;
  }

  @DesignerProperty(defaultValue = "0", editorType = "integer")
  @SimpleProperty(description = "You can pass age information to the Amazon Mobile Ad Network to target specific age groups. If set as 0, Age Targetting will not be used")
  public void TargetAge(int paramInt) {
    this.targetAge = paramInt;
  }

  @SimpleFunction(description = "Loads a new ad")
  public void LoadAd() {

    if (isAdLoaded) {
      //If ad is already loaded, we don't continue loading another ad. Just trigger the LoadAd block
//      LoadAd();
      return;
    }

    AdRegistration.enableTesting(this.enableTesting);
    try {
      AdRegistration.setAppKey(this.appKey);
      if (geoLocationEnabled || targetAge > 0) {
        AdTargetingOptions localAdTargetingOptions = new AdTargetingOptions();

        if (geoLocationEnabled) {
          localAdTargetingOptions.enableGeoLocation(this.geoLocationEnabled);
        }

        if (this.targetAge > 0) {
          localAdTargetingOptions.setAge(this.targetAge);
        }
        //Load the ad with the targeting options
        interstitialAd.loadAd(localAdTargetingOptions);
      } else {
        //Load the ad WITHOUT the targeting options
        interstitialAd.loadAd();
      }

    } catch (Exception localException) {
      Log.e(LOG_TAG, "Exception thrown: " + localException.toString());
    }
  }

  @SimpleFunction(description = "It will show the Interstitial Ad")
  public void ShowInterstitialAd() {
    if (isAdLoaded) {
      isAdLoaded=false;
      interstitialAd.showAd();
    } else {
      String message = "Interstitial ad was not ready to be shown. Make sure you have set ad AppKey and you invoke this after LoadAd";
      Log.d(LOG_TAG, message);
      AdFailedToShow(message);
    }
  }

  //https://developer.amazon.com/public/apis/earn/mobile-ads/docs/event-tracking-and-errors
  //Error Codes: https://developer.amazon.com/public/apis/earn/mobile-ads/docs/event-tracking-and-errors
  /**
   * Extendending the default AdListener interface that allows for tracking life-cycle events of an ad.
   * The listener allows the app to take action based on the current state of the Amazon ad view.
   */
  class AmazonAdListener extends DefaultAdListener {

    public void onAdCollapsed(Ad paramAd) {
      Log.i(LOG_TAG, "Ad collapsed.");
      AdCollapsed();
    }

    public void onAdExpanded(Ad paramAd) {
      Log.i(LOG_TAG, "Ad expanded.");
      AdExpanded();
    }

    public void onAdFailedToLoad(final Ad view, final AdError error) {
      Log.w(LOG_TAG,
            "Ad failed to load. Code: " + error.getCode() + ", Message: " + error.getMessage());
//      adFailedToLoadCode = ("" + error.getCode());
        isAdLoaded=false;
        String message = (error.getMessage());
      AdFailedToLoad(error.getCode().toString(), message);
    }

    public void onAdLoaded(Ad paramAd, AdProperties adProperties) {
      Log.i(LOG_TAG, adProperties.getAdType().toString() + " ad loaded successfully.");
      isAdLoaded = true;
      AdLoaded();
    }

    public void onAdDismissed(com.amazon.device.ads.Ad ad) {
      Log.i(LOG_TAG, "Ad onAdDismissed finished");
      AdClosed(); //keeping same name as amazon interstitial
    }

  }
}