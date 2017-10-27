// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import static com.google.appinventor.client.Ode.MESSAGES;

public class YoungAndroidScreenAnimationChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] screenAnimationChoices = new Choice[] {
    new Choice(MESSAGES.defaultScreenAnimation(), "default"),
    new Choice(MESSAGES.fadeScreenAnimation(), "fade"),
    new Choice(MESSAGES.zoomScreenAnimation(), "zoom"),
    new Choice(MESSAGES.slideHorizontalScreenAnimation(), "slidehorizontal"),
    new Choice(MESSAGES.slideVerticalScreenAnimation(), "slidevertical"),
    new Choice(MESSAGES.noneScreenAnimation(), "none"),
  };

  public YoungAndroidScreenAnimationChoicePropertyEditor() {
    super(screenAnimationChoices);
  }

}
