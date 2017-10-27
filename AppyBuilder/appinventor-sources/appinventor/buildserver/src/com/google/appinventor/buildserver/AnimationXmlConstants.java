// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.buildserver;

public class AnimationXmlConstants {

  private AnimationXmlConstants() {
  }

  public final static String FADE_IN_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<alpha xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:fromAlpha=\"0.0\" android:toAlpha=\"1.0\"\n" +
      "\tandroid:duration=\"@android:integer/config_longAnimTime\" />";

  public final static String FADE_OUT_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<alpha xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:fromAlpha=\"1.0\" android:toAlpha=\"0.0\"\n" +
      "\tandroid:duration=\"@android:integer/config_longAnimTime\" />";

  public final static String HOLD_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/accelerate_interpolator\"\n" +
      "\tandroid:fromXDelta=\"0\" android:toXDelta=\"0\"\n" +
      "\tandroid:duration=\"@android:integer/config_longAnimTime\" />";

  public final static String SLIDE_EXIT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/overshoot_interpolator\"\n" +
      "\tandroid:fromXDelta=\"0%\" android:toXDelta=\"-100%\"\n" +
      "\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_ENTER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/overshoot_interpolator\"\n" +
      "\t\tandroid:fromXDelta=\"100%\" android:toXDelta=\"0%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_EXIT_REVERSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/overshoot_interpolator\"\n" +
      "\t\tandroid:fromXDelta=\"0%\" android:toXDelta=\"100%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_ENTER_REVERSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/overshoot_interpolator\"\n" +
      "\t\tandroid:fromXDelta=\"-100%\" android:toXDelta=\"0%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_V_EXIT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\tandroid:fromYDelta=\"0%\" android:toYDelta=\"100%\"\n" +
      "\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_V_ENTER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:fromYDelta=\"-100%\" android:toYDelta=\"0%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_V_EXIT_REVERSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:fromYDelta=\"0%\" android:toYDelta=\"-100%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  public final static String SLIDE_V_ENTER_REVERSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<translate xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:fromYDelta=\"100%\" android:toYDelta=\"0%\"\n" +
      "\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />";

  //================= Start: animation for material design slider ltr
  // material_slide_ltr_enter, material_slide_ltr_exit. These names are assigned in compiler.java
  public final static String MATERIAL_DESIGN_CUSTOM_THEME = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
          "<resources>\n" +
          "  <style name=\"Theme.myCustomTheme\" parent=\"@android:style/Theme.Panel\">\n" +
          "    <item name=\"android:windowAnimationStyle\">@style/myCustomAnimation.Activity</item>\n" +
          "\t<item name=\"android:windowIsFloating\">false</item>\n" +
          "\t<item name=\"android:backgroundDimEnabled\">true</item>\n" +
          "  </style>\n" +
          "  \n" +
          "  <style name=\"myCustomAnimation.Activity\" parent=\"@android:style/Animation.Activity\">\n" +
          "\t<item name=\"android:windowEnterAnimation\">@anim/material_slide_ltr_enter</item>\n" +
          "\t<item name=\"android:windowExitAnimation\">@anim/material_slide_ltr_exit</item>\n" +
          "  </style>\n" +
          "</resources>";
  public final static String MATERIAL_DESIGN_SLIDE_LTR_ENTER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
          "<translate android:interpolator=\"@android:anim/decelerate_interpolator\" android:duration=\"170\" android:fromXDelta=\"-100.0%\" android:toXDelta=\"0.0%\"\n" +
          "  xmlns:android=\"http://schemas.android.com/apk/res/android\" />";
  public final static String MATERIAL_DESIGN_SLIDE_LTR_EXIT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
          "<translate android:interpolator=\"@android:anim/decelerate_interpolator\" android:duration=\"260\" android:fromXDelta=\"0.0%\" android:toXDelta=\"-100.0%\"\n" +
          "  xmlns:android=\"http://schemas.android.com/apk/res/android\" />";
  //================= End: animation for material design slider ltr

