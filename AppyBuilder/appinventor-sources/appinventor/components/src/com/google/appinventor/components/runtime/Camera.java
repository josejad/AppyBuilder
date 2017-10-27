// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.View;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.google.appinventor.components.runtime.util.FileUtil;

import java.io.*;
import java.io.File;
import java.util.Date;

/**
 * Camera provides access to the phone's camera
 *
 *
 */
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
   description = "A component to take a picture using the device's camera. " +
        "After the picture is taken, the name of the file on the phone " +
        "containing the picture is available as an argument to the " +
        "AfterPicture event. The file name can be used, for example, to set " +
        "the Picture property of an Image component." +
           "<p>You can also use Camera to perform a ScreenShot of the screen.",
   category = ComponentCategory.MEDIA,
   nonVisible = true,
   iconName = "images/camera.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.CAMERA,android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class Camera extends AndroidNonvisibleComponent
    implements ActivityResultListener, Component, OnDestroyListener, OnStopListener, OnPauseListener {

  private static final String CAMERA_INTENT = "android.media.action.IMAGE_CAPTURE";
  private static final String CAMERA_OUTPUT = "output";
  private final ComponentContainer container;
  private Uri imageFile;

  /* Used to identify the call to startActivityForResult. Will be passed back
  into the resultReturned() callback method. */
  private int requestCode;
  private android.hardware.Camera camera;
    private boolean enabled = false;
    android.hardware.Camera.Parameters cameraParams;
    private boolean isFlashOn;
    private String hasFlash=null;
    private String TAG="CameraComponent";

  // whether to open into the front-facing camera
  private boolean useFront;

  /**
   * Creates a Camera component.
   *
   * Camera has a boolean option to request the forward-facing camera via an intent extra.
   *
   * @param container container, component will be placed in
   */
  public Camera(ComponentContainer container) {
    super(container.$form());
    this.container = container;

    // Default property values
    UseFront(false);
  }

  /**
   * Returns true if the front-facing camera is to be used (when available)
   *
   * @return {@code true} indicates front-facing is to be used, {@code false} will open default
   */
  @Deprecated
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean UseFront() {
    return useFront;
  }

  /**
   * Specifies whether the front-facing camera should be used (when available)
   *
   * @param front
   *          {@code true} for front-facing camera, {@code false} for default
   */
  @Deprecated
  // Hide the deprecated property from the Designer
  //  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(description = "Specifies whether the front-facing camera should be used (when available). "
    + "If the device does not have a front-facing camera, this option will be ignored "
    + "and the camera will open normally.")
  public void UseFront(boolean front) {
    useFront = front;
  }

  /**
   * Takes a picture, then raises the AfterPicture event.
   * If useFront is true, adds an extra to the intent that requests the front-facing camera.
   */
  @SimpleFunction
  public void TakePicture() {
    Date date = new Date();
    String state = Environment.getExternalStorageState();

    if (Environment.MEDIA_MOUNTED.equals(state)) {

      imageFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
        "/Pictures/app_inventor_" + date.getTime()
        + ".jpg"));

      ContentValues values = new ContentValues();
      values.put(MediaStore.Images.Media.DATA, imageFile.getPath());
      values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
      values.put(MediaStore.Images.Media.TITLE, imageFile.getLastPathSegment());

      if (requestCode == 0) {
        requestCode = form.registerForActivityResult(this);
      }

      Uri imageUri = container.$context().getContentResolver().insert(
        MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
      Intent intent = new Intent(CAMERA_INTENT);
      intent.putExtra(CAMERA_OUTPUT, imageUri);

      // NOTE: This uses an undocumented, testing feature (CAMERA_FACING).
      // It may not work in the future.
      if (useFront) {
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
      }

      container.$context().startActivityForResult(intent, requestCode);
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      form.dispatchErrorOccurredEvent(this, "TakePicture",
          ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_READONLY);
    } else {
      form.dispatchErrorOccurredEvent(this, "TakePicture",
          ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_NOT_AVAILABLE);
    }
  }

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
//    Log.i("CameraComponent", "Returning result. Request code = " + requestCode + ", result code = " + resultCode);
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      File image = new File(imageFile.getPath());
      if (image.length() != 0) {
        scanFileToAdd(image);
        AfterPicture(imageFile.toString());
      } else {
        deleteFile(imageFile);  // delete empty file
        // see if something useful got returned in the data
        if (data != null && data.getData() != null) {
          Uri tryImageUri = data.getData();
//          Log.i("CameraComponent", "Calling Camera.AfterPicture with image path " + tryImageUri.toString());
          AfterPicture(tryImageUri.toString());
        } else {
//          Log.i("CameraComponent", "Couldn't find an image file from the Camera result");
          form.dispatchErrorOccurredEvent(this, "TakePicture",
              ErrorMessages.ERROR_CAMERA_NO_IMAGE_RETURNED);
        }
      }
    } else {
      // delete empty file
      deleteFile(imageFile);
    }
  }

  /**
   * Scan the newly added picture to be displayed in a default media content provider
   * in a device (e.g. Gallery, Google Photo, etc..)
   *
   * @param image the picture taken by Camera component
   */
  private void scanFileToAdd(File image) {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    Uri contentUri = Uri.fromFile(image);
    mediaScanIntent.setData(contentUri);
    container.$context().getApplicationContext().sendBroadcast(mediaScanIntent);
  }

  private void deleteFile(Uri fileUri) {
    File fileToDelete = new File(fileUri.getPath());
    try {
      if (fileToDelete.delete()) {
//        Log.i("CameraComponent", "Deleted file " + fileUri.toString());
      } else {
//        Log.i("CameraComponent", "Could not delete file " + fileUri.toString());
      }
    } catch (SecurityException e) {
      Log.d("CameraComponent", "Got security exception trying to delete file " + fileUri.toString());
    }
  }

  /**
   * Indicates that a photo was taken with the camera and provides the path to
   * the stored picture.
   */
  @SimpleEvent
  public void AfterPicture(String image) {
    EventDispatcher.dispatchEvent(this, "AfterPicture", image);
  }

    @SimpleFunction(description = "If enabled, turns flash on. If false and if flash is on, then it will turn it off")
    public void FlashtOn(boolean enabled) {
        //If no flashlight, then don't do anything.
        if (!HasFlash()) {
            return;
        }

        if (isFlashOn == enabled) {
            return;
        }

        getCamera();

        if (camera == null || cameraParams == null) {
            return;
        }

        if (enabled) {
            turnOnFlash();
        } else {
            turnOffFlash();
        }

    }

    @SimpleFunction(description = "Checks to see if device has flash or not")
    public boolean HasFlash() {
        //We only want to do this one time. Using below logic, we ensure that PackageManager is called only once
        if (hasFlash == null) {
            hasFlash = String.valueOf(form.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH));
        }

        boolean temp = Boolean.valueOf(hasFlash);

        return temp;
    }

    // getting camera parameters
    private void getCamera() {
        if (camera == null) {
            try {
                camera = android.hardware.Camera.open();
                cameraParams = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
            }
        }
    }

    @Override
    public void onStop() {

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPause() {
        // turn off the flash
        turnOffFlash();
    }

    @Override
    public void onDestroy() {
        // turn off the flash
        turnOffFlash();
    }

    // Turning On flash
    private void turnOnFlash() {
        try {
            if (HasFlash() && !isFlashOn) {
                camera = android.hardware.Camera.open();
                android.hardware.Camera.Parameters cameraParams = camera.getParameters();
                cameraParams.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(cameraParams);

                SurfaceTexture mPreviewTexture = new SurfaceTexture(0);
                camera.setPreviewTexture(mPreviewTexture);

                camera.startPreview();
                this.isFlashOn = true;
            }
        } catch (Exception e) {
           // no-op
        }
//        if (!isFlashOn) {
//            if (camera == null || cameraParams == null) {
//                return;
//            }
//
//            cameraParams = camera.getParameters();
//            cameraParams.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
//            camera.setParameters(cameraParams);
//            camera.startPreview();
//            isFlashOn = true;
//        }

    }

    // Turning Off flash
    private void turnOffFlash() {
        try {
            if (isFlashOn) {
                camera.stopPreview();
                camera.release();
                camera = null;
                isFlashOn = false;
            }
        } catch (Exception e) {
            // no-op;
        }
//        if (isFlashOn) {
//            if (camera == null || cameraParams == null) {
//                return;
//            }
//
//            cameraParams = camera.getParameters();
//            cameraParams.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
//            camera.setParameters(cameraParams);
//            camera.stopPreview();
//            isFlashOn = false;
//
//        }
    }

    //http://www.truiton.com/2013/03/android-take-screenshot-programmatically-and-send-email/
    @SimpleFunction(description = "Allows you to screenshot the device screen and save into a file. " +
            "Currently, only .jpg extension is supported. Please make sure that your imageName ends " +
            "with .jpg")
    public void TakeScreenshot(String imageName) {
        imageName=imageName.trim();

        //If image name doesn't end with .jpg, then error and return
        if (!(imageName.toLowerCase().endsWith(".jpg") || imageName.toLowerCase().endsWith(".png") )) {
            form.dispatchErrorOccurredEvent(this, "TakeScreenshot",
                    ErrorMessages.ERROR_MEDIA_IMAGE_FILE_FORMAT);
            return;
        }
//        String saveasFileName = imageName.equals("")?DEFAULT_CAPTURE_FILE_NAME:imageName;

//        Log.d(TAG, "Trying to screenshot with image name of: " + imageName);
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                form.dispatchErrorOccurredEvent(this, "TakeScreenshot",
                        ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_READONLY);
            } else {
                form.dispatchErrorOccurredEvent(this, "TakeScreenshot",
                        ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_NOT_AVAILABLE);
            }
//            Log.d(TAG, "Media error for for " + imageName);

            return ;
        }

        Bitmap bitmap;
        Bitmap.CompressFormat format;
        View v1 = container.$context().getWindow().getDecorView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        if (imageName.toLowerCase().endsWith(".jpg")) {
            format = Bitmap.CompressFormat.JPEG;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
        } else {
            format = Bitmap.CompressFormat.PNG;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        }

        try {

            java.io.File file = FileUtil.getExternalFile(imageName);
            String savedImageName = saveFile(file, format, "SaveAs");

            if (savedImageName.equals("")) {
                //something went wrong in saving the file
                form.dispatchErrorOccurredEvent(this, "TakeScreenshot", ErrorMessages.ERROR_MEDIA_FILE_ERROR);
            } else {
                AfterTakeScreenshot(savedImageName);
            }
//            savedImageName = saveFile(file, format, "SaveAs");
//            return !savedImageName.equals("");
        } catch (IOException e) {
            form.dispatchErrorOccurredEvent(this, "TakeScreenshot", ErrorMessages.ERROR_MEDIA_FILE_ERROR);
        }
    }

    @SimpleEvent
    public void AfterTakeScreenshot(String imageName) {
        EventDispatcher.dispatchEvent(this, "AfterTakeScreenshot", imageName);
    }

    // Helper method for Save and SaveAs
    private String saveFile(java.io.File file, Bitmap.CompressFormat format, String method) {
        try {
            boolean success = false;
            FileOutputStream fos = new FileOutputStream(file);
            // Don't cache, in order to save memory.  It seems unlikely to be used again soon.
            View v1 = container.$context().getWindow().getDecorView();
            v1.setDrawingCacheEnabled(true);
            v1.setDrawingCacheEnabled(false);
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            try {
                success = bitmap.compress(format,
                        100,  // quality: ignored for png
                        fos);
            } finally {
                fos.close();
            }
            if (success) {
                return file.getAbsolutePath();
            } else {
                container.$form().dispatchErrorOccurredEvent(this, method, ErrorMessages.ERROR_CANVAS_BITMAP_ERROR);
            }
        } catch (FileNotFoundException e) {
            container.$form().dispatchErrorOccurredEvent(this, method, ErrorMessages.ERROR_MEDIA_CANNOT_OPEN, file.getAbsolutePath());
        } catch (IOException e) {
            container.$form().dispatchErrorOccurredEvent(this, method,
                    ErrorMessages.ERROR_MEDIA_FILE_ERROR, e.getMessage());
        }
        return "";
    }

}
