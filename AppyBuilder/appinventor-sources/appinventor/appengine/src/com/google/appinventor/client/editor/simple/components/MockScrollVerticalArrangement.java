// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Mock VerticalArrangement component.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public final class MockScrollVerticalArrangement extends MockHVArrangement {

  /**
   * Component type name.
   */
  public static final String TYPE = "VerticalScrollArrangement";

  /**
   * Creates a new MockVerticalArrangement component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockScrollVerticalArrangement(SimpleEditor editor) {
    super(editor, TYPE, images.verticalScroll(),
      ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
      ComponentConstants.SCROLLABLE_ARRANGEMENT);
  }

}
