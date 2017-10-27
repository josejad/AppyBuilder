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
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ListPickerActivity class - Brings up a list of items specified in an intent
 * and returns the selected item as the result.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author M. Hossein Amerkashi (kkashi01@gmail.com)
 */
public class ListPickerCustomActivity extends Activity implements AdapterView.OnItemClickListener, OnDismissCallback {
    private String TAG = "ListPickerCustomActivity";

    private String closeAnim = "";
    private boolean isSwipeToArchive = false;
    private ListView listView;
    String items[];
    // Listview Adapter
    MyAdapter adapter;
    ArrayList<String> itemsArrayList = new ArrayList<String>();
    List<RowItem> listviewItems = new ArrayList<RowItem>();

    ArrayList<String> deletedList = new ArrayList<String>();

    // Search EditText
    EditText txtSearchBox;
    private boolean hasRemovedItems = false;  //If user swipes-to-delete, we need to pass back updated items list

    static int itemColor;
    static int backgroundColor;
    private int myLayout;
    private Resources resources;
    private Boolean isRepl=false;
    private AssetManager assetManager;
    LinearLayout layoutRows;

//    Form $form;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "starting activity" );

//        $form = (Form) getParent();
        LinearLayout viewLayout = new LinearLayout(this);
        viewLayout.setOrientation(LinearLayout.VERTICAL);

        layoutRows = new LinearLayout(this);

        //Don't setup up style; causes NPE when clicked:
        // Caused by: java.lang.NullPointerException: Attempt to invoke virtual
        // method 'void android.widget.RtlSpacingHelper.setDirection(boolean)' on a null object reference

//      setupStyle();

        Intent myIntent = getIntent();
        if (myIntent.hasExtra(ListPickerCustom.LIST_ACTIVITY_ANIM_TYPE)) {
            closeAnim = myIntent.getStringExtra(ListPickerCustom.LIST_ACTIVITY_ANIM_TYPE);
        }

        isRepl = Boolean.valueOf(myIntent.getStringExtra(ListPickerCustom.LIST_ACTIVITY_IS_REPL));

