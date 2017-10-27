// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.settings.user;

import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.shared.settings.SettingsConstants;

/**
 * General Young Android settings.
 *
 */
public final class YoungAndroidSettings extends Settings {

  /**
   * Creates a new instance of user-specific YoungAndroid settings.
   *
   * @param user  user associated with settings
   */
  public YoungAndroidSettings(UserInfoProvider user) {
    super(SettingsConstants.USER_YOUNG_ANDROID_SETTINGS);
  }
}
