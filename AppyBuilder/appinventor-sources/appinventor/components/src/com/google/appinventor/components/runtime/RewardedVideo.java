package com.google.appinventor.components.runtime;

//import android.widget.Toast;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import android.util.Log;
import com.google.appinventor.components.runtime.util.AdMobUtil;

@DesignerComponent(version = YaVersion.ADMOB_REWARD_VIDEO_COMPONENT_VERSION,
        description = "add description in OdeMessage ",
        category = ComponentCategory.MONETIZE,
        nonVisible = true,
        iconName = "images/rewardedvideo.png")
@SimpleObject
@UsesLibraries(libraries = "google-play-services.jar")
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.ACCESS_NETWORK_STATE" )
public class RewardedVideo extends AndroidNonvisibleComponent implements Component, RewardedVideoAdListener {

    public String adUnitId;
    private boolean enableTesting = false;
    private boolean adEnabled = true;
    private RewardedVideoAd mAd;

    private static final String LOG_TAG = "RewardedVideo";
    protected final ComponentContainer container;

    public RewardedVideo(ComponentContainer container) {
        super(container.$form());
        this.container = container;

        mAd = MobileAds.getRewardedVideoAdInstance(container.$context());
        mAd.setRewardedVideoAdListener(this);

        this.adEnabled = true;
//    AdUnitID(adUnitID);
    }

    @SimpleEvent(description = "Triggered when AD fails to load")
    public void AdFailedToLoad(int errCode, String errMessage) {
        EventDispatcher.dispatchEvent(this, "AdFailedToLoad", errCode, errMessage);
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

    // sample code here: https://developers.google.com/admob/android/rewarded-video
    // sample rewarded video id: ca-app-pub-3940256099942544/5224354917
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean TestMode() {
        return enableTesting;
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

            mAd.loadAd(adUnitId, new AdRequest.Builder().addTestDevice(device).build());

            return;
        }

        //otherwise, we now load the ad
        mAd.loadAd(adUnitId, new AdRequest.Builder().build());  //todo: make sure you have the testUnitid

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
    // Required to reward the user.
    @Override
    public void onRewarded(RewardItem reward) {
        Rewarded(reward.getType(), reward.getAmount());
    }

    // The following listener methods are optional.
    @Override
    public void onRewardedVideoAdLeftApplication() {
//        Toast.makeText(container.$context(), "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
        AdLeftApplication();
    }

    @Override
    public void onRewardedVideoAdClosed() {
//        Toast.makeText(container.$context(), "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        AdClosed();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
//        Toast.makeText(container.$context(), "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();

        // ERROR_CODE_INTERNAL_ERROR, ERROR_CODE_INVALID_REQUEST, ERROR_CODE_NETWORK_ERROR, or ERROR_CODE_NO_FILL
        String errMessage;
        switch (errorCode) {
            case 0 :
                errMessage = "Internal Error";
                break;
            case 1:
                errMessage = "Invalid Request";
                break;
            case 2:
                errMessage = "Network Error";
                break;
            case 3:
                errMessage = "Ad was not Filled";
                break;
            default: errMessage = "Unknown error. Ad Failed To Load";
                break;
        }
        AdFailedToLoad(errorCode, errMessage);
    }

    @Override
    public void onRewardedVideoAdLoaded() {
//        Toast.makeText(container.$context(), "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
        AdLoaded();
    }

    @Override
    public void onRewardedVideoAdOpened() {
//        Toast.makeText(container.$context(), "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
        AdOpened();
    }

    @SimpleEvent(description = "Ad was opened by user")
    public void AdOpened() {
        EventDispatcher.dispatchEvent(this, "AdOpened");
    }

    @Override
    public void onRewardedVideoStarted() {
//        Toast.makeText(container.$context(), "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
        AdOpened();
    }

    @SimpleFunction(description = "It will show the Video")
    public void ShowAd() {
        if (mAd.isLoaded()) {
            mAd.show();
            return;
        }

        String msg = "Video Ad is not ready to show. Make sure AD is loaded";
        AdFailedToShow(msg);
    }

    @SimpleEvent(description = "Called when an an attempt was made to display the ad, but the ad was not ready to display")
    public void AdFailedToShow(String message) {
        EventDispatcher.dispatchEvent(this, "AdFailedToShow", message);
    }

    @SimpleEvent(description = "User watched video and should be rewarded")
    public void Rewarded(String type, int amount) {
//        Toast.makeText(container.$context(), "onRewarded! currency: " + type + "  amount: " + amount, Toast.LENGTH_SHORT).show();
        EventDispatcher.dispatchEvent(this, "Rewarded", type, amount);

        // Reward the user.
    }



}