        if (myIntent.hasExtra(ListPickerCustom.LIST_ACTIVITY_ORIENTATION_TYPE)) {
            String orientation = myIntent.getStringExtra(ListPickerCustom.LIST_ACTIVITY_ORIENTATION_TYPE).toLowerCase();
            if (orientation.equals("portrait")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (orientation.equals("landscape")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        if (myIntent.hasExtra(ListPickerCustom.LIST_ACTIVITY_TITLE)) {
            String title = myIntent.getStringExtra(ListPickerCustom.LIST_ACTIVITY_TITLE);
            setTitle(title);
        }

        if (myIntent.hasExtra(ListPickerCustom.LIST_ACTIVITY_ARG_NAME)) {
            items = getIntent().getStringArrayExtra(ListPickerCustom.LIST_ACTIVITY_ARG_NAME);

            boolean hasImages = false;
            for (String anItem : items) {
                String[] temp = anItem.split("\\|");
                if (temp.length > 1) {
                    // we have image and text.
                    hasImages = true;
                    listviewItems.add(new RowItem(temp[0], temp[1]));   //0=desc, 1=imagePath
                } else {
                    // we only have text. 0th position is the text
                    listviewItems.add(new RowItem(temp[0], "NO_IMAGE_SPECIFIED"));
                }
            }

            listView = new ListView(this);
            listView.setOnItemClickListener(this);

            itemColor = myIntent.getIntExtra(ListPickerCustom.LIST_ACTIVITY_ITEM_TEXT_COLOR, ListPickerCustom.DEFAULT_ITEM_TEXT_COLOR);
            backgroundColor = myIntent.getIntExtra(ListPickerCustom.LIST_ACTIVITY_BACKGROUND_COLOR, ListPickerCustom.DEFAULT_ITEM_BACKGROUND_COLOR);

            viewLayout.setBackgroundColor(backgroundColor);

//            int myLayout = res.getIdentifier("list_item", "layout", getPackageName());
//            Resources res = getApplicationContext().getResources();

            //Did we have any images?
//            if (hasImages) {
//                Log.d(TAG, "listview has images");

                // Get it only 1 time. Don't put into MyAdapter
                resources = this.getResources();
                myLayout = resources.getIdentifier("lvimageview", "layout", getApplicationContext().getPackageName());

                // Adding items to listview
                adapter = new MyAdapter(getApplicationContext(), listviewItems);
                listView.setAdapter(adapter);
                setupStyle(adapter);
//            }

            Log.d(TAG, "got adapter ");


            String showFilterBar = myIntent.getStringExtra(ListPickerCustom.LIST_ACTIVITY_SHOW_SEARCH_BAR);

            // Determine if we should even show the search bar
            txtSearchBox = new EditText(this);
            txtSearchBox.setSingleLine(true);
            txtSearchBox.setWidth(Component.LENGTH_FILL_PARENT);
            txtSearchBox.setPadding(10, 10, 10, 10);
            txtSearchBox.setHint("Search list...");

            if (showFilterBar == null || !showFilterBar.equalsIgnoreCase("true")) {
                txtSearchBox.setVisibility(View.GONE);
            }

            //If swipe, and user tries to use FilterBox, it won't work.
            if (txtSearchBox.getVisibility() == View.VISIBLE && isSwipeToArchive) {
                txtSearchBox.setVisibility(View.GONE);
                Toast.makeText(this, "With Swipe, you can't use ShowFilterBar", Toast.LENGTH_LONG).show();
            }

            //set up the listener
            txtSearchBox.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // When user changed the Text
                    ListPickerCustomActivity.this.adapter.getFilter().filter(cs);
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    // no-op. Required method
                }

                @Override
                public void afterTextChanged(Editable arg0) {
                    // no-op. Required method
                }
            });

        } else {
            setResult(RESULT_CANCELED);
            finish();
            AnimationUtil.ApplyCloseScreenAnimation(this, closeAnim);
        }

        viewLayout.addView(txtSearchBox);
        viewLayout.addView(listView);

        this.setContentView(viewLayout);
        viewLayout.requestLayout();

        //hide the keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    private void setupStyle(BaseAdapter adapter) {
        String animationType = getIntent().getStringExtra(ListPickerCustom.LIST_ACTIVITY_ANIMATION);
        if (animationType == null || animationType.equalsIgnoreCase("default"))  //this is the default
        {
            listView.setAdapter(adapter);
        } else if (animationType.equalsIgnoreCase("bottom")) {
            setBottomAdapter(adapter);
        } else if (animationType.equalsIgnoreCase("bottomright")) {
            setBottomRightAdapter(adapter);
        } else if (animationType.equalsIgnoreCase("left")) {
            setLeftAdapter(adapter);
        } else if (animationType.equalsIgnoreCase("scale")) {
            setScaleAdapter(adapter);
        } else if (animationType.equalsIgnoreCase("alpha")) {
            setAlphaAdapter(adapter);
        } else if (animationType.equalsIgnoreCase("swipe")) {
            setSwipeDismissAdapter(adapter);
            isSwipeToArchive = true;
        } else if (animationType.equalsIgnoreCase("right")) {
            setRightAdapter(adapter);
        } else {
            setRightAdapter(adapter);
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapterWithImageClicked(parent, view, position, id);
    }

