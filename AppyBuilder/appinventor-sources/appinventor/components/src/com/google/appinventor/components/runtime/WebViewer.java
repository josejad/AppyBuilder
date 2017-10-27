// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.webkit.*;
import android.widget.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.EclairUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.IOException;
import static android.app.Activity.RESULT_OK;

/**
 * Component for displaying web pages
 * This is a very limited form of browser.  You can view web pages and
 * click on links. It also handles  Javascript. There are lots of things that could be added,
 * but this component is mostly for viewing individual pages.  It's not intended to take
 * the place of the browser.
 *
 * @author halabelson@google.com (Hal Abelson)
 */

@DesignerComponent(version = YaVersion.WEBVIEWER_COMPONENT_VERSION,
        category = ComponentCategory.USERINTERFACE,
        description = "Component for viewing Web pages.  The Home URL can be " +
                "specified in the Designer or in the Blocks Editor.  The view can be set " +
                "to follow links when they are tapped, and users can fill in Web forms. " +
                "Warning: This is not a full browser.  For example, pressing the phone's " +
                "hardware Back key will exit the app, rather than move back in the " +
                "browser history." +
                "<p />You can use the WebViewer.WebViewString property to communicate " +
                "between your app and Javascript code running in the Webviewer page. " +
                "In the app, you get and set WebViewString.  " +
                "In the WebViewer, you include Javascript that references the window.AppInventor " +
                "object, using the methoods </em getWebViewString()</em> and <em>setWebViewString(text)</em>.  " +
                "<p />For example, if the WebViewer opens to a page that contains the Javascript command " +
                "<br /> <em>document.write(\"The answer is\" + window.AppInventor.getWebViewString());</em> " +
                "<br />and if you set WebView.WebVewString to \"hello\", then the web page will show " +
                "</br ><em>The answer is hello</em>.  " +
                "<br />And if the Web page contains Javascript that executes the command " +
                "<br /><em>window.AppInventor.setWebViewString(\"hello from Javascript\")</em>, " +
                "<br />then the value of the WebViewString property will be " +
                "<br /><em>hello from Javascript</em>. ")

// TODO(halabelson): Integrate control of the Back key, when we provide it

