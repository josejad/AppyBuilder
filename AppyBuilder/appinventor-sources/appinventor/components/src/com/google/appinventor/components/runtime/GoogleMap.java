// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;
// https://github.com/mit-dig/punya/blob/master/appinventor/components/src/com/google/appinventor/components/runtime/GoogleMap.java
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.*;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.gson.*;
import gnu.math.DFloNum;
import gnu.math.IntNum;

/* Component for displaying information on Google Map
 * This component makes use of Android MapView (v2) to location specific information.
 * AppyBuilder user could use this component to do things like those demo apps
 * for Google Mapview in the android sdk
 *
 * @author fuming@mit.mit (Fuming Shih)
 * @author wli17@mit.edu (Weihua Li)
 */
@DesignerComponent(version = YaVersion.GOOGLE_MAP_COMPONENT_VERSION,
        description = "Visible component that show information on Google map.",
        category = ComponentCategory.VISUALIZATION)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, "
        + "android.permission.ACCESS_NETWORK_STATE, "
        + "android.permission.INTERNET, "
        + "android.permission.ACCESS_COARSE_LOCATION, "
        + "android.permission.ACCESS_FINE_LOCATION, "
        + "com.google.android.providers.gsf.permission.READ_GSERVICES, "
        + "android.permission.WRITE_EXTERNAL_STORAGE")

