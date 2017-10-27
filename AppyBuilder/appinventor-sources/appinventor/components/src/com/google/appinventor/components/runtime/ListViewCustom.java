// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

// good read:   http://hmkcode.com/android-custom-listview-titles-icons-counter/
//              http://www.vogella.com/tutorials/AndroidListView/article.html

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.text.*;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.LinearLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ListView Component. Non-Visible component to create a ListView in the Screen from a series of
 * elements added from a comma separated set of text elements. It is similar to the ListPicker
 * component but this one is placed on screen instead of opening a new Activity.
 * TOFO(hal): Think about generalizing this to include more than text/
 * @author halabelson@google.com (Hal Abelson)
 * @author osmidy@mit.edu (Olivier Midy)
 */

@DesignerComponent(version = YaVersion.LISTVIEW_COMPONENT_VERSION,
    description = "<p>This is a visible component that displays a list of Images and Text elements." +
        " <br> The list can be set using the ElementsFromString property" +
        " or using the Elements block in the blocks editor. </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/listviewcustom.png")
@SimpleObject
@UsesLibraries(libraries = "listviewanimations.jar")
public final class ListViewCustom extends AndroidViewComponent implements AdapterView.OnItemClickListener, OnDismissCallback {
    MyAdapter mAdapter;
    private String animationStyle;

  private static final String LOG_TAG = "ListViewCustom";

    private final android.widget.ListView listView;
  private EditText txtSearchBox;
  protected final ComponentContainer container;
  private final LinearLayout listViewLayout;

  // The adapter contains spannables rather than strings, since we will be changing the item
  // colors using ForegroundColorSpan
//  private ArrayAdapter<Spannable> adapter;
  private YailList items;
//    private ArrayList<String> itemsArrayList = new ArrayList<String>();

  private int selectionIndex;
  private String selection;
  private boolean showFilter = false;
  private static final boolean DEFAULT_ENABLED = false;

  private int backgroundColor;
  private static final int DEFAULT_BACKGROUND_COLOR = Component.COLOR_BLACK;

  // The text color of the ListView's items.  All items have the same text color
  private int textColor;
  private static final int DEFAULT_TEXT_COLOR = Component.COLOR_WHITE;
    private int sortOrder =0;
    private float fontSize = Component.FONT_DEFAULT_SIZE;
    private int imageSize=80;

    private String filterBarText="Search list..."; //This is the text that will be displayed for FilterBox.
    private int selectionColor;
    private static final int DEFAULT_SELECTION_COLOR = Component.COLOR_LTGRAY;
    private int myLayout;
    private Resources resources;
    List<RowItem> listviewItems = new ArrayList<RowItem>();
    private AssetManager assetManager;

//    private int textSize;
//    private static final int DEFAULT_TEXT_SIZE = 22;

  /**
   * Creates a new ListView component.
   * @param container  container that the component will be placed in
   */
  public ListViewCustom(ComponentContainer container) {
    super(container);
    this.container = container;
    items = YailList.makeEmptyList();
    // initialize selectionIndex which also sets selection
    SelectionIndex(0);
        listView = new android.widget.ListView(container.$context());
        listView.setOnItemClickListener(this);
    listViewLayout = new LinearLayout(container.$context());
    listViewLayout.setOrientation(LinearLayout.VERTICAL);

    txtSearchBox = new EditText(container.$context());
    txtSearchBox.setSingleLine(true);
    txtSearchBox.setWidth(Component.LENGTH_FILL_PARENT);
    txtSearchBox.setPadding(10, 10, 10, 10);
        txtSearchBox.setHint(filterBarText);
        SortOrder(0);
    //set up the listener
    txtSearchBox.addTextChangedListener(new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
          // When user changed the Text
                mAdapter.getFilter().filter(cs);
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

        setAdapterData();

    if (showFilter) {
      txtSearchBox.setVisibility(View.VISIBLE);
    } else {
      txtSearchBox.setVisibility(View.GONE);
    }

    // set the colors and initialize the elements
    // note that the TextColor and ElementsFromString setters
    // need to have the textColor set first, since they reset the
    // adapter

    Width(Component.LENGTH_FILL_PARENT);
    BackgroundColor(DEFAULT_BACKGROUND_COLOR);
    SelectionColor(DEFAULT_SELECTION_COLOR);

    textColor = DEFAULT_TEXT_COLOR;
    TextColor(textColor);
        fontSize = Component.FONT_DEFAULT_SIZE;
        TextSize(fontSize);
    ElementsFromString("");

    listViewLayout.addView(txtSearchBox);
        listViewLayout.addView(listView);
    listViewLayout.requestLayout();
    container.$add(this);
  };

  @Override
  public View getView() {
    return listViewLayout;
  }

  /**
  * Sets the height of the listView on the screen
  * @param height for height length
  */
  @Override
  @SimpleProperty(description = "Determines the height of the list on the view.",
      category =PropertyCategory.APPEARANCE)
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }

