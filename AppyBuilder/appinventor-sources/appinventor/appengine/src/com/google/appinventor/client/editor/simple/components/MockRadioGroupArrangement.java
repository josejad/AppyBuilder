// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Mock HorizontalArrangement component.
 *
 */
public final class MockRadioGroupArrangement extends MockHVArrangement {

  /**
   * Component type name.
   */
  public static final String TYPE = "RadioGroupArrangement";


  public MockRadioGroupArrangement(SimpleEditor editor) {
    super(editor, TYPE, images.horizontal(),
            ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL,
            ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
  }

}