// funf.jar uses gson.JsonParser, android support needed for newest google play sdk
@UsesLibraries(libraries = "google-play-services.jar,gson-2.1.jar,android-support-v4.jar")
public class GoogleMap extends AndroidViewComponent implements OnResumeListener, OnInitializeListener, OnPauseListener,
        OnMarkerClickListener, OnInfoWindowClickListener, OnMarkerDragListener, OnMapClickListener,
        OnMapLongClickListener, OnCameraChangeListener, LocationListener
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
    , OnMapReadyCallback
{
    private final Activity context;
    private final Form form;
    private static final String TAG = "GoogleMap";
    private GoogleMap googleMap;
    private android.widget.LinearLayout viewLayout;
    private GoogleApiClient mGoogleApiClient;

    // translates AppyBuilder alignment codes to Android gravity
    // private final AlignmentUtil alignmentSetter;
    private final String MAP_FRAGMENT_TAG;
    private com.google.android.gms.maps.GoogleMap mMap;
    private MapFragment mMapFragment;
    private Bundle savedInstanceState;
    private HashMap<Marker, Integer> markers = new HashMap<Marker, Integer>();

    // basic configurations of a map
    private int mapType = com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
    private boolean myLocationEnabled = false;
    private boolean compassEnabled = true;
    private boolean rotateEnabled = true;
    private boolean scrollEnabled = true;
    private boolean zoomControlEnabled = true;
    private boolean zoomGesturesEnabled = true;

    // default settings for marker
    private int mMarkerColor =  Component.COLOR_BLUE;
    private boolean mMarkerDraggable = false;

    // settings for map event listener
    private boolean enableMapClickListener = false;
    private boolean enableMapLongClickListener = false;
    private boolean enableCameraChangeListener = false;

    // setting up for circle overlay
    private static final double DEFAULT_RADIUS = 1000000;
    public static final double RADIUS_OF_EARTH_METERS = 6371009;
    private static final AtomicInteger snextCircleId = new AtomicInteger(1);
    private HashMap<Object, Integer> circles = new HashMap<Object, Integer>(); //we are storing references for both circle and draggable circle
    private List<DraggableCircle> mCircles = new ArrayList<DraggableCircle>(1);

    private HashMap<Polygon, Integer> polygons = new HashMap<Polygon, Integer>();

    //defaults for circle overlay
    private float mStrokeWidth = 2; // in pixel, 0 means no outline will be drawn
    private int mStrokeColor = Color.BLACK;  // perimeter default color is black
    private int mColorHue = 0 ; // value ranges from [0, 360]
    private int mAlpha = 20; //min 0, default 127, max 255
    private int mFillColor =  Color.HSVToColor(mAlpha, new float[] {mColorHue, 1, 1});//default to red, medium level hue color


    private UiSettings mUiSettings;

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static final AtomicInteger snextMarkerId = new AtomicInteger(1);
    private final Handler androidUIHandler = new Handler();

    private YailList markersList;

    // Setting for LocationClient
    // These settings are the same as the settings for the map. They will in fact give you updates at
    // the maximal rates currently possible.
//    private LocationClient mLocationClient;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private boolean isMapReady = false;

    public GoogleMap(ComponentContainer container) throws IOException {
        super(container);
        Log.i(TAG, "In the constructor of GoogleMap");
        context = container.$context();
        form = container.$form();
        savedInstanceState = form.getOnCreateBundle();
        Log.i(TAG, "savedInstanceState in GM: " + savedInstanceState);
        this.mGoogleApiClient = new GoogleApiClient.Builder(this.context, this, this)
                .addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
//        Log.i(TAG, "mGoogleApiClient is " + mGoogleApiClient);

        // try raw mapView with in the fragmment
        viewLayout = new android.widget.LinearLayout(context);
//        int viewId = ViewUtil.generateViewId();
        int viewId = generateViewId();
        viewLayout.setId(viewId);
//        Log.i(TAG, "viewlayout id was set to:" + viewId);

        MAP_FRAGMENT_TAG = "map_" + System.currentTimeMillis();
        Log.i(TAG, "map_tag:" + MAP_FRAGMENT_TAG);
        //add check if the phone has installed Google Map and Google Play Service sdk

        checkGooglePlayServiceSDK() ;
        checkGoogleMapInstalled() ;

        // TODO: need to add code to check Form (activity) whether savedInstanceState ==null
        mMapFragment = (MapFragment) form.getFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);


        // We only create a fragment if it doesn't already exist.
        if (mMapFragment == null) {


            Log.i(TAG, "mMapFragment is null.");
            // To programmatically add the map, we first create a SupportMapFragment.
            mMapFragment = MapFragment.newInstance();

            //mMapFragment = new SomeFragment();
            android.app.FragmentTransaction fragmentTransaction =
                    form.getFragmentManager().beginTransaction();
            Log.i(TAG, "here before adding fragment");
            // try to use replace to see if we solve the issue
            fragmentTransaction.replace(viewLayout.getId(), mMapFragment, MAP_FRAGMENT_TAG);

            fragmentTransaction.commit();
        }

        setUpMapIfNeeded();
        container.$add(this);

        Width(LENGTH_FILL_PARENT);
        Height(LENGTH_FILL_PARENT);
        form.registerForOnInitialize(this);
        form.registerForOnResume(this);
        form.registerForOnPause(this);
    }


    /**
     * Generate a value suitable for use in .
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    private static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }


    //  Currently this doesn't work
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            isMapReady = false;
            Log.i(TAG, "setUpMapIfNeeded. mMap is null");

            // Try to obtain the map from the SupportMapFragment.
            mMapFragment.getMapAsync(this);
            //xxxxxxxxxxxxxxxxxxx
            // Check if we were successful in obtaining the map.
            /*if (mMap != null) {
                Log.i(TAG, "Yes, we have a google map...");
                setUpMap();
            } else {
                Log.i(TAG, "setUpMapIfNeeded. mMap is still null - in else part");

                // means that Google Service is not available
                form.dispatchErrorOccurredEvent(this, "setUpMapIfNeeded",
                        ErrorMessages.ERROR_GOOGLE_PLAY_NOT_INSTALLED);
            }*/

        } else {
            isMapReady = true;
        }
    }


    @Override
    public void onMapReady(com.google.android.gms.maps.GoogleMap googleMap) {
        Log.i(TAG, "Yes, we have a google map...");
        isMapReady = true;
        mMap = googleMap;
        setUpMap();
    }


    private void setUpLocationClientIfNeeded() {
        if (mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(this.context, this, this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
    }



    private void setUpMap() {
        // could be the boilerplate for initiating everything
        // including all the configurations and markers

        // (testing: add an marker)
        Log.i(TAG, "in setUpMap()");
        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);

        // create UiSetting instance and default ui settings of the map
        setupUiSettings();

        // after this method is called, user can add markers and change other settings.
        //TODO: Actions(Functions) that are called within MapIsReady() are not working
        MapIsReady();

    }

    private void setupUiSettings() {
        if (mUiSettings == null && mMap != null) {
            mUiSettings = mMap.getUiSettings();
            mUiSettings.setCompassEnabled(this.compassEnabled);
            mUiSettings.setRotateGesturesEnabled(this.rotateEnabled);
            mUiSettings.setScrollGesturesEnabled(this.scrollEnabled);
            mUiSettings.setZoomControlsEnabled(this.zoomControlEnabled);
            mUiSettings.setZoomGesturesEnabled(this.zoomGesturesEnabled);
        }

    }
    @SimpleFunction(description = "Enables/disables the compass widget on the map's ui. Call this only after " +
            "event \"MapIsReady\" is received")
    public void EnableCompass(boolean enable) {
        if (!isMapReady) return;
        if (mUiSettings == null) setupUiSettings();
        this.compassEnabled = enable;
        mUiSettings.setCompassEnabled(enable);
    }

    @SimpleProperty(description = "Indicates whether the compass widget is currently enabled in the map ui")
    public boolean CompassEnabled() {
        return mUiSettings.isCompassEnabled();
    }

    @SimpleFunction(description = "Enables/disables the capability to rotate a map on the ui. Call this only after " +
            "the event \"MapIsReady\" is received.")
    public void EnableRotate(boolean enable) {
        if (!isMapReady) return;
        if (mUiSettings == null) setupUiSettings();
        this.rotateEnabled = enable;
        mUiSettings.setRotateGesturesEnabled(enable);
    }

    @SimpleProperty(description = "Indicates whether the capability to rotate a map on the ui is currently enabled")
    public boolean RotateEnabled() {
        return mUiSettings.isRotateGesturesEnabled();
    }

    @SimpleFunction(description = "Enables/disables the capability to scroll a map on the ui. Call this only after the " +
            "event \"MapIsReady\" is received")
    public void EnableScroll(boolean enable) {
        if (!isMapReady) return;
        if (mUiSettings == null) setupUiSettings();
        this.scrollEnabled = enable;
        mUiSettings.setScrollGesturesEnabled(enable);

    }

    @SimpleProperty(description = "Indicates whether the capability to scroll a map on the ui is currently enabled")
    public boolean ScrollEnabled() {
        return mUiSettings.isScrollGesturesEnabled();
    }

    @SimpleFunction(description = "Enables/disables the zoom widget on the map's ui. Call this only after the event" +
            " \"MapIsReady\" is received")
    public void EnableZoomControl(boolean enable) {
        if (mUiSettings == null) setupUiSettings();
        this.zoomControlEnabled = enable;
        mUiSettings.setZoomControlsEnabled(enable);

    }

    @SimpleProperty(description = "Indicates whether the zoom widget on the map ui is currently enabled")
    public boolean ZoomControlEnabled() {
        return mUiSettings.isZoomControlsEnabled();
    }

    @SimpleFunction(description = "Enables/disables zoom gesture on the map ui. Call this only after the event " +
            " \"MapIsReady\" is received. ")
    public void EnableZoomGesture(boolean enable) {
        if (!isMapReady) return;
        if (mUiSettings == null) setupUiSettings();
        this.zoomGesturesEnabled = enable;
        mUiSettings.setZoomGesturesEnabled(enable);

    }

    @SimpleProperty(description = "Indicates whether the zoom gesture is currently enabled")
    public boolean ZoomGestureEnabled() {
        return mUiSettings.isZoomGesturesEnabled();
    }

    @SimpleEvent(description = "Indicates that the map has been rendered and ready for adding markers " +
            "or changing other settings. Please add or updating markers within this event")
    public void MapIsReady(){
        Log.i(TAG, "Map is ready for adding markers and other setting");
        this.isMapReady = true;
        EventDispatcher.dispatchEvent(GoogleMap.this, "MapIsReady");
    }

    //TODO: Move this to Util
    private void checkGooglePlayServiceSDK() {
        //To change body of created methods use File | Settings | File Templates.
        final int googlePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        Log.i(TAG, "googlePlayServicesAvailable:" + googlePlayServicesAvailable);

        switch (googlePlayServicesAvailable) {
            case ConnectionResult.SERVICE_MISSING:
                form.dispatchErrorOccurredEvent(this, "checkGooglePlayServiceSDK",
                        ErrorMessages.ERROR_GOOGLE_PLAY_NOT_INSTALLED);
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                form.dispatchErrorOccurredEvent(this, "checkGooglePlayServiceSDK",
                        ErrorMessages.ERROR_GOOGLE_PLAY_SERVICE_UPDATE_REQUIRED);
                break;
            case ConnectionResult.SERVICE_DISABLED:
                form.dispatchErrorOccurredEvent(this, "checkGooglePlayServiceSDK",
                        ErrorMessages.ERROR_GOOGLE_PLAY_DISABLED);
                break;
            case ConnectionResult.SERVICE_INVALID:
                form.dispatchErrorOccurredEvent(this, "checkGooglePlayServiceSDK",
                        ErrorMessages.ERROR_GOOGLE_PLAY_INVALID);
                break;
        }
    }

    private void checkGoogleMapInstalled() {
        try
        {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );

        }
        catch(PackageManager.NameNotFoundException e)
        {
            form.dispatchErrorOccurredEvent(this, "checkGoogleMapInstalled",
                    ErrorMessages.ERROR_GOOGLE_MAP_NOT_INSTALLED);
        }
    }

    /**
     *
     * @param lat Latitude of the center of the circle
     * @param lng Longitude of the center of the circle
     * @param radius Radius of the circle
     * @param alpha Alpha value of the color of the circle overlay
     * @param hue Hue value of the color of the circle overaly
     * @param strokeWidth Width of the perimeter
     * @param strokeColor Color of the perimeter
     */
    @SimpleFunction(description = "Create a circle overlay on the map UI with specified latitude and longitude for center. " +
            "\"hue\" (min 0, max 360) and \"alpha\" (min 0, max 255) are used to set color and transparency level of the circle, "  +
            "\"strokeWidth\" and \"strokeColor\" are for the perimeter of the circle. " +
            "Returning a unique id of the circle for future reference to events raised by moving this circle. If the circle is" +
            "set to be draggable, two default markers will appear on the map: one in the center of the circle, another on the perimeter.")
    public int AddCircle(double lat, double lng, double radius, int alpha, float hue, float strokeWidth, int strokeColor, boolean draggable){
        int uid = generateCircleId();
        int fillColor =  Color.HSVToColor(alpha, new float[] {hue, 1, 1});

        if (draggable) {
            //create a draggableCircle
            DraggableCircle circle = new DraggableCircle(new LatLng(lat, lng), radius, strokeWidth, strokeColor, fillColor);

            mCircles.add(circle);
            circles.put(circle,uid);

        }
        else {
            Circle plainCircle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(lat, lng))
                    .radius(radius)
                    .strokeWidth(strokeWidth)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor));
            circles.put(plainCircle, uid);
        }

        return uid;

    }


    private Object getCircleIfExisted(int circleId){
        Object circle = getKeyByValue(circles, circleId);

        if(circle == null){
            form.dispatchErrorOccurredEvent(this, "getCircleIfExisted",
                    ErrorMessages.ERROR_GOOGLE_MAP_CIRCLE_NOT_EXIST, Integer.toString(circleId));
            return null;
        }
        return circle;
    }

    @SimpleFunction(description = "Remove a circle for the map. Returns true if successfully removed, false if the circle " +
            "does not exist with the specified id")
    public boolean RemoveCircle(int circleId){

        Object circle = getKeyByValue(circles, circleId);
        boolean isRemoved = false;

        if (circle == null) {
            //TODO: do we need another error Message?
            return isRemoved;
        } else {
            if (circle instanceof DraggableCircle){// if it's a draggable circle
                ((DraggableCircle) circle).removeFromMap(); // remove all it's inner objects from the map
                mCircles.remove(circle); //need to remove it from the mCircles arraylist

            }
            if (circle instanceof Circle) { // it's a plain circle, just remove it from the hashmap and the map
                ((Circle) circle).remove();

            }
            circles.remove(circle);
            isRemoved = true;
        }

        return isRemoved;
    }

    @SimpleFunction(description = "Set the property of an existing circle. Properties include: " +
            "\"alpha\"(number, value ranging from 0~255), \"color\" (nimber, hue value ranging 0~360), " +
            "\"radius\"(number in meters)")
    public void UpdateCircle(int circleId, String propertyName, Object value){
        Log.i(TAG, "inputs: " + circleId + "," + propertyName + ", " + value);
        float [] hsv = new float[3];
        Object circle = getCircleIfExisted(circleId);  // if it's null, getCircleIfExisted will show error msg
        Circle updateCircle = null; // the real circle content that gets updated

        if (circle != null) {
            if (circle instanceof DraggableCircle) {
                updateCircle = ((DraggableCircle) circle).getCircle();

            }
            if (circle instanceof Circle) {
                updateCircle = (Circle) circle;

            }
            try {

                Float val  = Float.parseFloat(value.toString());
                if (propertyName.equals("alpha")) {

                    int color = updateCircle.getFillColor();
                    Color.colorToHSV(color, hsv);
                    Integer alphaVal = val.intValue();
                    //Color.HSVToColor(mAlpha, new float[] {mColorHue, 1, 1});//default to red, medium level hue color
                    int newColor = Color.HSVToColor(alphaVal, hsv);
                    updateCircle.setFillColor(newColor);
                }

                if (propertyName.equals("color")) {
                    int alpha = Color.alpha(updateCircle.getFillColor());

                    int newColor = Color.HSVToColor(alpha, new float[] {val, 1, 1});
                    updateCircle.setFillColor(newColor);
                }

                if (propertyName.equals("radius")) {
                    // need to cast value to float
                    Float radius = val;
                    updateCircle.setRadius(radius);
                    // if it's a draggableCircle, then we need to remove the previous marker and get new radius marker
                    if (circle instanceof DraggableCircle){
                        // remove previous radius marker
                        Marker centerMarker = ((DraggableCircle) circle).getCenterMarker();
                        Marker oldMarker = ((DraggableCircle) circle).getRadiusMarker();
                        oldMarker.remove();
                        Marker newMarker = mMap.addMarker(new MarkerOptions()
                                .position(toRadiusLatLng(centerMarker.getPosition(), radius))
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_AZURE)));

                        ((DraggableCircle) circle).setRadiusMarker(newMarker);
                        // create a new draggabble circle

                    }

                }


            } catch(NumberFormatException e) { //can't parse the string
                form.dispatchErrorOccurredEvent(this, "UpdateCircle",
                        ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, value.toString());
            }


        } else {
            // the circle doesn't exist
            form.dispatchErrorOccurredEvent(this, "UpdateCircle",
                    ErrorMessages.ERROR_GOOGLE_MAP_CIRCLE_NOT_EXIST, circleId);
        }
    }

    @SimpleFunction(description = "Get all circles Ids. A short cut to get all the references for the eixisting circles")
    public YailList GetAllCircleIDs() {
        return YailList.makeList(circles.values());

    }

    // this event flow comes from  Marker.onMarkerDragStart/onMarkerDragEnd/onMarkerDrag
    // --> DraggableCircle.onMarkerMove() --> FinishDraggingCircle()
    // if the user is dragging the circle radius marker, the draggable circle object with re-draw automatically
    // only when the user finishes dragging the circle will we propagate this event to the UI.

    @SimpleEvent(description = "Event been raised after the action of moving a draggable circle is finished. Possible a user "
            + "drag the center of the circle or drag the radius marker of the circle")
    public void FinishedDraggingCircle(final int id, final double centerLat,
                                       final double centerLng, final double radius) {
        // called by moving the marker that's the center of the circle
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "a draggable circle:" + id + "finished been dragged");
                EventDispatcher.dispatchEvent(GoogleMap.this, "FinishedDraggingCircle",
                        id, centerLat, centerLng, radius);
            }
        });

    }


    // AndroidViewComponent implementation

    @Override
    public View getView() {
        //return viewLayout.getLayoutManager();
        return viewLayout;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "in onResume...Google Map redraw");
        //set up LocationClient for my location using GMS
        if(myLocationEnabled){//only if my location is enabled
            setUpLocationClientIfNeeded();
            mGoogleApiClient.connect();
        }
        setUpMapIfNeeded();
    }

    @Override
    public void onInitialize() {}

    private void prepareFragmentView() {

        mMapFragment = MapFragment.newInstance();

        androidUIHandler.post(new Runnable() {
            public void run() {
                boolean dispatchEventNow = false;
                if (mMapFragment != null) {

                    dispatchEventNow = true;
                }
                if (dispatchEventNow) {

                    // Then we add it using a FragmentTransaction.
                    // add fragment to the view
                    android.app.FragmentTransaction fragmentTransaction = form.getFragmentManager().beginTransaction();

                    //fragmentTransaction.add(viewLayout.getLayoutManager().getId(),
                    fragmentTransaction.add(viewLayout.getId(), mMapFragment, MAP_FRAGMENT_TAG);

                    fragmentTransaction.commit();

                    // set up map
                    setUpMapIfNeeded();
                } else {
                    // Try again later.
                    androidUIHandler.post(this);
                }
            }
        });
    }

    @SimpleFunction(description = "Enable or disable my location widget control for Google Map. One can call " +
            "GetMyLocation() to obtain the current location after enable this.\"")
    public void EnableMyLocation(boolean enabled){
        Log.i(TAG, "@EnableMyLocation:" + enabled);
        if (this.myLocationEnabled != enabled)
            this.myLocationEnabled = enabled;

        if (mMap != null) {

            mMap.setMyLocationEnabled(enabled); // enable google map mylocation widget

            if(enabled){
                setUpLocationClientIfNeeded();
                mGoogleApiClient.connect();
            }
            else{
                mGoogleApiClient.disconnect();
            }
        }
    }
    @SimpleProperty(description = "Indicates whether my locaiton UI control is currently enabled for the Google map.")
    public boolean MyLocationEnabled(){
        return this.myLocationEnabled;
    }

    @SimpleFunction(description = "Get current location using Google Map Service. Return a YailList with first item being" +
            "the latitude, the second item being the longitude, and last time being the accuracy of the reading.")
    public YailList GetMyLocation(){

        ArrayList<Object> latLng = new ArrayList<Object>();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.i(TAG, "client is connected");
            Location location = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);

