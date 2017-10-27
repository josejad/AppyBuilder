package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for floating button positions
 *
 * @author kashi01@gmail.com (M. Hossein Amerkashi)
 */
public class YoungAndroidFloatingButtonPositionPropertyEditor extends ChoicePropertyEditor {

  public static final String RIGHT_BOTTOM = ComponentConstants.FB_RIGHT_BOTTOM + "";
  public static final String RIGHT_TOP = ComponentConstants.FB_RIGHT_TOP + "";
  public static final String LEFT_BOTTOM = ComponentConstants.FB_LEFT_BOTTOM + "";
  public static final String LEFT_TOP = ComponentConstants.FB_LEFT_TOP + "";
  public static final String TOP_CENTER = ComponentConstants.FB_TOP_CENTER + "";
  public static final String BOTTOM_CENTER = ComponentConstants.FB_BOTTOM_CENTER + "";

  private static final Choice[] fbPositions = new Choice[] {
      new Choice(MESSAGES.fbRightBottom(), RIGHT_BOTTOM),
      new Choice(MESSAGES.fbRightTop(), RIGHT_TOP),
      new Choice(MESSAGES.fbLeftBottom(), LEFT_BOTTOM),
      new Choice(MESSAGES.fbLeftTop(), LEFT_TOP),
      new Choice(MESSAGES.fbTopCenter(), TOP_CENTER),
      new Choice(MESSAGES.fbBottomCenter(), BOTTOM_CENTER),
  };


  public YoungAndroidFloatingButtonPositionPropertyEditor() {
    super(fbPositions);
  }
}