    private void adapterWithImageClicked(AdapterView<?> parent, View view, int position, long id) {
        RowItem rowItem = (RowItem) parent.getAdapter().getItem(position);
        String selected = "";
        if (rowItem != null) {
            selected = rowItem.getDesc();
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra(ListPickerCustom.LIST_ACTIVITY_RESULT_NAME, selected);
        resultIntent.putExtra(ListPickerCustom.LIST_ACTIVITY_RESULT_INDEX, position + 1);
        joinItemsBack();
//        resultIntent.putExtra(ListPickerCustom.LIST_ACTIVITY_ITEMS, itemsToArrayList()); //add this in case user has removed items
        resultIntent.putExtra(ListPickerCustom.LIST_ACTIVITY_ITEMS, itemsArrayList);
        resultIntent.putExtra(ListPickerCustom.LIST_ACTIVITY_ITEMS_DELETED, deletedList);
        closeAnim = selected;
        setResult(RESULT_OK, resultIntent);
        finish();
        AnimationUtil.ApplyCloseScreenAnimation(this, closeAnim);
    }



    // Capture the hardware back button to make sure the screen animation
    // still applies. (In API level 5, we can override onBackPressed instead)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean handled = super.onKeyDown(keyCode, event);
            AnimationUtil.ApplyCloseScreenAnimation(this, closeAnim);
            return handled;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {

//        if (hasRemovedItems) {
        //See how to get this working with OnKeyDown. Below doesn't have onBackPressed:
        // https://github.com/mit-cml/appinventor-sources/blob/master/appinventor/components/src/com/google/appinventor/components/runtime/ListPickerActivity.java
        Intent resultIntent = new Intent();
        //todo: convert the arraylist of RowItems to listArrayList, then return it

        joinItemsBack();
        resultIntent.putExtra(ListPickerCustom.LIST_ACTIVITY_ITEMS, itemsArrayList);
        resultIntent.putExtra(ListPickerCustom.LIST_ACTIVITY_ITEMS_DELETED, deletedList);
        //We don't want to use RESULT_OK because user hasn't selected any item
        setResult(RESULT_CANCELED, resultIntent);
//        }

        finish();
    }

    private void joinItemsBack() {
        //join items back into a list of imageDesc|imageName
        itemsArrayList = new ArrayList<String>();
        for (int i=0; i<adapter.getCount(); i++) {
            RowItem rowItem = adapter.getItem(i);
            itemsArrayList.add(rowItem.getDesc()+"|"+rowItem.getImageName());
        }
//        for (RowItem aRowItem : adapter.getitem) {
//            itemsArrayList.add(aRowItem.getDesc()+"|"+aRowItem.getImageName());
//        }
    }


    private void setAlphaAdapter(BaseAdapter adapter) {
        AnimationAdapter anAdapter = new AlphaInAnimationAdapter(adapter);
        anAdapter.setAbsListView(listView);
        listView.setAdapter(anAdapter);
    }

    private void setLeftAdapter(BaseAdapter adapter) {
        AnimationAdapter anAdapter = new SwingLeftInAnimationAdapter(adapter);
        anAdapter.setAbsListView(listView);
        listView.setAdapter(anAdapter);
    }

    private void setRightAdapter(BaseAdapter adapter) {
        AnimationAdapter anAdapter = new SwingRightInAnimationAdapter(adapter);
        anAdapter.setAbsListView(listView);
        listView.setAdapter(anAdapter);
    }

    private void setBottomAdapter(BaseAdapter adapter) {
        AnimationAdapter anAdapter = new SwingBottomInAnimationAdapter(adapter);
        anAdapter.setAbsListView(listView);
        listView.setAdapter(anAdapter);
    }

    private void setBottomRightAdapter(BaseAdapter adapter) {
        AnimationAdapter anAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(adapter));
        anAdapter.setAbsListView(listView);
        listView.setAdapter(anAdapter);
    }

