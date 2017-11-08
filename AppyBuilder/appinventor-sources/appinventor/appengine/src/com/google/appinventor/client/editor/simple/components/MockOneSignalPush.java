// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.utils.MessageDialog;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.components.FirebaseAuthService;
import com.google.appinventor.shared.rpc.components.FirebaseAuthServiceAsync;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import static com.google.appinventor.client.Ode.MESSAGES;


/**
 * Mock for the non-visible FirebaseDB component. This needs a separate mock
 * from other non-visible components so that some of its properties can be
 * given dynamic default values.
 *
 * @author will2596@gmail.com (William Byrne)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class MockOneSignalPush extends MockNonVisibleComponent {

  public static final String TYPE = "OneSignalPush";

  private static boolean warningGiven = false; // Whether or not we have given experimental warning

  private boolean persistToken = false;

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   */
  public MockOneSignalPush(SimpleEditor editor) {
    super(editor, TYPE, new Image(images.onesignal()));
    OdeLog.log("MockOneSignal ");
  }


  @Override
    public void onPropertyChange(String propertyName, String newValue){
        super.onPropertyChange(propertyName, newValue);
    if (propertyName.equals("AppId")) {
      setAppId(newValue);
    }
//        OdeLog.log("MockFloatingButton property changed. property:" +
//                propertyName+ ", value: " + newValue);

        // we need to refresh by removing this widget and then refreshing again
//        removeFromParent();
//        ((HasWidgets)getWidget().getParent()).remove(getWidget()); // doesn't work throws class cast exception
//        getWidget().removeFromParent();  // doesn't work and throws exception

//        initComponent(new Image(images.floatingButton()));
        // Force to refresh/repaint the form
        refreshForm();

  }

  public void setAppId(String text) {
    editor.getProjectEditor().changeProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_ONE_SIGNAL_APP_ID, text);
  }
}
