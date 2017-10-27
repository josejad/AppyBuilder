package com.google.appinventor.components.runtime;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyAppWebViewClient extends WebViewClient {

    private static final String LOG_TAG = "MyAppWebViewClient";

    // variable for onReceivedError
    private boolean refreshed;

    // handling external links as intents
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d(LOG_TAG, "shouldOverrideUrlLoading: " + url);

        String host = Uri.parse(url).getHost();
        if( host.endsWith("facebook.com") ) {
            Log.d(LOG_TAG, "fb called returning: " + url);
            return false;
        }

        Log.d(LOG_TAG, "url OTHER than fb: " + url);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Unable to create Image File: " + e.getMessage());
        }
        return true;
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
