package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for elements sort options in list picker
 *
 * @author kashi01@gmail.com (M. Hossein Amerkashi)
 */
public class YoungAndroidSortOrderPropertyEditor extends ChoicePropertyEditor {

  // No sort order
  public static final String NONE = ComponentConstants.SORT_ORDER_NONE + "";
  // Elements should be sorted in ascending order
  public static final String ASCENDING = ComponentConstants.SORT_ORDER_ASCENDING + "";
  // Elements should be sorted in descending order
  public static final String DESCENDING = ComponentConstants.SORT_ORDER_DESCENDING + "";

  private static final Choice[] sortOptions = new Choice[] {
      new Choice(MESSAGES.sortOrderNone(), NONE),
      new Choice(MESSAGES.sortOrderAscending(), ASCENDING),
      new Choice(MESSAGES.sortOrderDescending(), DESCENDING)
  };


  public YoungAndroidSortOrderPropertyEditor() {
    super(sortOptions);
  }
}