  /**
  * Sets the width of the listView on the screen
  * @param width for width length
  */
  @Override
  @SimpleProperty(description = "Determines the width of the list on the view.",
      category = PropertyCategory.APPEARANCE)
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }

  /**
   * Sets true or false to determine whether the search filter box is displayed in the ListView
   *
   * @param showFilter set the visibility according to this input
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = DEFAULT_ENABLED ? "True" : "False")
  @SimpleProperty(description = "Sets visibility of ShowFilterBar. True will show the bar, " +
      "False will hide it.")
  public void ShowFilterBar(boolean showFilter) {
    this.showFilter = showFilter;
    if (showFilter) {
      txtSearchBox.setVisibility(View.VISIBLE);
    }
    else {
      txtSearchBox.setVisibility(View.GONE);
    }
  }

  /**
   * Returns true or false depending on the visibility of the Filter bar element
   * @return true or false (visibility)
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns current state of ShowFilterBar for visibility.")
  public boolean ShowFilterBar() {
    return showFilter;
  }

  /**
   * Set a list of text elements to build a ListView
   * @param itemsList a YailList containing the strings to be added to the ListView
   */
  @SimpleProperty(description="List of text elements to show in the ListView.  This will" +
                "signal an error if the elements are not text strings.",
      category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList itemsList) {
    items = ElementsUtil.elements(itemsList, "Listview");
//        itemsArrayList = itemsToArrayList(items.toStringArray());
    setAdapterData();
  }

  /**
   * Elements property getter method
   *
   * @return a YailList representing the list of strings to be picked from
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public YailList Elements() {
    return items;
  }

  /**
   * Specifies the text elements of the ListView.
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description="The TextView elements specified as a string with the " +
      "items separated by commas " +
      "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the " +
      "list.",  category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring) {
    items = ElementsUtil.elementsFromString(itemstring);
    setAdapterData();
  }

  /**
   * Sets the items of the ListView through an adapter
   */
  public void setAdapterData(){
//      itemsArrayList = itemsToArrayList(items.toStringArray());
      listviewItems = new ArrayList<RowItem>();
      for (String anItem : itemsToArrayList(items.toStringArray())) {
          String[] temp = anItem.split("\\|");
          if (temp.length > 1) {
              // we have image and text.
              listviewItems.add(new RowItem(temp[0], temp[1]));   //0=desc, 1=imagePath
          } else {
              // we only have text. 0th position is the text
              listviewItems.add(new RowItem(temp[0], "NO_IMAGE_SPECIFIED"));
          }
      }

      resources = container.$form().getResources();
      myLayout = resources.getIdentifier("lvimageview", "layout", container.$form().getPackageName());

      mAdapter = new MyAdapter(container.$context(), listviewItems);

        listView.setAdapter(mAdapter);
//      adapter = new ArrayAdapter<Spannable>(container.$context(), android.R.layout.simple_list_item_1, itemsToColoredText());
//      listView.setAdapter(adapter);
  }

  public Spannable[] itemsToColoredText() {
    // TODO(hal): Generalize this so that different items could have different
    // colors and even fonts and sizes
    int size = items.size();
//        int displayTextSize = Component.FONT_DEFAULT_SIZE;
    Spannable [] objects = new Spannable[size];
    for (int i = 1; i <= size; i++) {
//      String itemString = items.getString(i).toString(); // used if we use YailList as Array (new version of java bridge)
            String itemString = items.get(i).toString();
            // Is there a more efficient way to do this that does not
      // need to allocate new objects?
      Spannable chars = new SpannableString(itemString);
      chars.setSpan(new ForegroundColorSpan(textColor),0,chars.length(),0);
            chars.setSpan(new AbsoluteSizeSpan((int) TextSize()),0,chars.length(),0);
      objects[i - 1] = chars;
    }
    return objects;
  }

  /**
   * Selection index property getter method.
   */
  @SimpleProperty(
      description = "The index of the currently selected item, starting at " +
          "1.  If no item is selected, the value will be 0.  If an attempt is " +
          "made to set this to a number less than 1 or greater than the number " +
          "of items in the ListView, SelectionIndex will be set to 0, and " +
          "Selection will be set to the empty text.",
      category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex() {
    return selectionIndex;
  }

  /**
   * Sets the index to the passed argument for selection
   * @param index the index to be selected
   */
  @SimpleProperty(description="Specifies the position of the selected item in the ListView. " +
      "This could be used to retrieve" +
      "the text at the chosen position. If an attempt is made to set this to a " +
      "number less than 1 or greater than the number of items in the ListView, SelectionIndex " +
      "will be set to 0, and Selection will be set to the empty text."
      ,
      category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int index){
    selectionIndex = ElementsUtil.selectionIndex(index, items);
    // Now, we need to change Selection to correspond to SelectionIndex.
    selection = ElementsUtil.setSelectionFromIndex(index, items);
  }

  /**
  * Returns the text in the ListView at the position set by SelectionIndex
  */
  @SimpleProperty(description="Returns the text last selected in the ListView.",
      category = PropertyCategory
      .BEHAVIOR)
  public String Selection(){
      return selection;
  }

  /**
   * Selection property setter method.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Selection(String value) {
    selection = value;
    // Now, we need to change SelectionIndex to correspond to Selection.
    selectionIndex = ElementsUtil.setSelectedIndexFromValue(value, items);
  }

  /**
   * Simple event to raise when the component is clicked. Implementation of
   * AdapterView.OnItemClickListener
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      RowItem rowItem = (RowItem) parent.getAdapter().getItem(position);
      String selected = "";
      if (rowItem != null) {
          selected = rowItem.getDesc();
      }

      this.selection = selected;
        this.selectionIndex = position + 1;
    AfterPicking();
  }

  /**
   * Simple event to be raised after the an element has been chosen in the list.
   * The selected element is available in the Selection property.
   */
  @SimpleEvent(description = "Simple event to be raised after the an element has been chosen in the" +
      " list. The selected element is available in the Selection property.")
  public void AfterPicking() {
    EventDispatcher.dispatchEvent(this, "AfterPicking");
  }

  /**
   * Assigns a value to the backgroundColor
   * @param color  an alpha-red-green-blue integer for a color
   */

  public void setBackgroundColor(int color) {
      backgroundColor = color;
        listView.setBackgroundColor(backgroundColor);
      listViewLayout.setBackgroundColor(backgroundColor);
      // Keeps background color behind list elements correct when scrolling through listView
        listView.setCacheColorHint(backgroundColor);
  }

  /**
   * Returns the listview's background color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @return background color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(
      description = "The color of the listview background.",
      category = PropertyCategory.APPEARANCE)
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the ListView's background color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @param argb background color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty
  public void BackgroundColor(int argb) {
      backgroundColor = argb;
      setBackgroundColor(backgroundColor);
  }

  /**
   * Returns the listview's selection color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   * Is not supported on Icecream Sandwich or earlier
   *
   * @return selection color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(description = "The color of the item when it is selected.")
  public int SelectionColor() {
    return selectionColor;
  }

  /**
   * Specifies the ListView's selection color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   * Is not supported on Icecream Sandwich or earlier
   *
   * @param argb selection color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
  @SimpleProperty
  public void SelectionColor(int argb) {
    selectionColor = argb;
        listView.setSelector(new GradientDrawable(
      GradientDrawable.Orientation.TOP_BOTTOM, new int[]{argb, argb}
    ));
  }

  /**
   * Returns the listview's text item color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @return background color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(
      description = "The text color of the listview items.",
      category = PropertyCategory.APPEARANCE)
  public int TextColor() {
    return textColor;
  }

  /**
   * Specifies the ListView item's text color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @param argb background color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void TextColor(int argb) {
      textColor = argb;
      setAdapterData();
  }
//    /**
//     * Returns the listview's text font Size
//     *
//     * @return text size as an float
//     */
//    @SimpleProperty(
//            description = "The text size of the listview items.",
//            category = PropertyCategory.APPEARANCE)
//    public int TextSize() {
//        return textSize;
//    }
//
//    /**
//     * Specifies the ListView item's text font size
//     *
//     * @param fontSize value for font size
//     */
//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
//            defaultValue = DEFAULT_TEXT_SIZE + "")
//    @SimpleProperty
//    public void TextSize(int fontSize) {
//        if(fontSize>1000)
//            textSize = 999;
//        else
//            textSize = fontSize;
//        setAdapterData();
//    }
  /**
     * Create a YailList from an array. Items will be sorted based on sortOrder.
     * If 0 or invalid value, then no sorting will be performed.
     * Note: Currently only Strings are supported
     * @param sortOrder indicates sort order where:
     *                  <ul>
     *                    <li>-1 = Descending</li>
     *                    <li>0 = no sort,</li>
     *                    <li>1 = Ascending</li>
     *                  </ul>
     * @return
   */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SORT_OPTIONS, defaultValue = "0")
    @SimpleProperty(description = "Allows you to specify a sort order for the ListPicker. " +
            "The ListPicker can be sorted in Descending(-1), None (0), Ascending(1) ")
    public void SortOrder(int sortOrder) {
        if (null == items || items.size() == 0 ) {
            return;
        }

        String[] strArray = items.toStringArray();
        if (sortOrder == 1 || sortOrder == -1) {
            if (sortOrder == 1) Arrays.sort(strArray);
            else Arrays.sort(strArray, Collections.reverseOrder());
            Elements(YailList.makeList(strArray));
            this.sortOrder=sortOrder;
        }
    }

    @SimpleProperty(description = "Returns the ListPicker SortOrder", category = PropertyCategory.BEHAVIOR)
    public int SortOrder() {
        return sortOrder;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_ITEM_OPTIONS, defaultValue = "Default")
    @SimpleProperty(description = "Sets up the ListView item style. " +
            "Valid Styles are: Default, Swipe")
    public void ListViewItemStyle(String style) {
        if (!"default swipe ".contains(style.toLowerCase())) {
            throw new IllegalStateException("Invalid ListView Style.");
        }
        this.animationStyle = style;
        setAdapterData();
        setupStyle();
    }

    @SimpleProperty(description = "The font size of the ListView items",
      category = PropertyCategory.APPEARANCE)
    public float TextSize() {
        return this.fontSize;
  }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
  @SimpleProperty
    public void TextSize(float size) {
        this.fontSize = size;
  }

    @SimpleProperty(description = "The size of image - Don't use over 80", category = PropertyCategory.APPEARANCE)
    public int ImageSize() {
        return this.imageSize;
  }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "80")
  @SimpleProperty
    public void ImageSize(int imageSize) {
        this.imageSize = imageSize;
  }


    private void setupStyle() {

        if (animationStyle==null || animationStyle.equalsIgnoreCase("default"))  //this is the default
        {
            listView.setAdapter(mAdapter);
        } else if (animationStyle.equalsIgnoreCase("bottom")) {
            setBottomAdapter();
        } else if (animationStyle.equalsIgnoreCase("bottomright")) {
            setBottomRightAdapter();
        } else if (animationStyle.equalsIgnoreCase("left")) {
            setLeftAdapter();
        } else if (animationStyle.equalsIgnoreCase("scale")) {
            setScaleAdapter();
        } else if (animationStyle.equalsIgnoreCase("alpha")) {
            setAlphaAdapter();
        } else if (animationStyle.equalsIgnoreCase("swipe")) {
            setSwipeDismissAdapter();
//            isSwipeToArchive=true;
        } else if (animationStyle.equalsIgnoreCase("right")) {
            setRightAdapter();
        } else {
            setRightAdapter();
        }
}

    private void setScaleAdapter() {
        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(mAdapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }
    private void setAlphaAdapter() {
        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(mAdapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }

    private void setLeftAdapter() {
        AnimationAdapter animAdapter = new SwingLeftInAnimationAdapter(mAdapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }

    private void setRightAdapter() {
        AnimationAdapter animAdapter = new SwingRightInAnimationAdapter(mAdapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }

    private void setBottomAdapter() {
        AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(mAdapter);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }

    private void setBottomRightAdapter() {
        AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(mAdapter));
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
    }
    private void _setBottomRightAdapter() {
        AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(mAdapter));
        animAdapter.setAbsListView(listView);

        listView.setAdapter(animAdapter);
    }

    private void setSwipeDismissAdapter() {
        SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter, this);
        adapter.setAbsListView(listView);
        listView.setAdapter(adapter);
    }


    @Override
    public void onDismiss(final android.widget.AbsListView listView, final int[] reverseSortedPositions) {
        String deletedItem="";
        for (int position : reverseSortedPositions) {
            deletedItem = mAdapter.get(position).getDesc();
            mAdapter.remove(position);
            AfterDeleting(deletedItem, position);
            // there is only one item that gets deleted.
        }
//        itemsArrayList.remove(reverseSortedPositions);
        List<String> origList = Arrays.asList(items.toStringArray());
        ArrayList<String> itemsArrayList = new ArrayList<String>();

        for (String anItem : origList) {
            if (!anItem.contains(deletedItem)) {
                itemsArrayList.add(anItem);
//                break;
            }
        }
        items = YailList.makeList(itemsArrayList);

//        hasRemovedItems = true;
    }

    @SimpleEvent(description = "Event raised after an item is deleted")
    public void AfterDeleting(String deletedItem, int position) {
        // position is zero-based. Make it 1 based
        position = position + 1;
        EventDispatcher.dispatchEvent(this, "AfterDeleting", deletedItem, position);
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Returns the text that will display at top of FilterBar")
    public String FilterBarText() {
        return filterBarText;
    }

    //Too many properties in designer. We only show this in the block editor
//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "Allows you to setup a custom text to display for the ShowFilterBar. Default is 'Search list...'")
    public void FilterBarText(String filterBarText) {
        // set the default search string
        this.filterBarText = "Search List...";
        if (!(filterBarText == null || filterBarText.trim().equalsIgnoreCase(""))) {
            // user entered valid string. Update the default
            this.filterBarText = filterBarText;
        }
        txtSearchBox.setHint(this.filterBarText);
    }

    private class MyAdapter extends ArrayAdapter<RowItem> {

        private final Context mContext;
        //        public int setThumbWidth = 80;
//        public int setThumbHeigth = 80;
        public boolean showText = true;
//        private int width = 80;
//        private int height = 80;

        public MyAdapter(final Context context, final List<RowItem> items) {
            super(items);
            mContext = context;
        }

//        public void setWidth(int width) {
//            this.width = width;
//        }
//
//        public int getWidth() {
//            return this.width;
//        }
//
//        public void setHeight(int height) {
//            this.height = height;
//        }
//
//        public int getHeight() {
//            return this.height;
//        }

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
            Log.d(LOG_TAG, "starting to get item at position " +position);

            MyAdapter.ViewHolder viewHolder = null;
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
                viewHolder = new MyAdapter.ViewHolder();
                viewHolder.txtDesc = (TextView) convertView.findViewById(resources.getIdentifier("desc", "id", mContext.getPackageName()));
                viewHolder.txtDesc.setTextColor(textColor);
                viewHolder.imageView = (ImageView) convertView.findViewById(resources.getIdentifier("icon", "id", mContext.getPackageName()));

                viewHolder.imageView.getLayoutParams().height = container.$form().convertDpToDensity(ImageSize());
                viewHolder.imageView.getLayoutParams().width = container.$form().convertDpToDensity(ImageSize());


                // Now associate the convertView with our viewHolder, so that we can later use it
// do we want to show text or make it invisible?
                if (!showText) {
                    viewHolder.txtDesc.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.txtDesc.setTextSize(fontSize);
                }

//                viewHolder.txtDesc.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                viewHolder.txtDesc.setGravity(Gravity.LEFT);

//                setWidthHeight(viewHolder.imageView, setThumbWidth, setThumbHeigth);

                // Now associate the convertView with our viewHolder, so that we can later use it
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MyAdapter.ViewHolder) convertView.getTag();
            }

            Spanned htmlAsSpanned = Html.fromHtml(rowItem.getDesc());
//            viewHolder.txtDesc.setText(rowItem.getDesc());
            viewHolder.txtDesc.setText(htmlAsSpanned);
            String imageName = rowItem.getImageName();
            Log.d(LOG_TAG, "trying to find from asset: " + imageName );
            try {
//                viewHolder.imageView.setImageDrawable(MediaUtil.getBitmapDrawable((Form) getParent(), imageName));
//                viewHolder.imageView.setImageDrawable(MediaUtil.getBitmapDrawable((Form) this.mContext, imageName)); // doesn't work; can't cast
//                viewHolder.imageView.setImageDrawable(MediaUtil.getBitmapDrawable(mContext , imageName));
                viewHolder.imageView.setImageBitmap(getAsset(mContext, imageName));
                viewHolder.imageView.setMaxWidth(container.$form().convertDpToDensity(ImageSize()));
                viewHolder.imageView.setMaxHeight(container.$form().convertDpToDensity(ImageSize()));

            } catch (Exception e) {
                // Exception. Try to get it directly from assets
                Log.d(LOG_TAG, "Unable to get media file: " + imageName + ". Error:" + e.getMessage());
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
            Log.d(LOG_TAG, "Starting to get asset: " + assetName);
            Log.d(LOG_TAG, "Got asset manager" );

            try {
                if (container.$form() instanceof ReplForm) {
                    Bitmap bitmap = BitmapFactory.decodeFile(new java.io.File("/mnt/sdcard/AppInventor/assets/" + assetName).getAbsolutePath());
                    Log.d(LOG_TAG, "Was able to get repl asset" );
                    return bitmap;
                }

                // we are dealing with .apk
                if (assetManager == null) {
                    assetManager = contex.getAssets();
                }

                Bitmap bitmap = BitmapFactory.decodeStream(assetManager.open(assetName));
                Log.d(LOG_TAG, "Was able to get apk asset" );
                return bitmap;

            } catch (IOException e) {
                Log.d(LOG_TAG, "Unable to get asset: " + assetName + ", " + e.getMessage());
            }

            return null;
        }
    }

    private ArrayList<String> itemsToArrayList(String[] items) {
        if (items == null || items.length==0)
        {
            return new ArrayList<String>(0);
        }

        ArrayList<String> alistItems = new ArrayList<String>(items.length);
        Collections.addAll(alistItems, items);

        return alistItems;
    }

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

    @SimpleFunction(description = "List 1 would be text to display and list 2 will be image to display")
    public void ElementsFromLists(YailList listItems, YailList listImages) {
        String[] strArrayListItems = listItems.toStringArray();
        String[] strArrayListImages = listImages.toStringArray();

//    Log.d(LOG_TAG, "items:" + Arrays.toString(strArrayListItems));
//    Log.d(LOG_TAG, "images:" + Arrays.toString(strArrayListImages));

        String csvItems="";
        for (int i=0; i<strArrayListItems.length; i++) {
            csvItems = csvItems + "," + strArrayListItems[i] + "|" + (i < strArrayListImages.length?strArrayListImages[i]:"NO_IMAGE");
        }
        if (!csvItems.equals("")) {
            csvItems = csvItems.substring(1); //get rid of 1st comma
        }
//    Log.d(LOG_TAG, "new list is:{"+csvItems+"}");
        items = ElementsUtil.elementsFromString(csvItems);
        setAdapterData();
    }
}