    private void _setBottomRightAdapter(BaseAdapter adapter) {
        AnimationAdapter anAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(adapter));
        anAdapter.setAbsListView(listView);
        listView.setAdapter(anAdapter);
    }

    private void setSwipeDismissAdapter(BaseAdapter adapter) {
        SwipeDismissAdapter anAdapter = new SwipeDismissAdapter(adapter, this);
        anAdapter.setAbsListView(listView);
        listView.setAdapter(anAdapter);
    }

    @Override
    public void onDismiss(final AbsListView listView, final int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            if (this.adapter != null) {
//                Toast.makeText(this, "Item deleted!" + adapter.get(position).getDesc(), Toast.LENGTH_SHORT).show();
                deletedList.add(adapter.get(position).getDesc());
                adapter.remove(position);
            }
        }
        listviewItems.remove(reverseSortedPositions);

        //Users didn't want 'Item Deleted' to be displayed. They prefer to display in their native language. For now, disabling it
//        Toast.makeText(this, "Item Deleted!" , Toast.LENGTH_SHORT).show();

        hasRemovedItems = true;
    }

    private void setScaleAdapter(BaseAdapter adapter) {
        AnimationAdapter anAdapter = new ScaleInAnimationAdapter(adapter);
        anAdapter.setAbsListView(listView);
        listView.setAdapter(anAdapter);
    }


    //=================================================
    private class MyAdapter extends ArrayAdapter<RowItem> {

        private final Context mContext;
//        public int setThumbWidth = 80;
//        public int setThumbHeigth = 80;
        public boolean showText = true;
        private int width = 80;
        private int height = 80;

        public MyAdapter(final Context context, final List<RowItem> items) {
            super(items);
            mContext = context;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            return this.width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return this.height;
        }

        // nice tut, but won't be good for my work: http://www.viralandroid.com/2016/02/android-listview-with-image-and-text.html
//http://theopentutorials.com/tutorials/android/listview/android-custom-listview-with-image-and-text-using-arrayadapter/
        //http://www.android-examples.com/android-custom-listview-with-imageview-and-textview-using-arrayadapter-string/
//        private view holder class
        private class ViewHolder {
            ImageView imageView;
            TextView txtDesc;
        }

        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            RowItem rowItem = getItem(position);
            Log.d(TAG, "starting to get item at position " +position);

            ViewHolder viewHolder = null;
            if (convertView == null) {
                // programmatically find lvImageText.xml layout from resources
//                Resources res = mContext.getResources();
//                int myLayout = res.getIdentifier("lvimageview", "layout", mContext.getPackageName());

                // creating layouts programatically: https://stackoverflow.com/questions/20283723/creating-linearlayout-programmatically-dynamically-with-multiple-views

                // after layout is found, then inflate it and assign it to convertView
                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(myLayout, null);

                // from the inflated convertView (which is actually lvImageText.xml), find the desc and icon IDs,
                //  and then set it into our viewHolder
                viewHolder = new ViewHolder();
                viewHolder.txtDesc = (TextView) convertView.findViewById(resources.getIdentifier("desc", "id", mContext.getPackageName()));
                viewHolder.txtDesc.setTextColor(itemColor);

                viewHolder.imageView = (ImageView) convertView.findViewById(resources.getIdentifier("icon", "id", mContext.getPackageName()));

                viewHolder.imageView.getLayoutParams().height = convertDpToDensity(height);
                viewHolder.imageView.getLayoutParams().width = convertDpToDensity(width);


                // Now associate the convertView with our viewHolder, so that we can later use it
// do we want to show text or make it invisible?
                if (!showText) {
                    viewHolder.txtDesc.setVisibility(View.INVISIBLE);
                }

                layoutRows.setOrientation(LinearLayout.HORIZONTAL);
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setContentDescription("Thumbnail");
                imageView.setPadding(10, 0, 10, 0);
                imageView.setId(100);
                imageView.setMaxWidth(width);
                imageView.setMaxHeight(height);
                layoutRows.addView(imageView);

                TextView textView = new TextClock(getApplicationContext());
                textView.setId(110);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(convertDpToDensity(14));
                layoutRows.addView(textView);

                //todo: This code sets the description appear justified. We need to eliminate this
//                viewHolder.txtDesc.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                viewHolder.txtDesc.setGravity(Gravity.LEFT);

//                setWidthHeight(viewHolder.imageView, setThumbWidth, setThumbHeigth);

                // Now associate the convertView with our viewHolder, so that we can later use it
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.txtDesc.setText(rowItem.getDesc());
            String imageName = rowItem.getImageName();
            Log.d(TAG, "trying to find from asset: " + imageName );
            try {
//                viewHolder.imageView.setImageDrawable(MediaUtil.getBitmapDrawable((Form) getParent(), imageName));
//                viewHolder.imageView.setImageDrawable(MediaUtil.getBitmapDrawable((Form) this.mContext, imageName)); // doesn't work; can't cast
//                viewHolder.imageView.setImageDrawable(MediaUtil.getBitmapDrawable(mContext , imageName));
                viewHolder.imageView.setImageBitmap(getAsset(mContext, imageName));
                viewHolder.imageView.setMaxWidth(width);
                viewHolder.imageView.setMaxHeight(height);

            } catch (Exception e) {
                // Exception. Try to get it directly from assets
                Log.d(TAG, "Unable to get media file: " + imageName + ". Error:" + e.getMessage());
//                viewHolder.imageView.setImageDrawable(getAsset(mContext, imageName));
            }
            return convertView;
        }

        private void setWidthHeight(View view, int w, int h) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = w;
            params.height = h;
            view.setLayoutParams(params);
        }

        private Bitmap getAsset(Context contex, String assetName) {
            Log.d(TAG, "Starting to get asset: " + assetName);
            Log.d(TAG, "Got asset manager" );

            try {
                if (isRepl) {
                    Bitmap bitmap = BitmapFactory.decodeFile(new java.io.File("/mnt/sdcard/AppInventor/assets/" + assetName).getAbsolutePath());
                    Log.d(TAG, "Was able to get repl asset" );
                    return bitmap;
                }

                // we are dealing with .apk
                if (assetManager == null) {
                    assetManager = contex.getAssets();
                }

                Bitmap bitmap = BitmapFactory.decodeStream(assetManager.open(assetName));
                Log.d(TAG, "Was able to get apk asset" );
                return bitmap;

            } catch (IOException e) {
                Log.d(TAG, "Unable to get asset: " + assetName + ", " + e.getMessage());
            }

            return null;
        }
    }

    @SimpleProperty
    public void ThumbnailWidth(int width) {
        adapter.setWidth(width);
        listView.setAdapter(adapter);
        listView.requestLayout();
    }

    @SimpleProperty
    public int ThumbnailWidth() {
        return adapter.getWidth();
    }

    @SimpleProperty
    public void ThumbnailHeight(int height) {
        adapter.setHeight(height);
        listView.setAdapter(adapter);
        listView.requestLayout();
    }

    @SimpleProperty
    public int ThumbnailHeight() {
        return adapter.getHeight();
    }


    private ArrayList<String> itemsToArrayList() {
        if (items == null || items.length == 0) {
            Log.d(TAG, "items size is:" + items.length);

            return new ArrayList<String>(0);
        }

        ArrayList<String> alistItems = new ArrayList<String>(items.length);
        Collections.addAll(alistItems, items);
        Log.d(TAG, "added items to aListItems");

        return alistItems;
    }

    //=======================================
