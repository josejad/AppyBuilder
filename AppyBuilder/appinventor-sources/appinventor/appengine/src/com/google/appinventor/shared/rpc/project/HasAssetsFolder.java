// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

/**
 * Interface for projects that have an Assets folder.
 *
 * @param <T> the type of FolderNode
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 */
public interface HasAssetsFolder<T extends FolderNode> {
  T getAssetsFolder();
}
