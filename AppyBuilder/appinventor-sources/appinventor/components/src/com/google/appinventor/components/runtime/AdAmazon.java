package com.google.appinventor.components.runtime;

import android.util.Log;
import android.view.View;
import com.amazon.device.ads.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.ADAMAZON_COMPONENT_VERSION,
        description = "AdAmazon component allows you to monetize your app. You must have a valid publisher id " +
                "that can be obtained from https://developer.amazon.com. If your publisher id is invalid, the " +
                "AdAmazon banner will not display on the emulator or the device.",
        category = ComponentCategory.MONETIZE)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE,android.permission.ACCESS_FINE_LOCATION,android.permission.ACCESS_COARSE_LOCATION,android.permission.ACCESS_WIFI_STATE")
@UsesLibraries(libraries = "amazon-ads-5.8.1.1.jar")
public class AdAmazon extends AndroidViewComponent implements OnDestroyListener, AdListener {

    private AdLayout adView;
    private boolean debugEnabled=true;
    private String TAG = "AdAmazon";

    //A default developer id
    private String publisherId ="AmazonPublisherId";

    /**
     * Creates a new AdAmazon component.
     *
     * @param container  container, component will be placed in
     */
    public AdAmazon(ComponentContainer container) {
        super(container);


        //https://developer.amazon.com/sdk/mobileads/understanding-api.html#Amazon Mobile Ad Network Concepts
        adView = new AdLayout(container.$context(), AdSize.SIZE_320x50);
        adView.setLayoutParams( new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

        // Adds the component to its designated container
        container.$add(this);

        adView.setListener(this);
        AdRegistration.setAppKey(publisherId);
        TestMode(true);
        RefreshAd(true);
    }

    /**
     * Set the publisherId id
     *
     * @param publisherId The publisher id
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "AmazonPublisherId")
    @SimpleProperty(description = "Sets the Amazon Ad Publisher Id and refreshes the ad")
    public void PublisherId(String publisherId) {
        publisherId=publisherId.trim();
        this.publisherId = publisherId;
        AdRegistration.setAppKey(publisherId);
        RefreshAd(true);
    }

    /**
     * Load a new ad.
     */
    @SimpleProperty(description = "Refreshes the ad")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    public void RefreshAd(boolean enabled) {
        //This method should really take no-parameters. However, the annotation process will
        // complain if no parameters AND no return type (void)
        if (enabled)  {
            // Load the ad with the appropriate ad targeting options.
            AdTargetingOptions adOptions = new AdTargetingOptions();
            adView.loadAd(adOptions);
        }
    }

    /**
     * Get the current publisher id
     *
     * @return publisher id
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String PublisherId() {
        return publisherId;
    }


    /**
     * Turns the debugging on / off based on enabled parameter.
     *
     * @param enabled if true, then AdAmazon will display log result else it won't.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty
    public void TestMode(boolean enabled) {
        this.debugEnabled = enabled;

        //todo: 2   create a method allowing user to setup the ad size
        // For debugging purposes enable logging, but disable for production builds
        AdRegistration.enableLogging(enabled);
        // For debugging purposes flag all ad requests as tests, but set to false for production builds
        AdRegistration.enableTesting(enabled);
    }

    /**
     * Returns status of TestMode
     *
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean TestMode() {
        return debugEnabled;
    }

    @Override
    public View getView() {
        return adView;
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
    }

  /**
   * This event is called after a rich media ads has collapsed from an expanded state.
   */
  public void onAdCollapsed(Ad paramAd)
  {
    Log.i(TAG, "Ad collapsed.");
    AdDismissed();
  }

  @Override
  public void onAdDismissed(Ad ad) {
    Log.i(TAG, "Ad Dismissed.");
    AdDismissed();
  }

  /**
   * This event is called after a rich media ad expands.
   */
  public void onAdExpanded(Ad paramAd)
  {
    Log.i(TAG, "Ad expanded.");
    AdExpanded();
  }

  /**
   * This event is called if an ad fails to load.
   */
  public void onAdFailedToLoad(Ad paramAd, AdError paramAdError)
  {
    Log.w("AmazonInterstitialAds", "Ad failed to load. Code: " + paramAdError.getCode() + ", Message: " + paramAdError.getMessage());
    AdFailedToLoad(paramAdError.getCode().toString(), paramAdError.getMessage());
  }

  /**
   * This event is called once an ad loads successfully.
   */
  public void onAdLoaded(Ad paramAd, AdProperties paramAdProperties)
  {
    Log.i(TAG, paramAdProperties.getAdType().toString() + " ad loaded successfully.");
    AdLoaded();
  }

  @SimpleEvent
  public void AdDismissed()
  {
    EventDispatcher.dispatchEvent(this, "AdDismissed");
  }

  @SimpleEvent
  public void AdExpanded()
  {
    EventDispatcher.dispatchEvent(this, "AdExpanded");
  }

  @SimpleEvent
  public void AdFailedToLoad(String errCode, String errMsg)
  {
    EventDispatcher.dispatchEvent(this, "AdFailedToLoad", errCode, errMsg);
  }

  @SimpleEvent
  public void AdLoaded()
  {
    EventDispatcher.dispatchEvent(this, "AdLoaded");
  }

}