@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class WebViewer extends AndroidViewComponent implements ActivityResultListener {
    private final String LOG_TAG = "WebViewer"; //WebVewier

    private static int resultcode = 0;
    private final WebView webview;
  private android.widget.ProgressBar progressBar;

    //Pictures will be stored into SD Pictures/AppyBuilder folder
    final String PICTURE_FOLDER="AppyBuilder";

    // URL for the WebViewer to load initially
    private String homeUrl;

    // whether or not to follow links when they are tapped
    private boolean followLinks = true;

    // Whether or not to prompt for permission in the WebViewer
    private boolean prompt = true;

    // ignore SSL Errors (mostly certificate errors. When set
    // self signed certificates should work.

    private boolean ignoreSslErrors = false;

    // allows passing strings to javascript
    WebViewInterface wvInterface;
    private Resources resources;

    private boolean zoomEnabled = true;
    // the same for Android 5.0 methods only
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri> mUploadMessage;
    private boolean cameraEnabled=false;

    /**
     * Creates a new WebViewer component.
     *
     * @param container container the component will be placed in
     */
    public WebViewer(ComponentContainer container) {
        super(container);

        // Let's display the progress in the activity title bar, like the browser app does.
        // Call it BEFORE setting the webviwer to context
//        container.$form().getWindow().requestFeature(Window.FEATURE_PROGRESS);

//            Context applicationContext = container.$form().getApplicationContext();
//    resources = container.$form().getResources(); // do this only one time at initial constructor
//    LayoutInflater mInflater = (LayoutInflater) applicationContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//    int myLayout = resources.getIdentifier("lvimageview", "layout", applicationContext.getPackageName());
//
//    View convertView = mInflater.inflate(myLayout, null);
//        progressBar = (android.widget.ProgressBar) convertView.findViewById(resources.getIdentifier("progressBar", "id", applicationContext.getPackageName()));
//        webview = (WebView) convertView.findViewById(resources.getIdentifier("webView1", "id", applicationContext.getPackageName()));


        webview = new WebView(container.$context());
        webview.getSettings().setAllowFileAccess(true);

        resetWebViewClient();       // Set up the web view client
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setFocusable(true);
        // adds a way to send strings to the javascript
        wvInterface = new WebViewInterface(webview.getContext());
        webview.addJavascriptInterface(wvInterface, "AppInventor");

        // enable pinch zooming and zoom controls
        webview.getSettings().setBuiltInZoomControls(true);

    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR)
      EclairUtil.setupWebViewGeoLoc(this, webview, container.$context());

        container.$add(this);

        AllowCamera(false);

        webview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });

        // set the initial default properties.  Height and Width
        // will be fill-parent, which will be the default for the web viewer.

        HomeUrl("");
        Width(LENGTH_FILL_PARENT);
        Height(LENGTH_FILL_PARENT);
    }

    private void resetWebView() {
//    Context applicationContext = container.$form().getApplicationContext();
//    resources = applicationContext.getResources(); // do this only one time at initial constructor
//    LayoutInflater mInflater = (LayoutInflater) applicationContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//    int myLayout = resources.getIdentifier("lvimageview", "layout", applicationContext.getPackageName());
//
//    View convertView = mInflater.inflate(myLayout, null);
//    progressBar = (ProgressBar) convertView.findViewById(resources.getIdentifier("progressBar", "id", applicationContext.getPackageName()));
//    webview = (WebView) convertView.findViewById(resources.getIdentifier("webView1", "id", applicationContext.getPackageName()));
        setupClient();
        //todo: set h,w of both progressBar and webview to user-selected
        // set the initial default properties.  Height and Width
        // will be fill-parent, which will be the default for the web viewer.


    }

    private void setupClient() {
        Log.d(LOG_TAG, "setupClient invoked");
        webview.setWebViewClient(new MyAppWebViewClient());

        webview.setWebChromeClient(new WebChromeClient() {

            // page loading progress, gone when fully loaded
            public void onProgressChanged(WebView view, int progress) {
       /* if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
          progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        progressBar.setProgress(progress);
        if (progress == 100) {
          progressBar.setVisibility(ProgressBar.GONE);
        }*/
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
//                container.$form().setProgress(progress * 1000);
            }

            // for Lollipop, all in one
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
//                Log.d(LOG_TAG, "onShowFileChooser invoked 1");

                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                Intent takePictureIntent = null;

                if (cameraEnabled) {
                    takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(container.$form().getPackageManager()) != null) {

                        // create the file where the photo should go
                        java.io.File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.e(LOG_TAG, "Unable to create Image File", ex);
                        }

                        // continue only if the file was successfully created
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");

                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                container.$context().startActivityForResult(chooserIntent, getResultCode());

                return true;
            }

            // creating image files (Lollipop only)
            private java.io.File createImageFile() throws IOException {
//                Log.d(LOG_TAG, "createImageFile invoked 1");

                //NOTE: Camera files will be created in Pictures/AppyBuilder folder
                java.io.File imageStorageDir = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PICTURE_FOLDER);

                if (!imageStorageDir.exists()) {
                    imageStorageDir.mkdirs();
                }

                // create an image file name
                imageStorageDir = new java.io.File(imageStorageDir + java.io.File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                return imageStorageDir;
            }

            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
//                Log.d(LOG_TAG, "openFileChooser invoked 21");
                mUploadMessage = uploadMsg;

                try {
                    java.io.File imageStorageDir = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PICTURE_FOLDER);

                    if (!imageStorageDir.exists()) {
                        imageStorageDir.mkdirs();
                    }

                    java.io.File file = new java.io.File(imageStorageDir + java.io.File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");

                    mCapturedImageURI = Uri.fromFile(file); // save to the private variable

                    final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                    // captureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(i, "File Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                    container.$context().startActivityForResult(chooserIntent, getResultCode());
                } catch (Exception e) {
                    Toast.makeText(container.$context(), "Camera Exception:" + e, Toast.LENGTH_LONG).show();
                }

            }

            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // openFileChooser for other Android versions
            /* may not work on KitKat due to lack of implementation of openFileChooser() or onShowFileChooser()
               https://code.google.com/p/android/issues/detail?id=62220
               however newer versions of KitKat fixed it on some devices */
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

        });

    }

    // return here when file selected from camera or from SD Card
    @Override
    public void resultReturned(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "resultReturned invoked 1: resultCode:" + resultCode);

        // code for all versions except of Lollipop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode == getResultCode()) {
                if (null == this.mUploadMessage) {
                    return;
                }

                Uri result = null;

                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(container.$context(), "activity :" + e, Toast.LENGTH_LONG).show();
                }

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }

        } // end of code for all versions except of Lollipop

        // start of code for Lollipop only
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != resultcode || mFilePathCallback == null) {
                container.$form().onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // check that the response is a good one
            if (resultCode == RESULT_OK) {
                if (data == null || data.getData() == null) {
                    // if there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        } // end of code for Lollipop only

    }

    private int getResultCode() {
        if (resultcode == 0) {
            resultcode = container.$form().registerForActivityResult(this);
        }
        return resultcode;
    }
    /**
     * Gets the web view string
     *
     * @return string
     */
    @SimpleProperty(description = "Gets the WebView's String, which is viewable through " +
            "Javascript in the WebView as the window.AppInventor object",
            category = PropertyCategory.BEHAVIOR)
    public String WebViewString() {
        return wvInterface.getWebViewString();
    }

    /**
     * Sets the web view string
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public void WebViewString(String newString) {
        wvInterface.setWebViewString(newString);
    }

    @Override
    public View getView() {
        return webview;
    }


    // Create a class so we can override the default link following behavior.
    // The handler doesn't do anything on its own.  But returning true means that
    // this do nothing will override the default WebVew behavior.  Returning
    // false means to let the WebView handle the Url.  In other words, returning
    // true will not follow the link, and returning false will follow the link.
    private class WebViewerClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return !followLinks;
        }
    }

    // Components don't normally override Width and Height, but we do it here so that
    // the automatic width and height will be fill parent.
    @Override
    @SimpleProperty()
    public void Width(int width) {
        if (width == LENGTH_PREFERRED) {
            width = LENGTH_FILL_PARENT;
        }
        super.Width(width);
    }

    @Override
    @SimpleProperty()
    public void Height(int height) {
        if (height == LENGTH_PREFERRED) {
            height = LENGTH_FILL_PARENT;
        }
        super.Height(height);
    }


    /**
     * Returns the URL of the page the WebVewier should load
     *
     * @return URL of the page the WebVewier should load
     */
    @SimpleProperty(
            description = "URL of the page the WebViewer should initially open to.  " +
                    "Setting this will load the page.",
            category = PropertyCategory.BEHAVIOR)
    public String HomeUrl() {
        return homeUrl;
    }

    /**
     * Specifies the URL of the page the WebVewier should load
     *
     * @param url URL of the page the WebVewier should load
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
            defaultValue = "")
    @SimpleProperty()
    public void HomeUrl(String url) {
        homeUrl = url;
        // clear the history, since changing Home is a kind of reset
    webview.clearHistory();
        webview.loadUrl(homeUrl);
    }

    /**
     * Returns the URL currently being viewed
     *
     * @return URL of the page being viewed
     */
    @SimpleProperty(
            description = "URL of the page currently viewed.   This could be different from the " +
                    "Home URL if new pages were visited by following links.",
            category = PropertyCategory.BEHAVIOR)
    public String CurrentUrl() {
        return (webview.getUrl() == null) ? "" : webview.getUrl();
    }

    /**
     * Returns the title of the page currently being viewed
     *
     * @return title of the page being viewed
     */
    @SimpleProperty(
            description = "Title of the page currently viewed",
            category = PropertyCategory.BEHAVIOR)
    public String CurrentPageTitle() {
        return (webview.getTitle() == null) ? "" : webview.getTitle();
    }


  /** Indicates whether to follow links when they are tapped in the WebViewer
     * @return true or false
     */
    @SimpleProperty(
            description = "Determines whether to follow links when they are tapped in the WebViewer.  " +
                    "If you follow links, you can use GoBack and GoForward to navigate the browser history. ",
            category = PropertyCategory.BEHAVIOR)
    public boolean FollowLinks() {
        return followLinks;
    }


  /** Determines whether to follow links when they are tapped
     *
     * @param follow
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty()
    public void FollowLinks(boolean follow) {
        followLinks = follow;
    resetWebViewClient();
    }

    /**
     * Determines whether SSL Errors are ignored. Set to true to use self signed certificates
     *
     * @return true or false
   *
     */
    @SimpleProperty(
            description = "Determine whether or not to ignore SSL errors. Set to true to ignore " +
                    "errors. Use this to accept self signed certificates from websites.",
            category = PropertyCategory.BEHAVIOR)
    public boolean IgnoreSslErrors() {
        return ignoreSslErrors;
    }

    /**
     * Determines whether or not to ignore SSL Errors
     *
     * @param ignoreSslErrors set to true to ignore SSL errors
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty()
    public void IgnoreSslErrors(boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    resetWebViewClient();
    }

    /**
     * Loads the  page from the home URL.  This happens automatically when
     * home URL is changed.
     */
    @SimpleFunction(
            description = "Loads the home URL page.  This happens automatically when " +
                    "the home URL is changed.")
    public void GoHome() {
        webview.loadUrl(homeUrl);
    }

    /**
     * Go back to the previously viewed page.
     */
    @SimpleFunction(
            description = "Go back to the previous page in the history list.  " +
                    "Does nothing if there is no previous page.")
    public void GoBack() {
        if (webview.canGoBack()) {
            webview.goBack();
        }
    }

    /**
     * Go forward in the history list
     */
    @SimpleFunction(
            description = "Go forward to the next page in the history list.   " +
                    "Does nothing if there is no next page.")
    public void GoForward() {
        if (webview.canGoForward()) {
            webview.goForward();
        }
    }

    /**
     * @return true if the WebViewer can go forward in the history list
     */
    @SimpleFunction(
            description = "Returns true if the WebViewer can go forward in the history list.")
    public boolean CanGoForward() {
        return webview.canGoForward();
    }


    /**
     * @return true if the WebViewer can go back in the history list
     */
    @SimpleFunction(
            description = "Returns true if the WebViewer can go back in the history list.")
    public boolean CanGoBack() {
        return webview.canGoBack();
    }

   @SimpleFunction(description = "Load complete static html in webviewer")
   public void LoadHtml(String htmlContent) {
        webview.loadData(htmlContent, "text/html", "UTF-8");
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "HTML can be used to upload files directly from device storage. If AllowCamer is enabled, " +
            "then you can also take picture and upload the image. NOTE: For this, you'll require to add CAMERA component")
    public void AllowCamera(boolean enabled) {
        this.cameraEnabled = enabled;
    }


    /**
     * Load the given URL
     */
    @SimpleFunction(description = "Load the page at the given URL.")
    public void GoToUrl(String url) {
        webview.loadUrl(url);
    }


    /**
     * Specifies whether or not this WebViewer can access the JavaScript
     * Location API.
     *
     * @param uses -- Whether or not the API is available
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty(userVisible = false,
            description = "Whether or not to give the application permission to use the Javascript geolocation API. " +
                    "This property is available only in the designer.")
    public void UsesLocation(boolean uses) {
        // We don't actually do anything here (the work is in the MockWebViewer)
    }

    /**
     * Determine if the user should be prompted for permission to use the geolocation API while in
     * the webviewer.
     *
     * @return true if prompting is  required.  False assumes permission is granted.
     */

    @SimpleProperty(description = "If True, then prompt the user of the WebView to give permission to access the geolocation API. " +
            "If False, then assume permission is granted.")
    public boolean PromptforPermission() {
        return prompt;
    }

    /**
     * Determine if the user should be prompted for permission to use the geolocation API while in
     * the webviewer.
     *
     * @param prompt set to true to require prompting. False assumes permission is granted.
     */

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty(userVisible = true)
    public void PromptforPermission(boolean prompt) {
        this.prompt = prompt;
    }

    /**
     * Clear Stored Location permissions. When the geolocation API is used in
     * the WebViewer, the end user is prompted on a per URL basis for whether
     * or not permission should be granted to access their location. This
     * function clears this information for all locations.
     * <p>
     * As the permissions interface is not available on phones older then
     * Eclair, this function is a no-op on older phones.
     */

    @SimpleFunction(description = "Clear stored location permissions.")
    public void ClearLocations() {
        if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR)
            EclairUtil.clearWebViewGeoLoc();
    }

    private void resetWebViewClient() {
        /*if (SdkLevel.getLevel() >= SdkLevel.LEVEL_FROYO) {
            webview.setWebViewClient(FroyoUtil.getWebViewClient(ignoreSslErrors, followLinks, container.$form(), this));
        } else {
            webview.setWebViewClient(new WebViewerClient());
        }*/

        setupClient();
    }

    /**
     * Clear the webview cache, both ram and disk. This is useful
     * when using the webviewer to poll a page that may not be sending
     * appropriate cache control headers. This is particularly useful
     * when using the webviwer to look at a Fusion Table.
     */

    @SimpleFunction(description = "Clear WebView caches.")
    public void ClearCaches() {
        webview.clearCache(true);
    }

    /**
     * Specifies whether or not we want Zoom: If true add Zoom, if false removed the Zoom
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Enables or disables the ability to have zoom on page")
    public void ZoomEnabled(boolean enabled) {
        this.zoomEnabled = enabled;
        webview.getSettings().setBuiltInZoomControls(enabled);
    }

    @SimpleProperty(description = "Enables or disables the ability to have zoom on page", category = PropertyCategory.BEHAVIOR)
    public boolean ZoomEnabled() {
        return this.zoomEnabled;
    }

    /**
     * Allows the setting of properties to be monitored from the javascript
     * in the WebView
     */
    public class WebViewInterface {
        Context mContext;
        String webViewString;

    /** Instantiate the interface and set the context */
        WebViewInterface(Context c) {
            mContext = c;
            webViewString = " ";
        }

        /**
         * Gets the web view string
         *
         * @return string
         */
        @JavascriptInterface
        public String getWebViewString() {
            return webViewString;
        }

        /**
         * Sets the web view string
         */
        @JavascriptInterface
        public void setWebViewString(String newString) {
            webViewString = newString;
        }

    }

    private class MyAppWebViewClient extends WebViewClient {

        private static final String LOG_TAG = "MyAppWebViewClient";

        // variable for onReceivedError
        private boolean refreshed;

        // handling external links as intents
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(LOG_TAG, "shouldOverrideUrlLoading: " + url);
            return !followLinks;
//
//            String host = Uri.parse(url).getHost();
//            if( host.endsWith("facebook.com") ) {
//                Log.d(LOG_TAG, "fb called returning: " + url);
//                return false;
//            }
//
//            Log.d(LOG_TAG, "url OTHER than fb: " + url);
////            return !followLinks;
//            try {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                view.getContext().startActivity(intent);
//            } catch (Exception e) {
//                Log.d(LOG_TAG, "Unable to create Image File: " + e.getMessage());
//            }
//
//            // Returning false means to let the WebView handle the Url.
////            return false;
//            return !followLinks;
        }

        // refresh on connection error (sometimes there is an error even when there is a network connection)
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if(!refreshed) {
                view.loadUrl(failingUrl);
                // when network error is real do not reload url again
                refreshed = true;
            }
        }

    }
}