//            Location location = mGoogleApiClient.getLastLocation();
            latLng.add(location.getLatitude());
            latLng.add(location.getLongitude());
            latLng.add(location.getAccuracy());
        }

        return YailList.makeList(latLng);
    }

    @SimpleFunction(description = "Set the layer of Google map. Default layer is \"normal\", other choices including \"hybrid\"," +
            "\"satellite\", and \"terrain\" ")
    public void SetMapType(String layerName){

        if (layerName.equals("normal")) {
            this.mapType = com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
        } else if (layerName.equals("hybrid")) {
            this.mapType = com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
        } else if (layerName.equals("satellite")) {
            this.mapType = com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
        } else if (layerName.equals("terrain")) {
            this.mapType = com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;
        } else {
            Log.i(TAG, "Error setting layer with name " + layerName);
            form.dispatchErrorOccurredEvent(this, "SetMapType",
                    ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, layerName + " is not the correct type");
        }

        if(mMap != null) {
            mMap.setMapType(this.mapType);
        }
    }

    /**
     * Enable map click event listener for this component
     * @param enabled
     */
    @SimpleFunction(description = "Enable/Disable to listen to map's click event")
    public void EnableMapClickListener(boolean enabled) {
        Log.i(TAG, "@EnableMapClickListener:" + enabled);
        if (this.enableMapClickListener != enabled)
            this.enableMapClickListener = enabled;

        if (mMap != null) {
            Log.i(TAG, "enable map listener?: " + enabled);
            mMap.setOnMapClickListener(enabled? this : null);
        }
    }

    /**
     * Indicates if the mapClick listener is currently enabled
     * @return
     */
    @SimpleProperty(description = "Indicates if the mapClick event listener is currently enabled")
    public boolean MapClickListenerEnabled() {
        return this.enableMapClickListener;
    }

    /**
     * Enable map long click event listener
     * @return
     */
    @SimpleFunction(description = "Enable/disable to listen to map's long click event")
    public void EnableMapLongClickListener(boolean enabled){
        Log.i(TAG, "@EnableMapLongClickListener:" + enabled);
        if (this.enableMapLongClickListener != enabled) {
            this.enableMapLongClickListener = enabled;
        }
        if (mMap != null) {
            Log.i(TAG, "enable long click listener?:" + enabled);
            mMap.setOnMapLongClickListener(enabled? this : null);
        }
    }

    /**
     * Indicates if the map's longClick event listener is currently enabled
     * @return
     */
    @SimpleProperty(description = "Indicates if the map longClick listener is currently enabled")
    public boolean MapLongClickListenerEnabled() {
        return this.enableMapLongClickListener;
    }

    /**
     * Enable/Disable map's camera position changed event
     * @param enabled
     */
    @SimpleFunction(description = "Enable/Disable to listen to map's camera position changed event")
    public void EnableMapCameraPosChangeListener(boolean enabled){
        Log.i(TAG, "@EnableMapCameraPosChangeListener:" + enabled);
        if (this.enableCameraChangeListener != enabled) {
            this.enableCameraChangeListener = enabled;

        }

        if (mMap != null) {
            Log.i(TAG, "enable cameraChangedListener?:" + enabled);
            mMap.setOnCameraChangeListener(enabled? this : null);
        }
    }

    /**
     * Indicates if the map camera's position changed listener is currently enabled
     * @return
     */
    @SimpleProperty(description = "Indicates if the map camera's position changed listener is currently enabled")
    public boolean MapCameraChangedListenerEnabled() {
        return this.enableCameraChangeListener;
    }

    @SimpleProperty(description = "Indicates the current map type")
    public String MapType(){
        switch(this.mapType){
            case com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL:
                return "normal";
            case com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID:
                return "hybrid";
            case com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE:
                return "satellite";
            case com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN:
                return "terrain";
        }
        return null;
    }

    /**
     *
     * @param markers
     * @return
     * TODO: Adding customized icons, also too many error msgs (disable for now)
     */
    @SimpleFunction(description = "Adding a list of YailLists for markers. The representation of a maker in the "
            + "inner YailList is composed of: "
            + "lat(double) [required], long(double) [required], Color, "
            + "title(String), snippet(String), draggable(boolean). Return a list of unqiue ids for the added "
            + " markers. Note that the markers ids are not meant to persist after " +
            " the app is closed, but for temporary references to the markers within the program only. Return an empty list" +
            " if any error happen in the input")
    public YailList AddMarkers(YailList markers) {
        // For color, check out the code in Form.java$BackgroundColor() e.g. if
        // (argb != Component.COLOR_DEFAULT)
        // After the color is chosen, it's passed in as int into the method
        // We can have two ways for supporting color of map markers: 1) pass it in
        // as int in the Yailist,
        // 2) if the user omit the value for color, we will use the blue color\
        // what's a easier way for people to know about the color list?
        // AppyBuilder currently uses RGB (android.graphics.Color), but android map
        // marker uses HUE
        // http://developer.android.com/reference/com/google/android/gms/maps/model/BitmapDescriptorFactory.html#HUE_YELLOW
        // We can use Android.graphics.Color.colorToHSV(int rgbcolor, float[]hsv) to
        // get the hue value in the hsv array
        float[] hsv = new float[3];

        ArrayList<Integer> markerIds = new ArrayList<Integer>();
        for (Object marker : markers.toArray()) {
            boolean addOne = true;
            if (marker instanceof YailList) {
                Log.i(TAG, "interior YailLiat");
                if (((YailList) marker).size() < 2) {
                    addOne = false; // don't add this marker because its invalid inputs, going to the next one
                }
                // ((YailList) marker).getObject(0) will return type gnu.math.DFloNum
                Object latObj =  ((YailList) marker).getObject(0);
                Object lngObj =  ((YailList) marker).getObject(1);
                Log.i(TAG, "Type: " + latObj.getClass());
                Log.i(TAG, "Type: " + lngObj.getClass());
                Double lat = new Double(0);
                Double lng = new Double(0);


                if (!(latObj instanceof DFloNum && lngObj instanceof DFloNum)){//if one of the lat or lng is not DFloNum
                    addOne = false;
                } else {
                    lat = ((DFloNum)latObj).doubleValue();
                    lng = ((DFloNum)lngObj).doubleValue();
                }
                //check for lat, lng range
                // Latitude measurements range from 0������ to (+/���������)90������.
                // Longitude measurements range from 0������ to (+/���������)180
                if ((lat < -90) || (lat > 90) || (lng < -180) || (lng > 180) ){
                    addOne = false;
                }


                //default values for optional params
                int color = mMarkerColor;
                String title = "";
                String snippet = "";
                boolean draggable = mMarkerDraggable;

                if (((YailList) marker).size() >= 3) {
                    Log.i(TAG, "Type: " +  ((YailList) marker).getObject(2).getClass());
                    Log.i(TAG, "Value: " + ((YailList) marker).getObject(2).toString());
                    // Integer within Yaillist is of type gnu.math.IntNum
                    Object colorObj =  ((YailList) marker).getObject(2);

                    if (colorObj instanceof gnu.math.IntNum)
                        color = ((IntNum)((YailList) marker).getObject(2)).intValue();
                    else {
                        addOne = false;
                    }

                }
                if (((YailList) marker).size() >= 4) {
                    Log.i(TAG, "Type: " +  ((YailList) marker).getObject(3).getClass());
                    Log.i(TAG, "Value: " + ((YailList) marker).getObject(3).toString());
                    title = ((YailList) marker).getObject(3).toString();
                }
                if (((YailList) marker).size() >= 5) {
                    Log.i(TAG, "Type: " +  ((YailList) marker).getObject(4).getClass());
                    Log.i(TAG, "Value: " + ((YailList) marker).getObject(4).toString());
                    snippet = ((YailList) marker).getObject(4).toString();
                }
                if (((YailList) marker).size() >= 6) {
                    Log.i(TAG, "Type: " +  ((YailList) marker).getObject(5).getClass());
                    Log.i(TAG, "Value: " + ((YailList) marker).getObject(5).toString());

                    if (((YailList) marker).getObject(5) instanceof Boolean) {
                        draggable = (Boolean) ((YailList) marker).getObject(5);
                    }
                    else {
                        addOne = false;
                    }
                }

                Color.colorToHSV(color, hsv);
                if(addOne) {
                    int uniqueId = generateMarkerId();
                    markerIds.add(uniqueId);
                    addMarkerToMap(lat, lng, uniqueId, hsv[0], title, snippet, draggable);
                }

            } else {
                // fire exception and throw error messages
                form.dispatchErrorOccurredEvent(this, "AddMarkers",
                        ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, "marker is not represented as list");
                continue; // don't add this marker because its invalid inputs, going to the next one

            }
        }
        return YailList.makeList(markerIds);

    }


    /**
     * generate unique marker id
     * @return
     */
    private static int generateMarkerId(){
        return snextMarkerId.incrementAndGet();

    }

    /**
     * generate unique circle id
     * @return
     */
    private static int generateCircleId(){
        return snextCircleId.incrementAndGet();

    }

    /**
     * generate unique circle id
     * @return
     */

    /**
     * Add a marker on the Google Map
     * @param lat
     * @param lng
     * @param title
     * @param snippet
     * @param hue
     */
    private int addMarkerToMap(Double lat, Double lng, int id, float hue, String title,
                               String snippet, boolean draggable) {
        // what if there are too many markers on Google Map ?
        // TODO: https://code.google.com/p/android-maps-extensions/
        Log.i(TAG, "@addMarkerToMap");
        LatLng latlng = new LatLng(lat, lng);
        if (mMap == null) {
            setUpMapIfNeeded();
        }
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latlng)
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));

        if (!title.isEmpty()){
            marker.setTitle(title);
        }
        if (!snippet.isEmpty()){
            marker.setSnippet(snippet);
        }
        marker.setDraggable(draggable);

        markers.put(marker, id);
        return id;
    }


    @SimpleFunction(description = "Add a list of markers composed of name-value pairs. Name fields for a marker are: " +
            "\"lat\" (type double) [required], \"lng\"(type double) [required], " +
            "\"color\"(type int)[in hue value ranging from 0~360], " +
            "\"title\"(type String), \"snippet\"(type String), \"draggable\"(type boolean)")
    public YailList GetMarkers(){
        return markersList;
    }

    @SimpleFunction(description = "Adding a list of markers that are represented as JsonArray. " +
            " The inner JsonObject represents a marker" +
            "and is composed of name-value pairs. Name fields for a marker are: " +
            "\"lat\" (type double) [required], \"lng\"(type double) [required], " +
            "\"color\"(type int)[in hue value ranging from 0~360], " +
            "\"title\"(type String), \"snippet\"(type String), \"draggable\"(type boolean)")
    public void AddMarkersFromJson(String jsonString) {
        ArrayList<Integer> markerIds = new ArrayList<Integer>();
        JsonParser parser = new JsonParser();
        float[] hsv = new float[3];

        // parse jsonString into jsonArray
        try {
            JsonElement markerList = parser.parse(jsonString);
            if (markerList.isJsonArray()) {
                JsonArray markerArray = markerList.getAsJsonArray();

                Log.i(TAG, "It's a JsonArry: " + markerArray.toString());
                for (JsonElement marker : markerArray) {
                    boolean addOne = true;
                    // now we have marker
                    if (marker.isJsonObject()) {
                        JsonObject markerJson = marker.getAsJsonObject();
                        if (markerJson.get("lat") == null || markerJson.get("lng") == null) {
                            addOne = false;

                        } else { // having correct syntax of a marker in Json

                            // check for cases: "lat" : "40.7561"  (as String)
                            JsonPrimitive jpLat = (JsonPrimitive)markerJson.get("lat");
                            JsonPrimitive jpLng = (JsonPrimitive)markerJson.get("lng");

                            double latitude = 0;
                            double longitude = 0;

                            try{ //it's possible that when converting to Double, we will have errors
                                // for example, some json has "lat": "" (empty string for lat, lng values)

                                if (jpLat.isString() && jpLng.isString()){
                                    Log.i(TAG, "jpLat:" + jpLat.toString());
                                    Log.i(TAG, "jpLng:" + jpLng.toString());

                                    latitude =  new Double(jpLat.getAsString());
                                    longitude = new Double(jpLng.getAsString());
                                    Log.i(TAG, "convert to double:" + latitude + "," + longitude);
                                }
                                else {
                                    latitude = markerJson.get("lat").getAsDouble();
                                    longitude = markerJson.get("lng").getAsDouble();
                                }

                            }  catch (NumberFormatException e){
                                addOne = false;
                            }
                            // check for Lat, Lng correct range

                            if ((latitude < -90) || (latitude > 90) || (longitude < -180) || (longitude > 180)) {
                                Log.i(TAG, "Lat/Lng wrong range:" + latitude + "," + longitude);
                                addOne = false;

                            }

                            Color.colorToHSV(mMarkerColor, hsv);
                            float defaultColor = hsv[0];
                            float color = (markerJson.get("color") == null) ? defaultColor : markerJson.get("color").getAsInt();

                            if ((color < 0) || (color > 360)) {
                                Log.i(TAG, "Wrong color");
                                addOne = false;
                            }

                            String title = (markerJson.get("title") == null) ? "" : markerJson.get("title").getAsString();
                            String snippet = (markerJson.get("snippet") == null) ? "" : markerJson.get("snippet").getAsString();
                            boolean draggable = (markerJson.get("draggable") == null) ? mMarkerDraggable : markerJson.get("draggable").getAsBoolean();

                            if(addOne){
                                Log.i(TAG, "Adding marker" + latitude + "," + longitude);
                                int uniqueId = generateMarkerId();
                                markerIds.add(uniqueId);
                                addMarkerToMap(latitude, longitude, uniqueId, color, title,
                                        snippet, draggable);
                            }
                        }
                    }
                }//end of JsonArray

            } else { // not a JsonArray
                form.dispatchErrorOccurredEvent(this, "AddMarkersFromJson",
                        ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, "markers needs to be represented as JsonArray");
                markersList = YailList.makeList(markerIds);
            }

        } catch (JsonSyntaxException e) {
            form.dispatchErrorOccurredEvent(this, "AddMarkersFromJson",
                    ErrorMessages.ERROR_GOOGLE_MAP_JSON_FORMAT_DECODE_FAILED, jsonString);
            markersList = YailList.makeList(markerIds); // return an empty markerIds list
        }

        markersList = YailList.makeList(markerIds);
        //  return YailList.makeList(markerIds);
    }

    /**
     * Add a list of YailList to the map
     * @param markers
     */
    @SimpleFunction(description = "Adding a list of YailList for markers. The inner YailList represents a marker " +
            "and is composed of lat(Double) [required], long(Double) [required], color(int)[in hue value ranging from 0-360], " +
            "title(String), snippet(String), draggable(boolean). Return a list of unique ids for the markers that are added")
    public YailList AddMarkersHue(YailList markers) {
        ArrayList<Integer> markerIds = new ArrayList<Integer>();

        for (Object marker : markers.toArray()) {
            boolean addOne = true;
            if (marker instanceof YailList) {
                Log.i(TAG, "Interior YailLiat");
                if (((YailList) marker).size() < 2){
                    // throw an exception with error messages
                    form.dispatchErrorOccurredEvent(this, "AddMarkers",
                            ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, "Need more than 2 inputs");
                    addOne = false;

                }

                // ((YailList) marker).getObject(0) will return type gnu.math.DFloNum
                Object latObj =  ((YailList) marker).getObject(0);
                Object lngObj =  ((YailList) marker).getObject(1);
                Log.i(TAG, "Type: " + latObj.getClass());
                Log.i(TAG, "Type: " + lngObj.getClass());
                Double lat = new Double(0);
                Double lng = new Double(0);


                if (!(latObj instanceof DFloNum && lngObj instanceof DFloNum)){//if one of the lat or lng is not DFloNum
                    form.dispatchErrorOccurredEvent(this, "AddMarkersHue",
                            ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, "Not a number for latitude or longitude");
                    addOne = false;
                } else {
                    lat = ((DFloNum)latObj).doubleValue();
                    lng = ((DFloNum)lngObj).doubleValue();
                }

                if (lat < -90 || lat > 90 || lng < -180 || lng > 180 ){
                    addOne = false;
                    form.dispatchErrorOccurredEvent(this, "AddMarkers",
                            ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, "Range for the latitude or longitude is wrong");
                }

                Integer uniqueId = generateMarkerId();
                float color = BitmapDescriptorFactory.HUE_BLUE;
                String title = "";
                String snippet = "";
                boolean draggable = mMarkerDraggable;

                if (((YailList) marker).size() >= 3) {
                    Log.i(TAG, "Type: " +  ((YailList) marker).getObject(2).getClass());
                    Log.i(TAG, "Value: " + ((YailList) marker).getObject(2).toString());
                    // Integer within Yaillist is of type gnu.math.IntNum
                    Object colorObj =  ((YailList) marker).getObject(2);

                    if (colorObj instanceof gnu.math.IntNum)
                        color = new Float(((IntNum)((YailList) marker).getObject(2)).intValue());//extract the int val and convert to Float
                    else {
                        addOne = false;
                        form.dispatchErrorOccurredEvent(this, "AddMarkersHue",
                                ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, colorObj.toString() + " is not a number");
                    }
                }

                if (((YailList) marker).size() >= 4){
                    title = (String) ((YailList) marker).getObject(3);
                    Log.i(TAG, "Type: " +  ((YailList) marker).getObject(3).getClass());
                    Log.i(TAG, "Value: " + ((YailList) marker).getObject(3).toString());
                }

                if (((YailList) marker).size() >= 5){
                    snippet = (String) ((YailList) marker).getObject(4);
                    Log.i(TAG, "Type: " +  ((YailList) marker).getObject(4).getClass());
                    Log.i(TAG, "Value: " + ((YailList) marker).getObject(4).toString());
                }

                if (((YailList) marker).size() >= 6) {

                    Log.i(TAG, "Type: " +  ((YailList) marker).getObject(5).getClass());
                    Log.i(TAG, "Value: " + ((YailList) marker).getObject(5).toString());

                    if (((YailList) marker).getObject(5) instanceof Boolean) {
                        draggable = (Boolean) ((YailList) marker).getObject(5);
                    }
                    else {
                        form.dispatchErrorOccurredEvent(this, "AddMarkers",
                                ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, "marker not as a list");
                        addOne = false;
                    }

                }
                if (addOne){
                    markerIds.add(uniqueId);
                    addMarkerToMap(lat, lng, uniqueId,  color, title, snippet, draggable);
                }
            }
            else {
                // fire exception and throw error messages
                form.dispatchErrorOccurredEvent(this, "AddMarkersHue",
                        ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, "Marker is not represented as list");
                return YailList.makeList(markerIds); // return an empty markerIds list
            }
        }
        return YailList.makeList(markerIds);

    }

    @SimpleFunction(description = "Set the property of a marker, note that the marker has to be added first or else will "
            + "throw an exception! Properties include: \"color\"(hue value ranging from 0~360), \"title\", "
            + "\"snippet\", \"draggable\"(give either true or false as the value).")
    public void UpdateMarker(int markerId, String propertyName, Object value) {
        // we don't support update lat, lng here, one can remove the marker and add
        // a new one
        String property = propertyName.trim();
        String propVal = value.toString().trim(); //convert everything to String first

        Log.i(TAG, "@UpdateMarker");
        Log.i(TAG, "markerId:" + markerId);
        Log.i(TAG, "prop:" + propertyName);
        Log.i(TAG, "value:" + value);
        Marker marker = getMarkerIfExisted(markerId);
        Log.i(TAG, "marker?:" + marker);


        if (marker != null) {
            if (property.equals("color")) {
                Log.i(TAG, "we are changing color");
                Float hue = new Float(propVal);
                if(hue < 0 || hue > 360) {
                    form.dispatchErrorOccurredEvent(this, "UpdateMarker",
                            ErrorMessages.ERROR_GOOGLE_MAP_INVALID_INPUT, hue.toString());
                }
                else{

                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(new Float(propVal)));
                }
            }
            if (property.equals("title")) {
                Log.i(TAG, "we are changing title");
                marker.setTitle(propVal);
            }
            if (property.equals("snippet")) {
                Log.i(TAG, "we are changing snippet");
                marker.setSnippet(propVal);
            }
            if (property.equals("draggable")) {
                Log.i(TAG, "we are changing draggable");
                marker.setDraggable(new Boolean(propVal));
            }
        }
    }

    @SimpleFunction(description = "Get all the existing markers's Ids" )
    public YailList GetAllMarkerID(){
        return YailList.makeList(markers.values());

    }

    private Marker getMarkerIfExisted(int markerId){
        Marker marker = getKeyByValue(markers, markerId);

        if(marker.equals(null)){
            form.dispatchErrorOccurredEvent(this, "getMarkerIfExisted",
                    ErrorMessages.ERROR_GOOGLE_MAP_MARKER_NOT_EXIST, Integer.toString(markerId));
        }
        return marker;
    }


    @SimpleFunction(description = "Remove a marker from the map")
    public void RemoveMarker(int markerId) {
        Marker marker = getMarkerIfExisted(markerId);
        if (marker != null)
        {
            markers.remove(marker); //remove from the Hashmap
            marker.remove(); // remove from the google map
        }

    }


    @Override
    public void onMarkerDrag(Marker marker) {
        // TODO Auto-generated method stub
        Log.i(TAG, "Dragging M:" + marker);
        Integer markerId = markers.get(marker);
        // if it's a marker for draggable circle then it's not in the hashmap, Ui will not receive this dragging event
        if (markerId != null) {
            LatLng latlng = marker.getPosition();
            OnMarkerDrag(markerId, latlng.latitude, latlng.longitude);
        }
        // find if the marker is the center or radius marker of any existing draggable circle,
        // then call the move or resize this draggable circle
        for (DraggableCircle dCircle: mCircles){
            if ((dCircle.getCenterMarker().equals(marker)) || (dCircle.getRadiusMarker().equals(marker))) {
                dCircle.onMarkerMoved(marker);   //ask the draggable circle to change it's appearance
            }
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // TODO Auto-generated method stub

        Integer markerId = markers.get(marker);
        // if it's a marker for draggable circle then it's not in the hashmap, Ui will not receive this dragging event
        if (markerId != null) {
            LatLng latlng = marker.getPosition();
            OnMarkerDragEnd(markerId, latlng.latitude, latlng.longitude);
        }
        // find if the marker is the center or radius marker of any existing draggable circle, then call the move or resize
        // this draggable circle
        for (DraggableCircle dCircle: mCircles){
            if ((dCircle.getCenterMarker().equals(marker)) || (dCircle.getRadiusMarker().equals(marker))) {
                dCircle.onMarkerMoved(marker);   //ask the draggable circle to change it's appearance
                // also fire FinishedDraggingCircle() to UI
                int uid = circles.get(dCircle);
                LatLng center = dCircle.getCenterMarker().getPosition();
                FinishedDraggingCircle(uid, center.latitude, center.longitude, dCircle.getRadius());
            }
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // TODO Auto-generated method stub
        // if it's a marker for draggable circle then it's not in the hashmap, Ui will not receive this dragging event
        Integer markerId = markers.get(marker);
        if (markerId != null) {
            LatLng latLng = marker.getPosition();
            OnMarkerDragStart(markerId, latLng.latitude, latLng.longitude); //fire event to UI
        }
        // find if the marker is the center or radius marker of any existing draggable circle, then call the move or resize
        // this draggable circle
        for (DraggableCircle dCircle: mCircles){
            if ((dCircle.getCenterMarker().equals(marker)) || (dCircle.getRadiusMarker().equals(marker))) {
                dCircle.onMarkerMoved(marker);   //ask the draggable circle to change it's appearance
            }
        }


    }

    @SimpleEvent(description = "When a marker starts been dragged")
    public void OnMarkerDragStart (final int markerId, final double latitude, final double longitude){
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "a marker:" + markerId + "starts been dragged");
                EventDispatcher.dispatchEvent(GoogleMap.this, "OnMarkerDragStart", markerId, latitude, longitude);
            }
        });
    }

    @SimpleEvent(description = "When a marker is been dragged")
    public void OnMarkerDrag (final int markerId, final double latitude, final double longitude){
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "a marker:" + markerId + "is been dragged");
                EventDispatcher.dispatchEvent(GoogleMap.this, "OnMarkerDrag", markerId, latitude, longitude);
            }
        });
    }


    @SimpleEvent(description = "When the user drags a marker and finish the action, " +
            "returning marker's id and it's latest position")
    public void OnMarkerDragEnd(final int markerId, final double latitude, final double longitude){
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "a marker:" + markerId + "finishes been dragged");
                EventDispatcher.dispatchEvent(GoogleMap.this, "OnMarkerDragEnd", markerId, latitude, longitude);
            }
        });
    }

    @SimpleEvent(description = "When a marker is clicked")
    public void OnMarkerClick(final int markerId, final double latitude, final double longitude){
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "a marker:" + markerId + "is clicked");
                EventDispatcher.dispatchEvent(GoogleMap.this, "OnMarkerClick", markerId, latitude, longitude);
            }
        });
    }

    @SimpleEvent(description = "When the marker's infowindow is clicked, returning marker's id")
    public void InfoWindowClicked(final int markerId){
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "A marker: " + markerId + " its info window is clicked");
                EventDispatcher.dispatchEvent(GoogleMap.this, "InfoWindowClicked", markerId);
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // TODO Auto-generated method stub
        Integer markerId = markers.get(marker);
        InfoWindowClicked(markerId);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // TODO Auto-generated method stub

        Integer markerId = markers.get(marker);
        LatLng latLng = marker.getPosition();
        OnMarkerClick(markerId, latLng.latitude, latLng.longitude);

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    /**
     * A small util function to get the key-value mapping in a map.
     * We use this to get our marker (key) using unique values
     * of marker identifiers
     * @param map
     * @param value
     * @param <T>
     * @param <E>
     * @return
     */
    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        Double lat = position.target.latitude;
        Double lng = position.target.longitude;
        Float bearing = position.bearing;
        Float tilt = position.tilt;
        Float zoom = position.zoom;
        CameraPositionChanged(lat, lng, bearing, tilt, zoom);
    }

    /**
     * Called after the camera position has changed, returning all camera position parameters.
     * @param lat
     * @param lng
     * @param bearing
     * @param tilt
     * @param zoom
     */
    @SimpleEvent(description = "Called after the camera position of a map has changed.")
    public void CameraPositionChanged(final double lat, final double lng, final float bearing,
                                      final float tilt, final float zoom) {
        context.runOnUiThread(new Runnable(){
            public void run() {
                Log.i(TAG, "Camera's position has changed:" + lat + ", " + lng + ", " + bearing + "," + tilt + ", " + zoom);
                EventDispatcher.dispatchEvent(GoogleMap.this, "CameraPositionChanged", lat, lng, bearing, tilt, zoom);
            }
        });
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        // TODO Auto-generated method stub
        OnMapLongClick(latLng.latitude, latLng.longitude);
    }

    /**
     * Called when the user makes a long-press gesture on the map
     * @param lat
     * @param lng
     */
    @SimpleEvent(description = "Called when the user makes a long-press gesture on the map")
    public void OnMapLongClick(final double lat, final double lng){
        context.runOnUiThread(new Runnable(){
            public void run() {
                Log.i(TAG, "Map is longclicked at:" + lat + ", " + lng);
                EventDispatcher.dispatchEvent(GoogleMap.this, "OnMapLongClick", lat, lng);
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // TODO Auto-generated method stub
        Log.i(TAG, "receive google maps's onMapClick");
        OnMapClick(latLng.latitude, latLng.longitude);

    }

    @SimpleEvent(description =  "Called when the user makes a tap gesture on the map")
    public void OnMapClick(final double lat, final double lng){
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "map is clicked at:" + lat + ", " + lng);
                EventDispatcher.dispatchEvent(GoogleMap.this, "OnMapClick", lat, lng);
            }
        });
    }


    @SimpleFunction(description = "Move the map's camera to the specified position and zoom level")
    public void MoveCamera(double lat, double lng, float zoom){
        if(mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
        }
    }

    /**
     *
     * @param neLat Latitude of the northeast location of the bounding box
     * @param neLng Longitude of the northeast location of the bounding box
     * @param swLat Latitude of the southwest location of the bounding box
     * @param swLng Longitude of the southwest location of the bounding box
     */
    @SimpleFunction(description = "Transforms the camera such that the specified latitude/longitude " +
            "bounds are centered on screen at the greatest possible zoom level. Need to specify both latitudes and " +
            "longitudes for both northeast location and southwest location of the bounding box")
    public void BoundCamera(double neLat, double neLng, double swLat, double swLng){

        LatLng northeast  = new LatLng(neLat, neLng);
        LatLng southwest = new LatLng(swLat, swLng);
        LatLngBounds bounds = new LatLngBounds(northeast, southwest);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.moveCamera(cameraUpdate);

    }

    // private class representing the circle overlay. Code copied and extended from Google Example
    // We need to keep a data structure to tie circle and two markers together.
    private class DraggableCircle {
        private final Marker centerMarker;
        private Marker radiusMarker;
        private final Circle circle;
        private double radius;

        // In Draggable circle, AI user will not get reference of the inner objects (circle, markers)
        public DraggableCircle(LatLng center, double radius, float strokeWidth, int strokeColor, int fillColor) {
            this.radius = radius;
            centerMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true));
            radiusMarker = mMap.addMarker(new MarkerOptions()
                    .position(toRadiusLatLng(center, radius))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));
            circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(strokeWidth)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor));

        }
        public DraggableCircle(LatLng center, LatLng radiusLatLng, float strokeWidth, int strokeColor, int fillColor) {
            this.radius = toRadiusMeters(center, radiusLatLng);
            centerMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true));
            radiusMarker = mMap.addMarker(new MarkerOptions()
                    .position(radiusLatLng)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));
            circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(strokeWidth)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor));
        }
        // draw a circle between two known marker
        public DraggableCircle(Marker center, Marker radius, float strokeWidth, int strokeColor, int fillColor){
            this.radius = toRadiusMeters(center.getPosition(), radius.getPosition());
            centerMarker = center;
            radiusMarker = radius;
            circle = mMap.addCircle(new CircleOptions()
                    .center(center.getPosition())
                    .radius(this.radius)
                    .strokeWidth(strokeWidth)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor));
        }

        public boolean onMarkerMoved(Marker marker) {
            if (marker.equals(centerMarker)) {
                circle.setCenter(marker.getPosition());
                radiusMarker.setPosition(toRadiusLatLng(marker.getPosition(), radius));
                return true;
            }
            if (marker.equals(radiusMarker)) {
                radius = toRadiusMeters(centerMarker.getPosition(), radiusMarker.getPosition());
                circle.setRadius(radius);
                return true;
            }
            return false;
        }

        public void removeFromMap(){
            this.circle.remove();
            this.centerMarker.remove();
            this.radiusMarker.remove();
        }
        public Marker getCenterMarker(){
            return this.centerMarker;
        }

        public Marker getRadiusMarker(){
            return this.radiusMarker;
        }

        public Circle getCircle(){
            return this.circle;
        }

        public Double getRadius(){
            return this.radius;
        }

        public void setRadiusMarker(Marker marker){
            this.radiusMarker = marker;
        }
    }

    /** Generate LatLng of radius marker */
    private static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    private static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "onConnected to location listener.....");
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, REQUEST, this);

