// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A button allowing a user to select one among a list of text strings.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author M. Hossein Amerkashi (kkashi01@gmail.com)
 */
@DesignerComponent(version = YaVersion.LISTPICKER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "<p>A button that, when clicked on, displays a list of " +
    "Images and Texts for the user to choose among. These images and texts can be specified through " +
    "the Designer or Blocks Editor by setting the " +
    "<code>ElementsFromString</code> property to their string-separated " +
    "concatenation (for example, <em>choice 1|choice1.png, choice 2|choice2.png, choice 3|choice3.png</em>) or " +
    "by setting the <code>Elements</code> property to a List in the Blocks " +
    "editor.</p>" +
    "<p>Setting property ShowFilterBar to true, will make the list searchable.  " +
    "Other properties affect the appearance of the button " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be clicked on (<code>Enabled</code>).</p>" +
    "<p>Use the ListPickerStyle to change the appearance when the ListPicker is opened." +
    "<p>Use the ListPickerItemStyle to change the how the items appear in the picker. You can " +
    "use Right, Left, Bottom, BottomRight, Alpha, Scale. Use Swipe if you want to to " +
    "provide ability to swipe-to-delete ListPicker items.")
@SimpleObject
@UsesActivities(activities = {
    @ActivityElement(name = "com.google.appinventor.components.runtime.ListPickerCustomActivity",
                     configChanges = "orientation|keyboardHidden",
                     screenOrientation = "behind")
})
@UsesLibraries(libraries = "listviewanimations.jar")
public class ListPickerCustom extends Picker implements ActivityResultListener, Deleteable, OnResumeListener {

  private static final String LIST_ACTIVITY_CLASS = ListPickerCustomActivity.class.getName();
  static final String LIST_ACTIVITY_ARG_NAME = LIST_ACTIVITY_CLASS + ".list";
  static final String LIST_ACTIVITY_RESULT_NAME = LIST_ACTIVITY_CLASS + ".selection";
  static final String LIST_ACTIVITY_RESULT_INDEX = LIST_ACTIVITY_CLASS + ".index";
  static final String LIST_ACTIVITY_ITEMS = LIST_ACTIVITY_CLASS + ".items";
  static final String LIST_ACTIVITY_ITEMS_DELETED = LIST_ACTIVITY_CLASS + ".deleteditems";
  static final String LIST_ACTIVITY_ANIMATION = LIST_ACTIVITY_CLASS + ".animation.style";
//  static final String LIST_ACTIVITY_STYLE = LIST_ACTIVITY_CLASS + ".style";
  static final String LIST_ACTIVITY_ANIM_TYPE = LIST_ACTIVITY_CLASS + ".anim";
  static final String LIST_ACTIVITY_SHOW_SEARCH_BAR = LIST_ACTIVITY_CLASS + ".search";
//  static final String LIST_ACTIVITY_SHOW_SEARCH_BAR_TEXT = LIST_ACTIVITY_CLASS + ".search.text";
  static final String LIST_ACTIVITY_TITLE = LIST_ACTIVITY_CLASS + ".title";
  static final String LIST_ACTIVITY_ORIENTATION_TYPE = LIST_ACTIVITY_CLASS + ".orientation";
  static final String LIST_ACTIVITY_ITEM_TEXT_COLOR = LIST_ACTIVITY_CLASS + ".itemtextcolor";
  static final String LIST_ACTIVITY_BACKGROUND_COLOR = LIST_ACTIVITY_CLASS + ".backgroundcolor";
  static final String LIST_ACTIVITY_IS_REPL = LIST_ACTIVITY_CLASS + ".isrepl";
  private boolean isRepl=false;

  private YailList items;
  private String selection;
  private int selectionIndex;
  private int sortOrder =0;
  private boolean showFilter =false;
  private static final boolean DEFAULT_ENABLED = false;
  private String title = "";    // The Title to display the List Picker with
                                // if left blank, the App Name is used instead
  private String animationStyle;
//  private String listActivityStyle; //Light or Dialog or ...
  private boolean resumedFromListFlag  = false; //flag so onResume knows if the resume was triggered by closing the listpicker activity

  private YailList deletedItems = new YailList();

  private int itemTextColor;
  private int itemBackgroundColor;
  public final static int DEFAULT_ITEM_TEXT_COLOR = Component.COLOR_WHITE;
  public final static int DEFAULT_ITEM_BACKGROUND_COLOR = Component.COLOR_BLACK;

  private Form form;
  private String LOG_TAG="ListPickerCustom";

  /**
   * Create a new ListPicker component.
   *
   * @param container the parent container.
   */
  public ListPickerCustom(ComponentContainer container) {
    super(container);
    items = new YailList();
    // initialize selectionIndex which also sets selection
    SelectionIndex(0);
    itemTextColor = DEFAULT_ITEM_TEXT_COLOR;
    itemBackgroundColor = DEFAULT_ITEM_BACKGROUND_COLOR;

    container.$form().registerForOnResume(this);
    if (container.$form() instanceof ReplForm) {
      isRepl = true;
    }
  }

