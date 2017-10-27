//package com.google.appinventor.components.runtime;
//
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.View;
//import com.google.appinventor.components.annotations.*;
//import com.google.appinventor.components.common.ComponentCategory;
//import com.google.appinventor.components.common.PropertyTypeConstants;
//import com.google.appinventor.components.common.YaVersion;
//import com.millennialmedia.android.*;
//
//@DesignerComponent(version = YaVersion.ADAMAZON_COMPONENT_VERSION,
//        description = "MMedia component allows you to monetize your app. You must have a valid App id " +
//                "that can be obtained from http://www.MillennialMedia.com. If App id is invalid, the " +
//                "banner will not display on the emulator or the device.",
//        category = ComponentCategory.MONETIZE)
//@SimpleObject
//@UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO,android.permission.INTERNET,permission.ACCESS_NETWORK_STATE,android.permission.WRITE_EXTERNAL_STORAGE")
//@UsesLibraries(libraries = "MMSDK.jar,google-play-services.jar")
//public class MMedia extends AndroidViewComponent implements OnDestroyListener {
//
//    private MMAdView adView;
//    private String LOG_TAG = "MMedia";
//    String targetAge = "0";
//    //Constants for tablet sized ads (728x90)
//    private static final int IAB_LEADERBOARD_WIDTH = 728;
//    private static final int IAB_LEADERBOARD_HEIGHT = 90;
//
//    private static final int MED_BANNER_WIDTH = 480;
//    private static final int MED_BANNER_HEIGHT = 60;
//
//    //Constants for phone sized ads (320x50)
//    private static final int BANNER_AD_WIDTH = 320;
//    private static final int BANNER_AD_HEIGHT = 50;
//
//    int placementWidth = BANNER_AD_WIDTH;
//    int placementHeight = BANNER_AD_HEIGHT;
//    MMRequest request;
//    //A default developer id
//    private String appId ="AppID";
//
//    /**
//     * Creates a new AdAmazon component.
//     *
//     * @param container  container, component will be placed in
//     */
//    public MMedia(ComponentContainer container) {
//        super(container);
//
//        adView = new MMAdView(container.$context());
//
//        determineAdSize();
//
//        adView.setWidth(placementWidth);
//        adView.setHeight(placementHeight);
//
//        Width(placementWidth);  //todo: use AdSize banners ad
//        Height(placementHeight); //todo: use AdSize banners ad
//
//        android.widget.LinearLayout.LayoutParams localLayoutParams =
//                new android.widget.LinearLayout.LayoutParams(
//                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,      //Other person has MATCH_PARENT and I had WRAP_CONTENT
//                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
//
//        adView.setLayoutParams(localLayoutParams);
//        adView.setApid(appId);
//        //Sets the id to preserve your ad on configuration changes.
//        adView.setId(MMSDK.getDefaultAdId());
//
//        //Set your metadata in the MMRequest object
//        request = new MMRequest();
//        // Set available metadata here.  IE: age, children, education, etc
////        request.setAge(targetAge);
//        //Add the MMRequest object to your MMAdView.
//        adView.setMMRequest(request);
//
//        adView.setListener(new AdListener());
//
////        adView.getAd();
//
////
////        determineAdSize();
////        //Set the ad size. Replace the width and height values if needed.
////        adView.setWidth(placementWidth);
////        adView.setHeight(placementHeight);
////
////        //Calculate the size of the adView based on the ad size. Replace the width and height values if needed.
////        int layoutWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, placementWidth, container.$form().getResources().getDisplayMetrics());
////        int layoutHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, placementHeight,container.$form(). getResources().getDisplayMetrics());
////
////        //https://developer.amazon.com/sdk/mobileads/understanding-api.html#Amazon Mobile Ad Network Concepts
////        adView = new MMAdView(container.$context());
////
////        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
////        adView.setLayoutParams( new android.widget.LinearLayout.LayoutParams(layoutWidth, layoutHeight));
////        //This positions the banner.
////        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
////        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//
//        // Adds the component to its designated container
//        container.$add(this);
//
//    }
//
//
//    /**
//     * Set the publisherId id
//     *
//     * @param appId The publisher id
//     */
//    //NOTE: DO NOT allow setting in the blocks-editor. It can be set ONLY ONCE
//    @DesignerProperty(defaultValue = "AppID", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
//    @SimpleProperty(userVisible = false) //we can't keep setting AppID into ad. Therefore, i have disabled the block.
//    public void AppID(String appId) {
//        this.appId = appId;
//        adView.setApid(appId);
//
//        //Set your metadata in the MMRequest object
//        request = new MMRequest();
//        adView.setId(MMSDK.getDefaultAdId());
//
//        if (Integer.valueOf(targetAge) > 0) request.setAge(targetAge);
//        adView.setMMRequest(request);
//
//        adView.getAd();
//    }
//
//    private void determineAdSize() {
//        //Finds an ad that best fits a users device.
//        if(canFit(IAB_LEADERBOARD_WIDTH)) {
//            placementWidth = IAB_LEADERBOARD_WIDTH;
//            placementHeight = IAB_LEADERBOARD_HEIGHT;
//        } else if(canFit(MED_BANNER_WIDTH)) {
//            placementWidth = MED_BANNER_WIDTH;
//            placementHeight = MED_BANNER_HEIGHT;
//        }
//    }
//    protected boolean canFit(int adWidth) {
//        int adWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, adWidth, container.$form().getResources().getDisplayMetrics());
//        DisplayMetrics metrics = container.$form().getResources().getDisplayMetrics();
//        return metrics.widthPixels >= adWidthPx;
//    }
//
//
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public int TargetAge() {
//        return Integer.valueOf(targetAge);
//    }
//
//    @DesignerProperty(defaultValue = "0", editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER)
//    @SimpleProperty(description = "Leave 0 for targeting ALL ages")
//    public void TargetAge(int targetAge) {
//        this.targetAge = targetAge + "";
//        request.setAge(""+targetAge);
//    }
//
//    /**
//     * Get the current AppID
//     *
//     * @return AppID
//     */
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public String AppID() {
//        return appId;
//    }
//
//    @Override
//    public View getView() {
//        return adView;
//    }
//
//    @Override
//    public void onDestroy() {
//        if (adView != null) {
////            adView.destroy();
//        }
//    }
//
//
//
//
//
//    @SimpleEvent
//    public void AdDismissed()
//    {
//        EventDispatcher.dispatchEvent(this, "AdDismissed");
//    }
//
//    @SimpleEvent
//    public void AdExpanded()
//    {
//        EventDispatcher.dispatchEvent(this, "AdExpanded");
//    }
//
//    @SimpleEvent
//    public void AdFailedToLoad(String errMsg)
//    {
//        EventDispatcher.dispatchEvent(this, "AdFailedToLoad", errMsg);
//    }
//
//    @SimpleEvent
//    public void AdLoaded()
//    {
//        EventDispatcher.dispatchEvent(this, "AdLoaded");
//    }
//
//    /** Demonstrates methods to implement as part of the MMAdListener interface */
//    public class AdListener implements RequestListener
//    {
//        @Override
//        public void MMAdOverlayLaunched(MMAd mmAd)
//        {
//            Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") overlay launched");
//        }
//
//        @Override
//        public void MMAdRequestIsCaching(MMAd mmAd)
//        {
//            Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") caching started");
//        }
//
//        @Override
//        public void requestCompleted(MMAd mmAd)
//        {
//            Log.i(LOG_TAG,"Millennial Media Ad (" + mmAd.getApid() + ") request succeeded");
//        }
//
//        @Override
//        public void requestFailed(MMAd mmAd, MMException exception)
//        {
//            Log.i(LOG_TAG,String.format("Millennial Media Ad (" + mmAd.getApid() + ") request failed with error: %d %s.", exception.getCode(), exception.getMessage()));
//        }
//
//        @Override
//        public void MMAdOverlayClosed(MMAd mmAd)
//        {
//            Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") overlay closed");
//
//        }
//        @Override
//        public void onSingleTap(MMAd mmAd)
//        {
//            Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") single tap");
//        }
//    }
//
//    // ================= listener
////    @Override
////    public void MMAdOverlayLaunched(MMAd mmAd) {
////        //no-op
////    }
//
////    @Override
////    public void MMAdOverlayClosed(MMAd mmAd) {
////        //no-op
////    }
////
////    @Override
////    public void MMAdRequestIsCaching(MMAd mmAd) {
////        //no-op
////    }
////
////    @Override
////    public void requestCompleted(MMAd mmAd) {
////        AdLoaded();
////    }
////
////    @Override
////    public void requestFailed(MMAd mmAd, MMException e) {
////        AdFailedToLoad(e.getMessage());
////    }
////
////    @Override
////    public void onSingleTap(MMAd mmAd) {
////
////    }
//}