  //================= Start: stretch and spin animation:
  // https://developer.android.com/guide/topics/graphics/view-animation.html
  // see if these are any good: http://www.journaldev.com/9481/android-animation-example
  // material design: https://material.google.com/motion/creative-customization.html#
  public final static String HYPERSPACE_JUMP = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
          "<set android:shareInterpolator=\"false\" xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
          "    <scale\n" +
          "            android:interpolator=\"@android:anim/accelerate_decelerate_interpolator\"\n" +
          "            android:fromXScale=\"1.0\"\n" +
          "            android:toXScale=\"1.4\"\n" +
          "            android:fromYScale=\"1.0\"\n" +
          "            android:toYScale=\"0.6\"\n" +
          "            android:pivotX=\"50%\"\n" +
          "            android:pivotY=\"50%\"\n" +
          "            android:fillAfter=\"false\"\n" +
          "            android:duration=\"700\" />\n" +
          "    <set android:interpolator=\"@android:anim/decelerate_interpolator\">\n" +
          "        <scale\n" +
          "                android:fromXScale=\"1.4\"\n" +
          "                android:toXScale=\"0.0\"\n" +
          "                android:fromYScale=\"0.6\"\n" +
          "                android:toYScale=\"0.0\"\n" +
          "                android:pivotX=\"50%\"\n" +
          "                android:pivotY=\"50%\"\n" +
          "                android:startOffset=\"700\"\n" +
          "                android:duration=\"400\"\n" +
          "                android:fillBefore=\"false\" />\n" +
          "        <rotate\n" +
          "                android:fromDegrees=\"0\"\n" +
          "                android:toDegrees=\"-45\"\n" +
          "                android:toYScale=\"0.0\"\n" +
          "                android:pivotX=\"50%\"\n" +
          "                android:pivotY=\"50%\"\n" +
          "                android:startOffset=\"700\"\n" +
          "                android:duration=\"400\" />\n" +
          "    </set>\n" +
          "</set>";
  //================= End: stretch and spin animation

  public final static String ZOOM_ENTER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<set xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\t\tandroid:interpolator=\"@android:anim/decelerate_interpolator\">\n" +
      "\t<scale android:fromXScale=\"2.0\" android:toXScale=\"1.0\"\n" +
      "\t\t\tandroid:fromYScale=\"2.0\" android:toYScale=\"1.0\"\n" +
      "\t\t\tandroid:pivotX=\"50%p\" android:pivotY=\"50%p\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />\n" +
      "</set>";

  public final static String ZOOM_ENTER_REVERSE ="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n"+
      "<set xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\t\tandroid:interpolator=\"@android:anim/decelerate_interpolator\">\n" +
      "\t<scale android:fromXScale=\"0.5\" android:toXScale=\"1.0\"\n" +
      "\t\t\tandroid:fromYScale=\"0.5\" android:toYScale=\"1.0\"\n" +
      "\t\t\tandroid:pivotX=\"50%p\" android:pivotY=\"50%p\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />\n" +
      "</set>";

  public final static String ZOOM_EXIT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<set xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\t\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:zAdjustment=\"top\">\n" +
      "\t<scale android:fromXScale=\"1.0\" android:toXScale=\".5\"\n" +
      "\t\t\tandroid:fromYScale=\"1.0\" android:toYScale=\".5\"\n" +
      "\t\t\tandroid:pivotX=\"50%p\" android:pivotY=\"50%p\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />\n" +
      "\t<alpha android:fromAlpha=\"1.0\" android:toAlpha=\"0\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\"/>\n" +
      "</set>";

  public final static String ZOOM_EXIT_REVERSE ="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n" +
      "<set xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "\t\tandroid:interpolator=\"@android:anim/decelerate_interpolator\"\n" +
      "\t\tandroid:zAdjustment=\"top\">\n" +
      "\t<scale android:fromXScale=\"1.0\" android:toXScale=\"2.0\"\n" +
      "\t\t\tandroid:fromYScale=\"1.0\" android:toYScale=\"2.0\"\n" +
      "\t\t\tandroid:pivotX=\"50%p\" android:pivotY=\"50%p\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\" />\n" +
      "\t<alpha android:fromAlpha=\"1.0\" android:toAlpha=\"0\"\n" +
      "\t\t\tandroid:duration=\"@android:integer/config_mediumAnimTime\"/>\n" +
      "</set>";

  public final static String BASIC_MAP_XML= "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
          "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
          "    android:id=\"@+id/map\"" +
          "    android:layout_width=\"fill_parent\"\n" +
          "    android:layout_height=\"fill_parent\" >\n" +
          "    \n" +
          "    <com.google.android.gms.maps.MapView android:id=\"@+id/mapview\"\n" +
          "        android:layout_width=\"fill_parent\" \n" +
          "        android:layout_height=\"fill_parent\" />\n" +
          " \n" +
          "</LinearLayout>" ;

