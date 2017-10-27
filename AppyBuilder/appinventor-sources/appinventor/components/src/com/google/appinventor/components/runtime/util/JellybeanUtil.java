// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

/**
 * Helper methods for calling methods added in Jellybean
 *
 */
public class JellybeanUtil {

  private JellybeanUtil() {
  }

  public static void getRealSize(Display display, Point outSize) {
    display.getRealSize(outSize);
  }

}