//        mLocationClient.requestLocationUpdates(REQUEST, this);  // LocationListener
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


//    @Override
//    public void onDisconnected() {
//        // TODO Auto-generated method stub
//    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
//        Log.i(TAG, "OnPause, remote LocationClient");
//        if (mLocationClient != null) {
//            Log.i(TAG, "before location client disconnect");
//            mLocationClient.disconnect();
//        }
        if (this.mGoogleApiClient != null)
            this.mGoogleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        OnLocationChanged(location.getLatitude(), location.getLongitude());
    }

    @SimpleEvent(description = "Triggers this event when user location has changed. Only works when EnableMylocation is set to true")
    public void OnLocationChanged(final double lat, final double lng){
        context.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "location changed"  + lat + lng );
                EventDispatcher.dispatchEvent(GoogleMap.this, "OnLocationChanged", lat, lng);
            }
        });
    }

    @SimpleFunction
    public void addPolygon(double latMin, double latMax, double lonMin, double lonMax) {
//  	LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//  	LatLng ne = latLngBounds.northeast;
//  	LatLng sw = latLngBounds.southwest;

//  	double lat1 = ne.latitude;
//  	double lng1 = ne.latitude;
//  	double lat4 = sw.latitude;
//  	double lng4 = sw.longitude;

        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(latMin, lonMax),
                        new LatLng(latMax, lonMax),
                        new LatLng(latMax, lonMin),
                        new LatLng(latMin, lonMin),
                        new LatLng(latMin, lonMax));

        // Get back the mutable Polygon
        Polygon polygon = mMap.addPolygon(rectOptions);
        polygons.put(polygon, 1);
    }

    @SimpleFunction
    public void clearAllPolygons() {
        Set<Polygon> setOfPolygon = polygons.keySet();
        for(Polygon p : setOfPolygon) {
            p.remove();
        }
    }

    @SimpleFunction
    public void drawCentralSquare() {
        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        LatLng ne = latLngBounds.northeast;
        LatLng sw = latLngBounds.southwest;

        double lat1 = ne.latitude;
        double lng1 = ne.latitude;

        double lat4 = sw.latitude;
        double lng4 = sw.longitude;


        double latC = mMap.getCameraPosition().target.latitude;
        double lngC = mMap.getCameraPosition().target.longitude;

        double latDiff2 = (latC - lat4)*0.5;
        double lngDiff2 = (lngC - lng4)*0.5;

//		AddMarkersFromJson("[{lat:"+(latC+latDiff2)+",lng:"+(lngC+lngDiff2)+"}]");
//		AddMarkersFromJson("[{lat:"+(latC-latDiff2)+",lng:"+(lngC+lngDiff2)+"}]");
//		AddMarkersFromJson("[{lat:"+(latC-latDiff2)+",lng:"+(lngC-lngDiff2)+"}]");
//		AddMarkersFromJson("[{lat:"+(latC+latDiff2)+",lng:"+(lngC-lngDiff2)+"}]");
        AddMarkersFromJson("[{lat:"+latC+",lng:"+lngC+"}]");

        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng((latC+latDiff2), (lngC+lngDiff2)),
                        new LatLng((latC-latDiff2), (lngC+lngDiff2)),
                        new LatLng((latC-latDiff2), (lngC-lngDiff2)),
                        new LatLng((latC+latDiff2), (lngC-lngDiff2)),
                        new LatLng((latC+latDiff2), (lngC+lngDiff2)));

        // Get back the mutable Polygon
        Polygon polygon = mMap.addPolygon(rectOptions);
        polygons.put(polygon, 1);
    }

    @SimpleFunction
    public String getBoundingBox(double latitudeInDegrees, double longitudeInDegrees, double halfSideInKm) {
        // Semi-axes of WGS-84 geoidal reference
        double WGS84_a = 6378137.0;  // Major semiaxis [m]
        double WGS84_b = 6356752.3;  // Minor semiaxis [m]

        // Bounding box surrounding the point at given coordinates,
        // assuming local approximation of Earth surface as a sphere
        // of radius given by WGS84
        double lat = Math.toRadians(latitudeInDegrees);
        double lon = Math.toRadians(longitudeInDegrees);
        double halfSide = 1000*halfSideInKm;

        // Radius of Earth at given latitude
        // Earth radius at a given latitude, according to the WGS-84 ellipsoid [m]
        // http://en.wikipedia.org/wiki/Earth_radius
        double An = WGS84_a*WGS84_a * Math.cos(lat);
        double Bn = WGS84_b*WGS84_b * Math.sin(lat);
        double Ad = WGS84_a * Math.cos(lat);
        double Bd = WGS84_b * Math.sin(lat);
        double radius = Math.sqrt( (An*An + Bn*Bn)/(Ad*Ad + Bd*Bd) );

        // Radius of the parallel at given latitude
        double pradius = radius*Math.cos(lat);

        double latMin = lat - halfSide/radius;
        double latMax = lat + halfSide/radius;
        double lonMin = lon - halfSide/pradius;
        double lonMax = lon + halfSide/pradius;

        String coordinates = Math.toDegrees(latMin) + "," + Math.toDegrees(lonMin) + ","
                + Math.toDegrees(latMax) + "," + Math.toDegrees(lonMax);
        return coordinates;
    }


    @SimpleFunction
    public void addOverlay() {
        LatLng NEWARK = new LatLng(40.714086, -74.228697);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .position(NEWARK, 8600f, 6500f);
        mMap.addGroundOverlay(newarkMap);
    }

    @SimpleFunction
    public void addTileOverlay() {
        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {

  	    /* Define the URL pattern for the tile images */
                String s = String.format("http://my.image.server/images/%d/%d/%d.png",
                        zoom, x, y);

                if (!checkTileExists(x, y, zoom)) {
                    return null;
                }

                try {
                    return new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
            }

            /*
             * Check that the tile server supports the requested x, y and zoom.
             * Complete this stub according to the tile range you support.
             * If you support a limited range of tiles at different zoom levels, then you
             * need to define the supported x, y range at each zoom level.
             */
            private boolean checkTileExists(int x, int y, int zoom) {
                int minZoom = 12;
                int maxZoom = 16;

                if ((zoom < minZoom || zoom > maxZoom)) {
                    return false;
                }

                return true;
            }
        };

        mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider));
    }

    @SimpleFunction
    public String getMapCenter() {
        LatLng latLng = mMap.getCameraPosition().target;
        return latLng.toString();
    }

    @SimpleFunction
    public float getZoomLevelInfo() {
        return mMap.getCameraPosition().zoom;
    }
}
