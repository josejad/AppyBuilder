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
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.appinventor.components.annotations.*;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.*;

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
    description = "<p>This is a visible component that displays a list of text elements." +
        " <br> The list can be set using the ElementsFromString property" +
        " or using the Elements block in the blocks editor. </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/listView.png")
@SimpleObject
@UsesLibraries(libraries = "listviewanimations.jar")
public final class ListView extends AndroidViewComponent implements AdapterView.OnItemClickListener, OnDismissCallback {
    MyAdapter mAdapter;
    private String animationStyle;

  private static final String LOG_TAG = "ListView";

    private final android.widget.ListView listView;
  private EditText txtSearchBox;
  protected final ComponentContainer container;
  private final LinearLayout listViewLayout;

  // The adapter contains spannables rather than strings, since we will be changing the item
  // colors using ForegroundColorSpan
//  private ArrayAdapter<Spannable> adapter;
  private YailList items;
    private ArrayList<String> itemsArrayList = new ArrayList<String>();

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
    private String filterBarText="Search list..."; //This is the text that will be displayed for FilterBox.

  private int selectionColor;
  private static final int DEFAULT_SELECTION_COLOR = Component.COLOR_LTGRAY;

//    private int textSize;
//    private static final int DEFAULT_TEXT_SIZE = 22;

  /**
   * Creates a new ListView component.
   * @param container  container that the component will be placed in
   */
  public ListView(ComponentContainer container) {
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
        itemsArrayList = itemsToArrayList(items.toStringArray());
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
        itemsArrayList = itemsToArrayList(items.toStringArray());
        mAdapter = new MyAdapter(container.$context(), itemsArrayList);

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
        this.selection = (String) parent.getAdapter().getItem(position);
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
            deletedItem = mAdapter.get(position);
            mAdapter.remove(position);
            AfterDeleting(deletedItem, position);
            // there is only one item that gets deleted.
        }
        List<String> origList = Arrays.asList(items.toStringArray());
        ArrayList<String> itemsArrayList = new ArrayList<String>();
        for (String anItem : origList) {
            if (!anItem.contains(deletedItem)) {
                itemsArrayList.add(anItem);
//                break;
            }
        }
        items = YailList.makeList(itemsArrayList);
//        itemsArrayList.remove(reverseSortedPositions);
//        for (String anItem : origList) {
//            if (!itemsArrayList.contains(anItem)) {
//                AfterDeleting(anItem);
//                break;
//            }
//        }
//        items = YailList.makeList(itemsArrayList);

//        hasRemovedItems = true;
    }

    @SimpleEvent(description = "Event raised after an item is deleted")
    public void AfterDeleting(String deletedItem, int position) {
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

    private class MyAdapter extends com.nhaarman.listviewanimations.ArrayAdapter<String> {

        private final android.content.Context mContext;

        public MyAdapter(final android.content.Context context, final ArrayList<String> items) {
            super(items);
            mContext = context;
        }

        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(final int position, final View convertView, final android.view.ViewGroup parent) {
            android.widget.TextView tv = (android.widget.TextView) convertView;
            if (tv == null) {
                //    adapter = new ArrayAdapter<Spannable>(container.$context(), android.R.layout.simple_list_item_1, itemsToColoredText());
                tv = (android.widget.TextView) android.view.LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
                tv.setTextColor(textColor);
//                Spannable str = (Spannable) tv.getText();
//                str.setSpan(new ForegroundColorSpan(textColor),0,str.length(),0);
            }
//            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
//            float dp = 20f;
//            float fpixels = metrics.density * dp;
//            int pixels = (int) (fpixels + 0.5f);
//            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tv.setText(getItem(position));
            tv.setTextSize(TextSize());

            return tv;
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
}
