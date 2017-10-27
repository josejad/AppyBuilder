// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.*;
import java.util.*;

/**
 * ListPickerActivity class - Brings up a list of items specified in an intent
 * and returns the selected item as the result.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author M. Hossein Amerkashi (kkashi01@gmail.com)
 */
public class ListPickerActivity extends Activity implements AdapterView.OnItemClickListener, OnDismissCallback {

  private String closeAnim = "";
    private boolean isSwipeToArchive = false;
  private ListView listView;
    String items[];
  // Listview Adapter
  MyAdapter adapter;
    ArrayList<String> itemsArrayList = new ArrayList<String>();
    ArrayList<String> deletedList = new ArrayList<String>();

  // Search EditText
  EditText txtSearchBox;
    private boolean hasRemovedItems=false;  //If user swipes-to-delete, we need to pass back updated items list

  static int itemColor;
  static int backgroundColor;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout viewLayout = new LinearLayout(this);
    viewLayout.setOrientation(LinearLayout.VERTICAL);


    //Don't setup up style; causes NPE when clicked:
    // Caused by: java.lang.NullPointerException: Attempt to invoke virtual
      // method 'void android.widget.RtlSpacingHelper.setDirection(boolean)' on a null object reference

//      setupPickerStyle();

    Intent myIntent = getIntent();
    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_ANIM_TYPE)) {
      closeAnim = myIntent.getStringExtra(ListPicker.LIST_ACTIVITY_ANIM_TYPE);
    }
    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_ORIENTATION_TYPE)) {
      String orientation = myIntent.getStringExtra(ListPicker.LIST_ACTIVITY_ORIENTATION_TYPE).toLowerCase();
      if (orientation.equals("portrait")) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      }
      else if (orientation.equals("landscape")) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      }
    }

    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_TITLE)) {
      String title = myIntent.getStringExtra(ListPicker.LIST_ACTIVITY_TITLE);
      setTitle(title);
    }
    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_ARG_NAME)) {
                items = getIntent().getStringArrayExtra(ListPicker.LIST_ACTIVITY_ARG_NAME);
      listView = new ListView(this);
      listView.setOnItemClickListener(this);
      itemsArrayList = itemsToArrayList();

      itemColor = myIntent.getIntExtra(ListPicker.LIST_ACTIVITY_ITEM_TEXT_COLOR, ListPicker.DEFAULT_ITEM_TEXT_COLOR);
      backgroundColor = myIntent.getIntExtra(ListPicker.LIST_ACTIVITY_BACKGROUND_COLOR, ListPicker.DEFAULT_ITEM_BACKGROUND_COLOR);

      viewLayout.setBackgroundColor(backgroundColor);

      // Adding items to listview
      adapter = new MyAdapter(this, itemsArrayList);
      setupStyle();

      String showFilterBar =myIntent.getStringExtra(ListPicker.LIST_ACTIVITY_SHOW_SEARCH_BAR);

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
                Toast.makeText(this, "With Swipe, you can't use ShowFilterBar" , Toast.LENGTH_LONG).show();
            }

      //set up the listener
      txtSearchBox.addTextChangedListener(new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
          // When user changed the Text
          ListPickerActivity.this.adapter.getFilter().filter(cs);
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

    }
    else {
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


  private void setupStyle() {
    String animationType =getIntent().getStringExtra(ListPicker.LIST_ACTIVITY_ANIMATION);
    if (animationType==null || animationType.equalsIgnoreCase("default"))  //this is the default
    {
      listView.setAdapter(adapter);
    } else if (animationType.equalsIgnoreCase("bottom")) {
      setBottomAdapter();
    } else if (animationType.equalsIgnoreCase("bottomright")) {
      setBottomRightAdapter();
    } else if (animationType.equalsIgnoreCase("left")) {
      setLeftAdapter();
    } else if (animationType.equalsIgnoreCase("scale")) {
      setScaleAdapter();
    } else if (animationType.equalsIgnoreCase("alpha")) {
      setAlphaAdapter();
    } else if (animationType.equalsIgnoreCase("swipe")) {
      setSwipeDismissAdapter();
      isSwipeToArchive=true;
    } else if (animationType.equalsIgnoreCase("right")) {
      setRightAdapter();
    } else {
      setRightAdapter();
    }


  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    String selected = (String) parent.getAdapter().getItem(position);
    Intent resultIntent = new Intent();
    resultIntent.putExtra(ListPicker.LIST_ACTIVITY_RESULT_NAME, selected);
    resultIntent.putExtra(ListPicker.LIST_ACTIVITY_RESULT_INDEX, position + 1);
    resultIntent.putExtra(ListPicker.LIST_ACTIVITY_ITEMS, itemsArrayList); //add this in case user has removed items
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
    resultIntent.putExtra(ListPicker.LIST_ACTIVITY_ITEMS, itemsArrayList);
    //We don't want to use RESULT_OK because user hasn't selected any item
    setResult(RESULT_CANCELED, resultIntent);
//        }

    finish();
  }

    private void setAlphaAdapter() {
        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(adapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
}

    private void setLeftAdapter() {
        AnimationAdapter animAdapter = new SwingLeftInAnimationAdapter(adapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }

    private void setRightAdapter() {
        AnimationAdapter animAdapter = new SwingRightInAnimationAdapter(adapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }

    private void setBottomAdapter() {
        AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(adapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }

    private void setBottomRightAdapter() {
        AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(adapter));
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }
   private void _setBottomRightAdapter() {
        AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(adapter));
        animAdapter.setAbsListView(listView);

        listView.setAdapter(animAdapter);
    }

    private void setSwipeDismissAdapter() {
        SwipeDismissAdapter adapter = new SwipeDismissAdapter(this.adapter, this);
        adapter.setAbsListView(listView);
        listView.setAdapter(adapter);
    }

    @Override
    public void onDismiss(final AbsListView listView, final int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            adapter.remove(position);
        }
        itemsArrayList.remove(reverseSortedPositions);

//        Toast.makeText(this, "Item deleted!" + java.util.Arrays.toString(reverseSortedPositions), Toast.LENGTH_SHORT).show();
        //Users didn't want 'Item Deleted' to be displayed. They prefer to display in their native language. For now, disabling it
//        Toast.makeText(this, "Item Deleted!" , Toast.LENGTH_SHORT).show();

        hasRemovedItems = true;
    }

    private void setScaleAdapter() {
        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(adapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }

  private static class MyAdapter extends ArrayAdapter<String> {

    private final Context mContext;

        public MyAdapter(final Context context, final ArrayList<String> items) {
            super(items);
      mContext = context;
    }

    @Override
    public long getItemId(final int position) {
      return getItem(position).hashCode();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      TextView tv = (TextView) convertView;
      if (tv == null) {
        tv = (TextView) LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
      }
      tv.setText(getItem(position));
      tv.setTextColor(itemColor);
      return tv;
    }
  }

    private ArrayList<String> itemsToArrayList() {
        if (items == null || items.length==0)
        {
            return new ArrayList<String>(0);
}

        ArrayList<String> alistItems = new ArrayList<String>(items.length);
        Collections.addAll(alistItems, items);

        return alistItems;
    }
}

