// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.widget.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.IllegalArgumentError;
import com.google.appinventor.components.runtime.util.*;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import java.io.IOException;

/**
 * Component for displaying images and animations.
 *
 */
@DesignerComponent(version = YaVersion.IMAGE_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Component for displaying images.  The picture to display, " +
    "and other aspects of the Image's appearance, can be specified in the " +
    "Designer or in the Blocks Editor.")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class Image extends AndroidViewComponent implements View.OnClickListener, View.OnLongClickListener {

  private final ImageView view;

  private String picturePath = "";  // Picture property

  private double rotationAngle = 0.0;

  private int scalingMode = Component.SCALING_SCALE_PROPORTIONALLY;

  /**
   * Creates a new Image component.
   *
   * @param container  container, component will be placed in
   */
  public Image(ComponentContainer container) {
    super(container);

    view = new ImageView(container.$context()) {
      @Override
      public boolean verifyDrawable(Drawable dr) {
        super.verifyDrawable(dr);
        // TODO(user): multi-image animation
        return true;
      }
    };
    view.setFocusable(true);
    Enabled(true);
    view.setOnClickListener(this);
    view.setOnLongClickListener(this);

    // Adds the component to its designated container
    container.$add(this);
  }

  @Override
  public View getView() {
    return view;
  }

  /**
   * Returns the path of the image's picture.
   *
   * @return  the path of the image's picture
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Picture() {
    return picturePath;
  }

  /**
   * Specifies the path of the image's picture.
   *
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path of the image's picture
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty
  public void Picture(String path) {
    picturePath = (path == null) ? "" : path;

    Drawable drawable;
    try {
      drawable = MediaUtil.getBitmapDrawable(container.$form(), picturePath);
    } catch (IOException ioe) {
      Log.e("Image", "Unable to load " + picturePath);
      drawable = null;
    }

    ViewUtil.setImage(view, drawable);
  }

  // too simplified. It works, but won't include in this release
/*  @SimpleFunction(description = "Creates a rounded image and places it in horizontalArrangement. Change background color as needed")
  public void CreateRoundImage(int radius) {

    View rootView = container.$form().getWindow().getDecorView().getRootView();
    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, -1);
    int borderWidth = container.$form().convertDpToDensity(5);
    Drawable drawable;
    try {
      picturePath = Picture();
      drawable = MediaUtil.getBitmapDrawable(container.$form(), picturePath);
      Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
      Bitmap circularBitmap = ImageUtils.getRoundedCornerBitmap(bitmap, radius);
      view.setImageBitmap(circularBitmap);

    } catch (IOException ioe) {
      Log.e("Image", "Unable to load " + picturePath);
      drawable = null;
    }
  }*/

  /**
   * Specifies the angle at which the image picture appears rotated.
   *
   * @param rotated  the rotation angle
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0.0")
  @SimpleProperty
  public void RotationAngle(double rotationAngle) {
    if (this.rotationAngle == rotationAngle) {
      return;                   // Nothing to do...
                                // This also means that you can always set the
                                // the angle to 0.0 even on older Android devices
    }
    if (SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
      container.$form().dispatchErrorOccurredEvent(this, "RotationAngle",
        ErrorMessages.ERROR_IMAGE_CANNOT_ROTATE);
      return;
    }
    HoneycombUtil.viewSetRotate(view, rotationAngle);
    this.rotationAngle = rotationAngle;
  }

  @SimpleProperty(description = "The angle at which the image picture appears rotated. " +
      "This rotation does not appear on the designer screen, only on the device.",
      category = PropertyCategory.APPEARANCE)
  public double RotationAngle() {
    return rotationAngle;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "False")
  // @Deprecated -- We will deprecate this in a future release (jis: 2/12/2016)
  @SimpleProperty(description = "Specifies whether the image should be resized to match the size of the ImageView.")
  public void ScalePictureToFit(boolean scale) {
    if (scale)
      view.setScaleType(ImageView.ScaleType.FIT_XY);
    else
      view.setScaleType(ImageView.ScaleType.FIT_CENTER);
  }

  /**
   * Animation property setter method.
   *
   * @see AnimationUtil
   *
   * @param animation  animation kind
   */
  @SimpleProperty(description = "This is a limited form of animation that can attach " +
      "a small number of motion types to images.  The allowable motions are " +
      "ScrollRightSlow, ScrollRight, ScrollRightFast, ScrollLeftSlow, ScrollLeft, " +
      "ScrollLeftFast, Stop and HyperJump",
      category = PropertyCategory.APPEARANCE)
  // TODO(user): This should be changed from a property to an "animate" method, and have the choices
  // placed in a dropdown.  Aternatively the whole thing should be removed and we should do
  // something that is more consistent with sprites.
  public void Animation(String animation) {
    AnimationUtil.ApplyAnimation(view, animation, container.$context());
  }

  @Deprecated
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SCALING,
//      defaultValue = Component.SCALING_SCALE_PROPORTIONALLY + "")
  @SimpleProperty(description = "This property determines how the picture " +
      "scales according to the Height or Width of the Image. Scale " +
      "proportionally (0) preserves the picture aspect ratio. Scale to fit " +
      "(1) matches the Image area, even if the aspect ratio changes.")
  public void Scaling(int mode) {
    switch (mode) {
      case 0:
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        break;
      case 1:
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        break;
      default:
        throw new IllegalArgumentError("Illegal scaling mode: " + mode);
    }
    scalingMode = mode;
  }

  @SimpleProperty
  public int Scaling() {
    return scalingMode;
  }

  @Override
  public void onClick(View view) {
    Click();
  }

  @Override
  public boolean onLongClick(View view) {
    return LongClick();
  }

  /**
   * Indicates a user has clicked on the button.
   */
  @SimpleEvent(description = "User tapped and released the component.")
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }

  /**
   * Indicates a user has long clicked on the button.
   */
  @SimpleEvent(description = "User held the component down.")
  public boolean LongClick() {
    return EventDispatcher.dispatchEvent(this, "LongClick");
  }

  /**
   * Returns true if the component is clickable.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return view.isEnabled();
  }

  /**
   * Specifies whether the checkbox should be active and clickable.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty(description = "Can be used make component Clickable")
  public void Enabled(boolean enabled) {
    getView().setEnabled(enabled);
  }

}
