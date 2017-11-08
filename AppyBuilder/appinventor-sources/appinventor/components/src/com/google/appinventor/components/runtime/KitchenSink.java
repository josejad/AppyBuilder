// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Contains various helper methods
 *
 * @author M. Hossein Amerkashi ( kkashi01@gmail.com )
 */
@DesignerComponent(version = YaVersion.KITCHENSINK_COMPONENT_VERSION,
        description = "Non-visible component that is used for updating default values in the " +
                "androidmanifest.xml. The androidmanifest.xml contains all the details needed by the android system" +
                " about the app. ",
        category = ComponentCategory.ADVANCED,
        nonVisible = true,
        iconName = "images/kitchensink.png")
@UsesLibraries(libraries = "metadata-extractor-2.8.1.jar")
@SimpleObject
public class KitchenSink extends AndroidNonvisibleComponent implements Component {
    private static final String LOG_TAG = "KitchenSink";
    private boolean notificationBarVisible = true;
    private boolean titleBarVisible = true;
    private boolean screenOnEnabled = false;
    private final Context context;
    private String deviceKey="";
    //private static final String TAG = KitchenSink.class.getCanonicalName();
    //private static final String processId = Integer.toString(android.os.Process.myPid());
    /**
     * Creates a new KitchenSink component.
     *
     * @param container the Form that this component is contained in.
     */
    public KitchenSink(ComponentContainer container) {
        super(container.$form());
        context = (Context) container.$context();
    }


    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty
    public void NotificationBarVisible(final boolean visible) {
        if (visible) {
            form.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            form.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            form.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            form.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        this.notificationBarVisible = visible;
    }

    @SimpleProperty
    public boolean NotificationBarVisible() {
        return notificationBarVisible;
    }

//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
//    @SimpleProperty
//    public void TitleBarVisible(final boolean visible) {
//        try {
//            if (visible) {
//                ((View)form.findViewById(android.R.id.title).getParent()).setVisibility(View.VISIBLE);
//            } else {
//                ((View)form.findViewById(android.R.id.title).getParent()).setVisibility(View.GONE);
//            }
//
//            this.titleBarVisible = visible;
//        } catch (Exception e) {
//            //no-op -- ActionBar introduced in API 11.
//        }
//    }
//
////    @SimpleProperty
//    public boolean TitleBarVisible() {
//        return titleBarVisible;
//    }



    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void KeepScreenOn(final boolean enabled) {
        if (enabled) {
            form.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            form.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        this.screenOnEnabled = enabled;
    }

    @SimpleProperty
    public boolean KeepScreenOn() {
        return screenOnEnabled;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty
    public void HideSoftKeyboard(boolean enabled)
    {
        //input.setRawInputType(Configuration.KEYBOARD_12KEY);  this will only show numbers. How switch back &forth
        // to alpha? Also needs update for handling existing apps -- todo: setup in next release
        InputMethodManager imm = (InputMethodManager) form.getSystemService(INPUT_METHOD_SERVICE);

        if (enabled) {
            form.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            imm.hideSoftInputFromWindow(form.getWindow().getDecorView().getWindowToken(), 0);
        } else {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @SimpleProperty(description = "Returns true if soft keyboard is open else false")
    public boolean IsKeybaordOpen() {
        InputMethodManager imm = (InputMethodManager) form.getSystemService(INPUT_METHOD_SERVICE);
        return imm.isAcceptingText();
    }

    @SimpleProperty(description = "Checks to see if network is available or not; i.e. is device connected to network")
    public boolean IsNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) form.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @SimpleProperty(description = "Checks to see if device is GPS enabled")
    public boolean IsGPSEnabledDevice()
    {
        final LocationManager mgr = (LocationManager) form.getSystemService(Context.LOCATION_SERVICE);
        if ( mgr == null ) return false;
        final List<String> providers = mgr.getAllProviders();
        return providers != null && providers.contains(LocationManager.GPS_PROVIDER);
    }

    @SimpleProperty(description = "Checks to see if device is GPS enabled and if so, checks to see if GPS is started or not")
    public boolean IsGPSEnabled()
    {
        if (!IsGPSEnabledDevice()) {
            return false;
        }

        //http://code.google.com/p/krvarma-android-samples/source/browse/trunk/CheckGPS/src/com/varma/samples/checkgps/MainActivity.java
        final LocationManager manager = (LocationManager) form.getSystemService(Context.LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @SimpleProperty(description = "Starts up the GPS configuration activity, giving user option to turn turn on the GPS")
    public boolean StartGPSOptions()
    {
        //NOTE: When method has no arguments, the annotation processor wants us to return a type and cannot be void;
        // hence, for now, using boolean
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        form.startActivity(intent);
        //TODO: Should we do startActivityForResult so thta we could check status of user's gps selection?
//        startActivityForResult(intent, REQUEST_CODE);
        //TODO: we can now implement method such as:
        /*
        @Override
                protected void onActivityResult(int requestCode, int resultCode, Intent data)
                {
                        super.onActivityResult(requestCode, resultCode, data);
                        if(requestCode == REQUEST_CODE)
                        {
                            displayGPSState(isGPSenabled());
                        }
                }

         */

        return true;
    }

    /**
     * The Copy method
     * http://stackoverflow.com/a/19253877/1545993 and http://stackoverflow.com/a/28780585/1545993
     *
     * @param text =  the text to copy
     */
    @SimpleFunction(description = "Copy text to clipboard. Returns true if successfully copied else false")
    public boolean CopyToClipboard(String text) {
        try {
            if (SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied text", text);
                clipboard.setPrimaryClip(clip);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error copying to clipboard. Error", e);
            return false;
        }
        return true;
    }

    @SimpleFunction(description = "Paste text from clipboard.")
    public String PasteFromClipboard() {
        if (SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            return clipboard.getText().toString();
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

            // Gets the clipboard data from the clipboard
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null) {
                // Gets the first item from the clipboard data
                ClipData.Item item = clip.getItemAt(0);

                // If the contents of the clipboard wasn't a reference to a note, then this converts whatever it is to text.
                return coerceToText(item).toString();
}
        }

        return "";
    }

    private CharSequence coerceToText(ClipData.Item item) {
        // If this Item has an explicit textual value, simply return that.
        CharSequence text = item.getText();
        if (text != null) {
            return text;
        }

        // If this Item has a URI value, try using that.
        Uri uri = item.getUri();
        if (uri != null) {
            // First see if the URI can be opened as a plain text stream (of any sub-type). If so, this is the best textual representation for it.
            FileInputStream stream = null;
            try {
                // Ask for a stream of the desired type.
                AssetFileDescriptor descr = context.getContentResolver().openTypedAssetFileDescriptor(uri, "text/*", null);
                stream = descr.createInputStream();
                InputStreamReader reader = new InputStreamReader(stream, "UTF-8");

                // Got it... copy the stream into a local string and return it.
                StringBuilder builder = new StringBuilder(128);
                char[] buffer = new char[8192];
                int len;
                while ((len = reader.read(buffer)) > 0) {
                    builder.append(buffer, 0, len);
                }
                return builder.toString();
            } catch (FileNotFoundException e) {
                // Unable to open content URI as text... not really an error, just something to ignore.
                Log.d(LOG_TAG, "Unable to open content URI as text, ignoring... " + e.getMessage(), e);
            } catch (IOException e) {
                // Something bad has happened.
                Log.w(LOG_TAG, "Failure loading text", e);
                return e.toString();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            }

            // If we couldn't open the URI as a stream, then the URI itself probably serves fairly well as a textual representation.
            Log.d(LOG_TAG, "Couldn't open the URI as a stream, then the URI itself probably serves fairly well as a textual representation");
            return uri.toString();
        }

        // Finally, if all we have is an Intent, then we can just turn that into text. Not the most user-friendly thing, but it's something.
        Intent intent = item.getIntent();
        if (intent != null) {
            Log.d(LOG_TAG, "all we have is an Intent, then we can just turn that into text");
            return intent.toUri(Intent.URI_INTENT_SCHEME);
        }

        // Shouldn't get here, but just in case...
        return "";
    }

    // To read from asset:
    //    before packaging (dev path): file:///mnt/sdcard/AppInventor/assets/kitty.png
    //    after packaging (prod path): file:///android_asset/kitty.png

    @SimpleFunction(description = "Returns a sorted list of image meta data. If valid key is passed in, " +
            "it will return meta data for the passed in key. ")
    public YailList GetImageMetaData(String imagePath, String key) {
        imagePath = imagePath.trim();
        if (!imagePath.toLowerCase().startsWith("file:")) {
           imagePath = "file:" + imagePath;
        }
        key=key.toUpperCase().trim();
        List<String> aList = new ArrayList<String>();
        try {
            InputStream is = new URL(imagePath).openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            Metadata metadata = ImageMetadataReader.readMetadata(bis);

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (key.equals("")) {
                        aList.add(tag.getTagName() + "="+tag.getDescription());
                    } else {
                        if (tag.getTagName().toUpperCase().contains(key)) {
                            aList.add(tag.getTagName() + "="+tag.getDescription());
                        }
                    }
                }
            }
        }
        catch (Exception e){
            Log.e(LOG_TAG, "Image Exception: " + e.getMessage());
            aList.add(0, "ERROR=" + e.getMessage());
        }

        Collections.sort(aList);
        return YailList.makeList(aList);
    }

    // http://stackoverflow.com/questions/27957300/read-logcat-programmatically-for-an-application
 /*   @SimpleFunction(description = "Reads the last n number of log lines from Logcat for debugging the application. " +
            "If numberOfLines <= 0, then default of 500 will be used. use logLevels of V, D, I, W, E. Default is All logs")
    public YailList GetLogData(int numberOfLines, String logLevel) {
        if (numberOfLines <=0) numberOfLines = 500;

        logLevel = logLevel.toUpperCase().trim();
        if (logLevel.equals("V") || logLevel.equals("D") || logLevel.equals("I") ||logLevel.equals("W") ||logLevel.equals("E") ) {
            logLevel = "*:" + logLevel;
        } else {
            logLevel="*:*";
        }
//        switch (logLevel) {
//            case "E":
//            case "I":
//            case "D":
//            case "W":
//            case "V":
//                logLevel = "*:" + logLevel;
//                break;
//            default:
//                logLevel="*:*";
//        }
        List<String> aList = new ArrayList<String>();

        final String TAG = KitchenSink.class.getCanonicalName();
        StringBuilder builder = new StringBuilder();
        String processId = Integer.toString(android.os.Process.myPid());

        try {
            String[] command = new String[] {"logcat", "-t", "" + numberOfLines + "", "-v", "time", logLevel};

            Process process = Runtime.getRuntime().exec(command);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null){
                aList.add(line + "\r\n");
//                if(line.contains(processId)) {
//                    aList.add(line + "\r\n");
////                    Log.i(LOG_TAG, "Line is: " + line);
//                }
            }
        }
        catch (IOException ex) {
            Log.e(TAG, "GetLogData failed", ex);
//            builder.append(ex.getMessage());
            aList.add(0, "ERROR\r\n" + ex.getMessage());

        }
            return YailList.makeList(aList);
//        return builder.toString();

    }*/
}