//    public class MyAdapter extends android.widget.ArrayAdapter<RowItem> {
//
//        private static final String TAG = "MyAdapter";
//        private final Context mContext;
//        public int setThumbWidth=80;
//        public int setThumbHeigth=80;
//        public boolean showText=true;
//
//        public MyAdapter(final Context context, int resourceId, final List<RowItem> items) {
//            super(context, resourceId, items);
//            this.mContext = context;
//        }
//
//
//        // nice tut, but won't be good for my work: http://www.viralandroid.com/2016/02/android-listview-with-image-and-text.html
////http://theopentutorials.com/tutorials/android/listview/android-custom-listview-with-image-and-text-using-arrayadapter/
//        //http://www.android-examples.com/android-custom-listview-with-imageview-and-textview-using-arrayadapter-string/
//        /*private view holder class*/
//        private class ViewHolder {
//            ImageView imageView;
//            TextView txtDesc;
//        }
//
//        @Override
//        public long getItemId(final int position) {
//            return getItem(position).hashCode();
//        }
//
//        @Override
//        public View getView(final int position, View convertView, final ViewGroup parent) {
//            RowItem rowItem = getItem(position);
//
//            ViewHolder viewHolder = null;
//            if (convertView == null) {
//
//                // programmatically find lvImageText.xml layout from resources
//                Resources res = mContext.getResources();
//                int myLayout = res.getIdentifier("list_item", "layout", mContext.getPackageName());
//
//                // after layout is found, then inflate it and assign it to convertView
//                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//                convertView = mInflater.inflate(myLayout, null);
//
//                // from the inflated convertView (which is actually lvImageText.xml), find the desc and icon IDs,
//                //  and then set it into our viewHolder
//                viewHolder = new com.google.appinventor.components.runtime.MyAdapter.ViewHolder();
//                viewHolder.txtDesc = (TextView) convertView.findViewById(res.getIdentifier("desc", "id", mContext.getPackageName()));
//                viewHolder.imageView = (ImageView) convertView.findViewById(res.getIdentifier("icon", "id", mContext.getPackageName()));
//
//                // do we want to show text or make it invisible?
//                if (!showText) {
//                    viewHolder.txtDesc.setVisibility(View.INVISIBLE);
//                }
//
////            viewHolder.txtDesc.setGravity(Gravity.CENTER);
//
//                setWidthHeight(viewHolder.imageView, setThumbWidth, setThumbHeigth);
//
//                // Now associate the convertView with our viewHolder, so that we can later use it
//                convertView.setTag(viewHolder);
//
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//
//            viewHolder.txtDesc.setText(rowItem.getDesc());
//            String imageName = rowItem.getImageName();
//            try {
//                viewHolder.imageView.setImageDrawable(getAsset(mContext, imageName));
//            } catch (Exception e) {
//                Log.d(TAG, "Error loading image: " + imageName);
//            }
//
////            viewHolder.imageView.setImageResource(rowItem.getImageName());
//
////            RowItem rowItem = getItem(position);
////
////            tv.setText(rowItem.getDesc());
////            tv.setTextColor(itemColor);
//            return convertView;
//        }
//
//        private void setWidthHeight(View view, int w, int h) {
//            ViewGroup.LayoutParams params=view.getLayoutParams();
//            params.width=w;
//            params.height=h;
//            view.setLayoutParams(params);
//        }
//
//        public Drawable getDrawable(Context context, String name) {
//            int resourceId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
//            return context.getResources().getDrawable(resourceId);
//        }
//
//        public Drawable getAsset(Context contex, String assetName) {
//            AssetManager assetManager = contex.getAssets();
//
//
//            try {
//                Drawable drawable = Drawable.createFromStream(assetManager.open(assetName), null);
////            InputStream is = assetManager.open(assetName);
////            BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
//                return drawable;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//
//    }
    //=======================================
    public class RowItem {
        private String imageName;
        //        private String title;
        private String desc;

        public RowItem(String desc, String imageName) {
            this.imageName = imageName;
//            this.title = title;
            this.desc = desc;
        }

        public String getImageName() {
            return imageName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        /*public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }*/
        @Override
        public String toString() {
            return imageName + "," + desc;
        }
    }

        private int convertDpToDensity(int value) {
        // convert dp to pixels
        double density = getResources().getDisplayMetrics().density;
        double pixels = value * density;
        return (int) pixels;
    }
}

