// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMInterstitial;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.RequestListener;

@DesignerComponent(version = YaVersion.MMEDIA_INTERSTITIAL_COMPONENT_VERSION,
                   description = "An interstitial ad is a full-page ad. "
                                 + "MMediaInterstitial component allows you to monetize your app. You must have a valid MMedia account and AdUnitId "
                                 + "that can be obtained from http://www.MillennialMedia.com . If your id is invalid, the "
                                 + "MMediaInterstitial will not display on the emulator or the device. ",
                   category = ComponentCategory.MONETIZE,
                   nonVisible = true,
                   iconName = "images/mmediainterstitial.png")
@SimpleObject
@UsesLibraries(libraries = "MMSDK.jar,google-play-services.jar")
@UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO,android.permission.INTERNET,permission.ACCESS_NETWORK_STATE,android.permission.WRITE_EXTERNAL_STORAGE")
public class MMediaInterstitial extends AndroidNonvisibleComponent implements Component {

  public String adFailedToLoadMessage;
  public String appId;
  private MMInterstitial interstitialAd;
  public int targetAge = 0;
  private String targetGender = "ALL";
    MMRequest mmRequest;

  private static final String LOG_TAG = "MMediaInterstitial";

  public MMediaInterstitial(ComponentContainer container) {
    super(container.$form());

    interstitialAd = new MMInterstitial(container.$context());
    mmRequest = new MMRequest();
    interstitialAd.setMMRequest(mmRequest);
    interstitialAd.setListener(new AdListenerPage());

  }


  @SimpleEvent(description = "Called when an ad request failed. message will display the reason for why the ad failed")
  public void AdFailedToLoad(String message) {
    EventDispatcher.dispatchEvent(this, "AdFailedToLoad", message);
  }

  @SimpleEvent(description = "Called when the user is about to return to the application after clicking on an ad")
  public void AdClosed() {
    EventDispatcher.dispatchEvent(this, "AdClosed");
  }


  @SimpleEvent(description = "Called when an ad is received")
  public void AdLoaded() {
    EventDispatcher.dispatchEvent(this, "AdLoaded");
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false) //we don't want the blocks for this
  public String AppID() {
    return this.appId;
    }

  //NOTE: DO NOT allow setting in the blocks-editor. It can be set ONLY ONCE
  @DesignerProperty(defaultValue = "AppID", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(userVisible = false)  //we can't keep setting AppID into ad. Therefore, i have disabled the block.
  public void AppID(String appId) {
    this.appId = appId;

    //NOTE: The ad unit ID can only be set once on InterstitialAd. Therefore, we don't allow it in designer property
    interstitialAd.setApid(appId);
    LoadAd();   //NOTE: setAdUnitId has to be done first. If we load ad, it will cause ambigeous runtime exception. DO NOT LoadAd here
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


  @SimpleFunction(description = "Loads a new ad.")
  public void LoadAd() {
      MMRequest request = new MMRequest();

      //target for gender, if any
      if (!targetGender.equals("ALL")) {
          request.setGender(targetGender);
      }


    //target for age, if any
    if (targetAge > 0) {
      Log.d(LOG_TAG, "Targeting calendar age of: " + targetAge);
      request.setAge(""+targetAge);
    }

      interstitialAd.setMMRequest(request);
      interstitialAd.fetch();

    interstitialAd.setListener((new AdListenerPage()));
  }

    // http://docs.millennialmedia.com/android-SDK/AndroidDocsRequestListener.html
    @SimpleEvent(description = "Invoked when user taps on the ad")
    public void AdExpanded()
    {
        EventDispatcher.dispatchEvent(this, "AdExpanded");
    }

    @SimpleEvent(description = "Invoked when user taps on the ad")
    public void AdClicked()
    {
        EventDispatcher.dispatchEvent(this, "AdClicked");
    }

    public class AdListenerPage extends RequestListener.RequestListenerImpl {
      @Override
      public void requestCompleted(MMAd mmAd) {
          Log.d("MMediaListener", "requestCompleted");
          AdLoaded();
      }

        public void MMAdOverlayLaunched(MMAd mmAd) {
            AdExpanded();

        }
      public void onSingleTap(com.millennialmedia.android.MMAd mmAd) {
          AdClicked();
      }


      public void requestFailed(com.millennialmedia.android.MMAd mmAd, com.millennialmedia.android.MMException error) {
          Log.d("MMediaListener", "requestFailed");
           AdFailedToLoad(error.getMessage());
      }

      public void MMAdOverlayClosed(com.millennialmedia.android.MMAd mmAd) {
          Log.d("MMediaListener", "MMAdOverlayClosed" );
          AdClosed();
      }



  }

  @SimpleFunction(description = "It will show the Interstitial Ad")
  public void ShowInterstitialAd() {
    if (interstitialAd.isAdAvailable()) {
        interstitialAd.display(); // display the ad that was cached by fetch
    } else {
      adFailedToLoadMessage = "Interstitial AppID of " + appId +
              " was not ready to be shown. Make sure you have set AppId and is valid ID";
      Log.d(LOG_TAG, adFailedToLoadMessage);
      AdFailedToLoad(adFailedToLoadMessage);
    }
  }
}