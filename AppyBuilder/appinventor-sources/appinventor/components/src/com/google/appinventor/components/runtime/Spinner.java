// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import android.widget.TextView;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;

@DesignerComponent(version = YaVersion.SPINNER_COMPONENT_VERSION,
        description = "<p>A spinner component that displays a pop-up with a list of elements." +
                " These elements can be set in the Designer or Blocks Editor by setting the" +
                "<code>ElementsFromString</code> property to a string-separated concatenation" +
                " (for example, <em>choice 1, choice 2, choice 3</em>) or by setting the " +
                "<code>Elements</code> property to a List in the Blocks editor. " +
                "Spinners are created with the first item already selected. So selecting " +
                " it does not generate an After Picking event. Consequently it's useful to make the " +
                " first Spinner item be a non-choice like \"Select from below...\". </p>",
        category = ComponentCategory.USERINTERFACE,
        nonVisible = false,
        iconName = "images/spinner.png")
@SimpleObject
public final class Spinner extends AndroidViewComponent implements OnItemSelectedListener {

  private android.widget.Spinner view;
  private final Resources resources;
  private final int layout;
  private ArrayAdapter<String> adapter;
  private YailList items = new YailList();
  private int oldAdapterCount;
  private int oldSelectionIndex;
  private int backgroundColor;
  private boolean isWithRadioButtons=true;

  public Spinner(ComponentContainer container) {
    super(container);
    view = new android.widget.Spinner(container.$context());

//    view = new android.widget.Spinner(container.$context(),null,android.R.style.Widget_Spinner, android.widget.Spinner.MODE_DROPDOWN);
//    view.setBackgroundColor(COLOR_LTGRAY);

//    http://stackoverflow.com/questions/17540123/modify-spinners-dropdown-box-programmatically

    // set regular and dropdown layouts
    //changing from container.$form() to container.$context()  -- http://stackoverflow.com/questions/16354168/text-on-spinner-is-white-on-a-white-background
//    adapter = new ArrayAdapter<String>(container.$context().getBaseContext(), android.R.layout.simple_spinner_item);

    // http://www.digitalinternals.com/android/spinner-text-white-on-white-background/389/
    // http://www.broculos.net/2013/09/how-to-change-spinner-text-size-color.html#.WbKV7siGNPY
    resources = container.$form().getResources();
    layout = resources.getIdentifier("simple_spinner_item_2", "layout", container.$form().getPackageName());

//    android.util.TypedValue value = new android.util.TypedValue();
//    container.$context().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
//    android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
//    container.$context().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//    float ret = value.getDimension(metrics);
//    view.setMinimumHeight((int) (ret - 1 * metrics.density));


//    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_spinner_item);
//    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_spinner_dropdown_item);
    adapter = new ArrayAdapter<String>(container.$form(), layout);


//    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
//    adapter = new WhiteTextArrayAdapter<>(container.$form(), android.R.layout.simple_spinner_item);

    view.setAdapter(adapter);

    // make the spinner show radio buttons
//    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
    view.setOnItemSelectedListener(this);

    container.$add(this);

    Prompt("");
    BackgroundColor(Component.COLOR_DKGRAY);
    ElementsFromString("Option1,Option2,Option3");
    ShowRadioButtons(isWithRadioButtons);
    oldSelectionIndex = SelectionIndex();

  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty(description = "If enabled, then radio buttons will be shown")
  public void ShowRadioButtons(boolean enabled) {
    isWithRadioButtons = enabled;

    if (enabled) adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
//    if (enabled) adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
//    if (enabled) adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // not good. Won't show rb
    else adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  }

  @SimpleProperty(description = "If enabled, then radio buttons will be shown")
  public boolean ShowRadioButtons() {
    return isWithRadioButtons;
  }


  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Gets background color of this component")
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the component background color as integer.
   *
   * @param argb  background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
  @SimpleProperty(description = "Sets background color of this component")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
//    https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html

    // set the background clor of the arrow-drop-down
    view.getBackground().setColorFilter(argb, PorterDuff.Mode.SRC_ATOP);

    // set the background color of the actual drop-down to match the arrow-color
    view.getPopupBackground().setColorFilter(argb, PorterDuff.Mode.SRC_ATOP);
//    view.getBackground().setColorFilter(argb, PorterDuff.Mode.LIGHTEN);
  }

  @Override
  public View getView(){
    return view;
  }


  /**
   * Selection property getter method.
   */
  @SimpleProperty(description = "Returns the current selected item in the spinner ",
          category = PropertyCategory.BEHAVIOR)
  public String Selection(){
    return SelectionIndex() == 0 ? "" : (String) view.getItemAtPosition(SelectionIndex() - 1);
  }