  public final static String MY_WEBVIEW = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
          "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
          "    android:orientation=\"vertical\" android:layout_width=\"match_parent\"\n" +
          "    android:layout_height=\"match_parent\">\n" +
          "    <LinearLayout\n" +
          "        xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
          "        android:id=\"@+id/content_main\"\n" +
          "        android:layout_width=\"match_parent\"\n" +
          "        android:layout_height=\"match_parent\"\n" +
          "        android:orientation=\"vertical\">\n" +
          "        <ProgressBar\n" +
          "            style=\"?android:attr/progressBarStyleHorizontal\"\n" +
          "            android:id=\"@+id/progressBar\"\n" +
          "            android:layout_width=\"match_parent\"\n" +
          "            android:layout_height=\"wrap_content\"/>\n" +
          "        <WebView\n" +
          "            android:id=\"@+id/webView1\"\n" +
          "            android:layout_width=\"match_parent\"\n" +
          "            android:layout_height=\"match_parent\"/>\n" +
          "    </LinearLayout>\n" +
          "</LinearLayout>" ;

  public final static String LISTVIEW_IMAGE_TEXT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
          "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
          "    android:layout_width=\"fill_parent\"\n" +
          "    android:layout_height=\"fill_parent\" >\n" +
          "    <ImageView\n" +
          "        android:id=\"@+id/icon\"\n" +
          "        android:layout_width=\"wrap_content\"\n" +
          "        android:layout_height=\"wrap_content\"\n" +
          "        android:contentDescription=\"Thumbnail\"\n" +
          "        android:paddingLeft=\"10dp\"\n" +
          "        android:paddingRight=\"10dp\" />\n" +
          " \n" +
 /*         "    <TextView\n" +
          "        android:id=\"@+id/title\"\n" +
          "        android:layout_width=\"wrap_content\"\n" +
          "        android:layout_height=\"wrap_content\"\n" +
          "        android:layout_toRightOf=\"@+id/icon\"\n" +
          "        android:paddingBottom=\"10dp\"\n" +
          "        android:textColor=\"#CC0033\"\n" +
          "        android:textSize=\"16dp\" />\n" +*/
          " \n" +
          "    <TextView\n" +
          "        android:id=\"@+id/desc\"\n" +
          "        android:layout_width=\"wrap_content\"\n" +
          "        android:layout_height=\"wrap_content\"\n" +
          "        android:layout_below=\"@+id/title\"\n" +
          "        android:layout_toRightOf=\"@+id/icon\"\n" +
          "        android:paddingLeft=\"10dp\"\n" +
          "        android:textColor=\"#FFFFFF\"\n" +
          "        android:textSize=\"14dp\" />\n" +
          "</LinearLayout>";

          public final static String OKAY_GOOGLE_SEARCHABLE =
                  "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                          "<searchable xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                          "    android:label=\"$[REPLACE_WITH_APP_NAME]\" >\n" +
                          "</searchable>";

          public static String CUSTOM_SPINNER =
                  "<TextView xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                          "    android:id=\"@android:id/text1\"\n" +
                          "    android:layout_width=\"match_parent\"\n" +
                          "    android:layout_height=\"wrap_content\"\n" +
//                          "    android:dropDownVerticalOffset=\"35dp\"\n" +
//                          "    android:spinnerMode=\"dialog\"\n" +
//                          "    android:textSize=\"20sp\"\n" +
                          "    android:textColor=\"#000000\" />";

          public static String CUSTOM_SPINNER_DROP_DOWN =
                  "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                          "    android:layout_width=\"fill_parent\"\n" +
                          "    android:layout_height=\"fill_parent\"\n" +
                          "    android:gravity=\"center\"\n" +
                          "    android:weightSum=\"5\"\n" +
                          "    android:orientation=\"horizontal\" >\n" +
                          "\n" +
                          "    <RadioButton\n" +
                          "        android:id=\"@+id/sp_radioButton1\"\n" +
                          "        android:layout_width=\"0dp\"\n" +
                          "        android:layout_weight=\"1\"\n" +
                          "        android:layout_height=\"wrap_content\"\n" +
                          "        android:text=\"\" />\n" +
                          "\n" +
                          "    <TextView\n" +
                          "        android:id=\"@+id/sp_textView1\"\n" +
                          "        android:layout_width=\"0dp\"\n" +
                          "        android:layout_weight=\"4\"\n" +
                          "        android:layout_height=\"wrap_content\"\n" +
                          "        android:text=\"Medium Text\"\n" +
                          "        android:textAppearance=\"?android:attr/textAppearanceMedium\" />\n" +
                          "\n" +
                          "</LinearLayout>";
}