  @Override
  public void onResume() {
    if (resumedFromListFlag) {
      container.$form().getWindow().setSoftInputMode(
              WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
      );
      resumedFromListFlag = false;
    }
  }

  /**
   * Selection property getter method.
   */
  @SimpleProperty(
      description = "The selected item.  When directly changed by the " +
      "programmer, the SelectionIndex property is also changed to the first " +
      "item in the ListPicker with the given value.  If the value does not " +
      "appear, SelectionIndex will be set to 0.",
      category = PropertyCategory.BEHAVIOR)
  public String Selection() {
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

    /*
     * Specifies the Version Name.
     *
     * @param listpickerStyle ListPicker style; Default or Light or Dialog
     */
    /*
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_PICKER_STYLE_OPTIONS, defaultValue = "Default")
    @SimpleProperty
    public void ListPickerStyle(String listpickerStyle) {
        this.listActivityStyle=listpickerStyle;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE  )
    public String ListPickerStyle() {
        return this.listActivityStyle;
    }*/

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_PICKER_ITEM_OPTIONS, defaultValue = "Default")
  @SimpleProperty(description = "Sets up the ListPicker item style. " +
          "Valid Styles are: Default, Alpha, Left, Right, Bottom, BottomRight, Scale, Swipe")
  public void ListPickerItemStyle(String style) {
    if (!"default left right bottom alpha swipe scale bottomright".contains(style.toLowerCase())) {
      throw new IllegalStateException("Invalid PickerItem Style.");
    }
    this.animationStyle = style;
  }

  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Sets up the ListPicker item style. " +
          "Valid Styles are: Default, Alpha, Left, Right, Bottom, BottomRight, Scale, Swipe")
  public String ListPickerItemStyle() {
    return this.animationStyle;
  }

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
//      if (sortOrder == 1)
//      {
//          Arrays.sort(strArray);
//          Elements(YailList.makeList(strArray));
//          this.sortOrder=sortOrder;
//      } else if (sortOrder == -1) {
//          Arrays.sort(strArray, Collections.reverseOrder());
//          Elements(YailList.makeList(strArray));
//          this.sortOrder=sortOrder;
//      }
    }

  @SimpleProperty(description = "Returns the ListPicker SortOrder", category = PropertyCategory.BEHAVIOR)
  public int SortOrder() {
    return sortOrder;
  }

  @DesignerProperty(
    editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = DEFAULT_ENABLED ? "True" : "False")
  @SimpleProperty
  public void ShowFilterBar(boolean showFilter) {
    this.showFilter = showFilter;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns current state of ShowFilterBar indicating if " +
          "Search Filter Bar will be displayed on ListPicker or not")
  public boolean ShowFilterBar() {
    return showFilter;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void ItemTextColor(int argb) {
    this.itemTextColor = argb;
  }

  @SimpleProperty(description = "The text color of the ListPicker items.",
      category = PropertyCategory.APPEARANCE)
  public int ItemTextColor() {
    return this.itemTextColor;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty
  public void ItemBackgroundColor(int argb) {
    this.itemBackgroundColor = argb;
  }

  @SimpleProperty(description = "The background color of the ListPicker items.",
      category = PropertyCategory.APPEARANCE)
  public int ItemBackgroundColor() {
    return this.itemBackgroundColor;
  }

  /**
   * Selection index property getter method.
   */
  @SimpleProperty(
      description = "The index of the currently selected item, starting at " +
      "1.  If no item is selected, the value will be 0.  If an attempt is " +
      "made to set this to a number less than 1 or greater than the number " +
      "of items in the ListPicker, SelectionIndex will be set to 0, and " +
      "Selection will be set to the empty text.",
      category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex() {
    return selectionIndex;
  }

  /**
   * Selection index property setter method.
   */
  // Not a designer property, since this could lead to unpredictable
  // results if Selection is set to an incompatible value.
  @SimpleProperty
  public void SelectionIndex(int index) {
    selectionIndex = ElementsUtil.selectionIndex(index, items);
    // Now, we need to change Selection to correspond to SelectionIndex.
    selection = ElementsUtil.setSelectionFromIndex(index, items);
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
   * Elements property setter method
   * @param itemList - a YailList containing the strings to be added to the
   *                   ListPicker
   */
  // TODO(user): we need a designer property for lists
  @SimpleProperty
  public void Elements(YailList itemList) {
    items = ElementsUtil.elements(itemList, "ListPicker");
  }

  /**
   * ElementsFromString property setter method
   *
   * @param itemstring - a string containing a comma-separated list of the
   *                     strings to be picked from
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
                    defaultValue = "")
  // TODO(sharon): it might be nice to have a list editorType where the developer
  // could directly enter a list of strings (e.g. one per row) and we could
  // avoid the comma-separated business.
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring) {
    items = ElementsUtil.elementsFromString(itemstring);
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
//      csvItems = csvItems + "," + strArrayListItems[i] + "|" + strArrayListImages[i];
    }
    if (!csvItems.equals("")) {
      csvItems = csvItems.substring(1); //get rid of 1st comma
    }
//    Log.d(LOG_TAG, "new list is:{"+csvItems+"}");
    items = ElementsUtil.elementsFromString(csvItems);
  }

  /**
   * Title property getter method.
   *
   * @return  list picker title
   */
    @SimpleProperty(category = PropertyCategory.APPEARANCE,
                    description = "Optional title displayed at the top of the list of choices.")
  public String Title() {
    return title;
  }

  /**
   * Title property setter method: sets a new caption for the list picker in the
   * list picker activity's title bar.
   *
   * @param title  new list picker caption
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Title(String title) {
    this.title = title;
  }

  @Override
  public Intent getIntent() {
    Intent intent = new Intent();
    intent.setClassName(container.$context(), LIST_ACTIVITY_CLASS);
    intent.putExtra(LIST_ACTIVITY_ARG_NAME, items.toStringArray());
    intent.putExtra(LIST_ACTIVITY_IS_REPL, isRepl+"");
    intent.putExtra(LIST_ACTIVITY_SHOW_SEARCH_BAR, String.valueOf(showFilter)); //convert to string
    if (!title.equals("")) {
      intent.putExtra(LIST_ACTIVITY_TITLE, title);
    }
    // Get the current Form's opening transition anim type,
    // and pass it to the list picker activity. For consistency,
    // the closing animation will be the same (but in reverse)
    String openAnim = container.$form().getOpenAnimType();
    intent.putExtra(LIST_ACTIVITY_ANIM_TYPE, openAnim);
    intent.putExtra(LIST_ACTIVITY_ANIMATION, animationStyle);
//    intent.putExtra(LIST_ACTIVITY_STYLE, listActivityStyle);
    intent.putExtra(LIST_ACTIVITY_ORIENTATION_TYPE,container.$form().ScreenOrientation());
    intent.putExtra(LIST_ACTIVITY_ITEM_TEXT_COLOR, itemTextColor);
    intent.putExtra(LIST_ACTIVITY_BACKGROUND_COLOR, itemBackgroundColor);

    return intent;
  }

  // ActivityResultListener implementation

  /**
   * Callback method to get the result returned by the list picker activity
   *
   * @param requestCode a code identifying the request.
   * @param resultCode a code specifying success or failure of the activity
   * @param data the returned data, in this case an Intent whose data field
   *        contains the selected item.
   */
  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
      if (requestCode == this.requestCode  && (resultCode == Activity.RESULT_OK  || resultCode == Activity.RESULT_CANCELED)) {

          //If user has removed items,
          if (data.hasExtra(LIST_ACTIVITY_ITEMS)) {
              ArrayList<String> stringArrayListExtra = data.getStringArrayListExtra(LIST_ACTIVITY_ITEMS);
//              Log.d(LOG_TAG, "returned items are:" + stringArrayListExtra);
            this.deletedItems = YailList.makeList(data.getStringArrayListExtra(LIST_ACTIVITY_ITEMS_DELETED));
            items = YailList.makeList(stringArrayListExtra);
//            items = loadDeletedItems(items, data.getStringArrayListExtra(LIST_ACTIVITY_ITEMS_DELETED));
//              items = YailList.makeList(stringArrayListExtra);
          }
          if (resultCode == Activity.RESULT_OK) {
          if (data.hasExtra(LIST_ACTIVITY_RESULT_NAME)) {
            selection = data.getStringExtra(LIST_ACTIVITY_RESULT_NAME);
          } else {
            selection = "";
          }
          selectionIndex = data.getIntExtra(LIST_ACTIVITY_RESULT_INDEX, 0);
          }

          //regardless, invoke AfterPicking because even if user back-arrows, we invoke event
          //so that we can get Elements in the block
          AfterPicking();
          // It is necessary for the code of onResume to run there instead of here
          // because the activity has not yet been initialized at this point. At this
          // point, calls to the keyboard fail.
          resumedFromListFlag = true;
        }



//    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
//      if (data.hasExtra(LIST_ACTIVITY_RESULT_NAME)) {
//        selection = data.getStringExtra(LIST_ACTIVITY_RESULT_NAME);
//      } else {
//        selection = "";
//      }
//      selectionIndex = data.getIntExtra(LIST_ACTIVITY_RESULT_INDEX, 0);
//      AfterPicking();
//    }
  }

    @SimpleProperty(description = "Returns list of deleted items from last swipe", category = PropertyCategory.BEHAVIOR)
    public YailList DeletedItems() {
        return this.deletedItems;
    }

    private YailList loadDeletedItems(YailList origList, ArrayList<String> deletedItems) {
//      Log.d(LOG_TAG, "Deleted items are:"+ deletedItems);
//      Log.d(LOG_TAG, "Orig items are:"+ origList);
        ArrayList<String> newList = new ArrayList<String>();
        for (String anItem : origList.toStringArray()) {
            //If the item in orig list is not found in updatedList, then add it to deleted list
            if (!deletedItems.contains(anItem)) {
                newList.add(anItem);
            }
        }
        return YailList.makeList(newList);
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    container.$form().unregisterForActivityResult(this);
  }

}