  /**
   * Selection property setter method.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "Set the selected item in the spinner",
          category = PropertyCategory.BEHAVIOR)
  public void Selection(String value){
    SelectionIndex(ElementsUtil.setSelectedIndexFromValue(value, items));
    /*TextView textView = (TextView)this.view.getChildAt(0);
    if (textView != null) {
      textView.setTextColor(-16777216);
    }*/
  }

  /**
   * Selection index property getter method.
   */
  @SimpleProperty(description = "The index of the currently selected item, starting at 1. If no " +
          "item is selected, the value will be 0.", category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex(){
    return ElementsUtil.selectionIndex(view.getSelectedItemPosition() + 1, items);
  }

  /**
   * Selection index property setter method, not a designer property to prevent
   * inconsistencies if selection is invalid
   */
  @SimpleProperty(description = "Set the spinner selection to the element at the given index." +
          "If an attempt is made to set this to a number less than 1 or greater than the number of " +
          "items in the Spinner, SelectionIndex will be set to 0, and Selection will be set to empty.",
          category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int index){
    oldSelectionIndex = SelectionIndex();
    view.setSelection(ElementsUtil.selectionIndex(index, items) - 1); // AI lists are 1-based
  }

  /**
   * Elements property getter method
   */
  @SimpleProperty(description = "returns a list of text elements to be picked from.",
          category = PropertyCategory.BEHAVIOR)
  public YailList Elements(){
    return items;
  }

  /**
   * Elements property setter method
   */
  @SimpleProperty(description = "adds the passed text element to the Spinner list",
          category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList itemList){
    // The following conditional handles special cases for the fact that
    // spinners automatically select an item when non-empty data is fed
    if (itemList.size() == 0) {
      SelectionIndex(0);
    } else if (itemList.size() < items.size() && SelectionIndex() == items.size()) {
      SelectionIndex(itemList.size());
    }
    items = ElementsUtil.elements(itemList, "Spinner");
    setAdapterData(itemList.toStringArray());
  }

  /**
   * ElementsFromString property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Option1,Option2,Option3")
  @SimpleProperty(description = "sets the Spinner list to the elements passed in the " +
          "comma-separated string", category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring){
    Elements(ElementsUtil.elementsFromString(itemstring));
  }

  private void setAdapterData(String[] theItems) {
    oldAdapterCount = adapter.getCount();
    adapter.clear();
    for (int i = 0; i < theItems.length; i++){
      adapter.add(theItems[i]);
    }
  }

  /**
   * Prompt property getter method
   */
  @SimpleProperty(description = "Text with the current title for the Spinner window",
          category = PropertyCategory.APPEARANCE)
  public String Prompt(){
    return view.getPrompt().toString();
  }

  /**
   * Prompt property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "sets the Spinner window prompt to the given tittle",
          category = PropertyCategory.APPEARANCE)
  public void Prompt(String str){
    view.setPrompt(str);
  }

  /**
   * To display the dropdown list without the user having to click it
   */
  @SimpleFunction(description = "displays the dropdown list for selection, " +
          "same action as when the user clicks on the spinner.")
  public void DisplayDropdown(){
    view.performClick();
  }

  /**
   * Indicates a user has selected an item
   */
  @SimpleEvent(description = "Event called after the user selects an item from the dropdown list.")
  public void AfterSelecting(String selection){
    EventDispatcher.dispatchEvent(this, "AfterSelecting", selection);
  }

  public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
    // special case 1:
    // onItemSelected is fired when the adapter goes from empty to non-empty AND
    // SelectionIndex was not set, i.e. oldSelectionIndex == 0
    // special case 2:
    // onItemSelected is fired when the adapter goes from larger size to smaller size AND
    // the old selection position (one-based) is larger than the size of the new adapter
    if (oldAdapterCount == 0 && adapter.getCount() > 0 && oldSelectionIndex == 0 ||
            oldAdapterCount > adapter.getCount() && oldSelectionIndex > adapter.getCount()) {
      SelectionIndex(position + 1);  // AI lists are 1-based
      oldAdapterCount = adapter.getCount();
    } else {
      SelectionIndex(position + 1); // AI lists are 1-based
      AfterSelecting(Selection());
    }
  }

  public void onNothingSelected(AdapterView<?> parent){
    view.setSelection(0);
  }

  public static class WhiteTextArrayAdapter<T> extends ArrayAdapter<T> {

    public WhiteTextArrayAdapter(Context context, int textViewResourceId, T[] objects) {
      super(context, textViewResourceId, objects);
    }

    public WhiteTextArrayAdapter(Context context, int textViewResourceId) {
      super(context, textViewResourceId);
    }

    public static WhiteTextArrayAdapter<CharSequence> createFromResource(Context context, int textArrayResId, int textViewResId) {
      CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
      return new WhiteTextArrayAdapter<CharSequence>(context, textViewResId, strings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return SetColourWhite(super.getView(position, convertView, parent));
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
      return SetColourWhite(super.getView(position, convertView, parent));
    }

    private View SetColourWhite(View v) {
      if (v instanceof TextView) ((TextView)v).setTextColor(Color.rgb(0xff, 0xff, 0xff)); // white
      return v;
    }

  }
}
