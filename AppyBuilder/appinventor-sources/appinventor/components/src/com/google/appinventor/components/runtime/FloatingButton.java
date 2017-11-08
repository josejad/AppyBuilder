// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;

import java.io.IOException;

@DesignerComponent(version = YaVersion.FLOATING_BUTTON_COMPONENT_VERSION,
        description = "A FloatingButton component is a non-visual component that can be shown as floating button " +
                "on the screen. By default, the button will be at bottom-right of screen. " +
                "NOTE: For this to work, the Screen Scrollable should be set to true.",
        category = ComponentCategory.USERINTERFACE,
        nonVisible = true,
        iconName = "images/floatingButton.png")

@SimpleObject
public class FloatingButton extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "FloatingButton";

  // Image path
  private String imagePath = "";
  private Drawable backgroundImageDrawable;
  private FrameLayout.LayoutParams params;

  final static OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
  final static AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
  private Builder fabButton;

  private Context context;  // this was a local in constructor and final not private

  private ComponentContainer container;
  private boolean enabled = true;
  private Boolean visible = true;

  private FloatingActionButton view;
  private ViewGroup rootViewGroup;
  private int marginLeft = 0;  // we don't change this
  private int marginTop = 0;  // we don't change this
  private int marginRight = 16;
  private int marginBottom = 16;
  private int size = 32;
  private int position = 0;

  /**
   * Creates a new FloatingButton component.
   *
   * @param container the Form that this component is contained in.
   */
  public FloatingButton(ComponentContainer container) {
    super(container.$form());
    this.container = container;
    context = (Context) container.$context();
    Log.e(LOG_TAG, "FloatingButton constructor");

    fabButton = new Builder(context)
            .withDrawable(container.$context().getResources(). getDrawable(android.R.drawable.ic_input_add))
            .withButtonColor(Color.WHITE)
            .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
            .withButtonSize(container.$form().convertDpToDensity(this.size))
            .withMargins(marginLeft, marginTop, marginRight, marginBottom);
    ;
    fabButton.create();
    Enabled(true);

//    intcontainer.$form().addContentView(fabButton);
  }

  /**
   * Returns true if the button is active and clickable.
   *
   * @return {@code true} indicates enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty(
          category = PropertyCategory.BEHAVIOR,
          description = "If set, user can tap check box to cause action.")
  public boolean Enabled() {
    return enabled;
  }

  /**
   * Specifies whether the button should be active and clickable.
   *
   * @param enabled {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    this.enabled = enabled;
    view.setEnabled(enabled);
  }

  /**
   * Returns the path of the button's image.
   *
   * @return the path of the button's image
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Image to display on button.")
  public String Image() {
    return imagePath;
  }

  /**
   * Specifies the path of the button's image.
   * <p/>
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path the path of the button's image
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  @SimpleProperty(description = "Specifies the path of the button's image.  " +
          "If there is both an Image and a BackgroundColor, only the Image will be visible.")
  public void Image(String path) {
    // If it's the same as on the prior call and the prior load was successful, do nothing.
    if (path.equals(imagePath)) {
      return;
    }

    imagePath = (path == null) ? "" : path;

    // Load image from file.
    if (imagePath.length() > 0) {
      try {
        backgroundImageDrawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
        view.setFloatingActionButtonDrawable(backgroundImageDrawable);
//        ViewUtil.setBackgroundImage(view, backgroundImageDrawable);

      } catch (IOException ioe) {
        // TODO(user): Maybe raise Form.ErrorOccurred.
        Log.e(LOG_TAG, "Unable to load " + imagePath);
      }
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "48")
  @SimpleProperty(description = "Specifies the button size. Default is 56 x 56. This would be ideal if your image size is 48 x 48. Always add 8 for button size.")
  public void ButtonSize(int size) {
    this.size = size;
    fabButton.setImageSize(size);
  }

//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOATING_BUTTON_LOCATIONS, defaultValue = "0")
  public void ButtonPosition(int position) {
    this.position = position;
    if (position == 0 ) fabButton = fabButton.withGravity(Gravity.BOTTOM | Gravity.RIGHT);
    else if (position == 1 ) fabButton.setGravity(Gravity.TOP | Gravity.RIGHT);
    else if (position == 2 ) fabButton.setGravity(Gravity.LEFT | Gravity.BOTTOM);
    else if (position == 3 ) fabButton.setGravity(Gravity.LEFT | Gravity.TOP);
    else if (position == 4 ) fabButton.setGravity(Gravity.CENTER | Gravity.TOP);
    else if (position == 5 ) fabButton.setGravity(Gravity.CENTER | Gravity.BOTTOM);
  }

  @SimpleEvent(description = "Indicates that the button was pressed down.")
  public void TouchDown() {
    EventDispatcher.dispatchEvent(this, "TouchDown");
  }

  @SimpleEvent(description = "Indicates that the button has been released.")
  public void TouchUp() {
    EventDispatcher.dispatchEvent(this, "TouchUp");
  }

//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "0")
//  @SimpleProperty(description = "Sets up left margin")
  public void MarginLeft(int marginLeft) {
    this.marginLeft = marginLeft;
    fabButton = fabButton.withMargins(marginLeft, marginTop, marginRight, marginBottom);
//    view.invalidate(); // force redraw
  }



//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "0")
//  @SimpleProperty(description = "Sets up top margin")
  public void MarginTop(int marginTop) {
    this.marginTop = marginTop;
    fabButton = fabButton.withMargins(marginLeft, marginTop, marginRight, marginBottom);
//    view.invalidate(); // force redraw
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "16")
  @SimpleProperty(description = "Sets up right margin")
  public void MarginRight(int marginRight) {
    this.marginRight = marginRight;
    fabButton = fabButton.withMargins(marginLeft, marginTop, marginRight, marginBottom);
//      view.invalidate(); // force redraw

    if (true) return;

    if (rootViewGroup != null) {
      Log.e(LOG_TAG, "Removing view for MarginRight");
      rootViewGroup.removeView(view);
    }
//    this.marginRight = marginRight;
//    fabButton = new Builder(context)
//            .withDrawable(container.$context().getResources().getDrawable(android.R.drawable.ic_input_add))
//            .withButtonColor(Color.WHITE)
//            .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
//            .withMargins(0, 0, marginRight, marginBottom)
//    ;
//    fabButton.createButton();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "16")
  @SimpleProperty(description = "Sets up bottom margins")
  public void MarginBottom(int marginBottom) {
    this.marginBottom = marginBottom;
    fabButton = fabButton.withMargins(marginLeft, marginTop, marginRight, marginBottom);
//    view.invalidate(); // force redraw

    if (true) return;

    if (rootViewGroup != null) {
      Log.e(LOG_TAG, "Removing view for MarginBottom");
      rootViewGroup.removeView(view);
    }
//    this.marginBottom = marginBottom;
//    fabButton = new Builder(context)
//            .withDrawable(container.$context().getResources().getDrawable(android.R.drawable.ic_input_add))
//            .withButtonColor(Color.WHITE)
//            .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
//            .withMargins(0, 0, marginRight, marginBottom)
//    ;
//    fabButton.createButton();
  }


  //  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "16,16")
//  @SimpleFunction(description = "Sets up top, left, bottom, right margins of this ActionButton")
  public void MarginsFromCsvRB(String csvLTRB) {

    // Check for empty parameter and setup default if non passed in
    Log.e(LOG_TAG, "MarginsFromCsvLTRB");
    csvLTRB = csvLTRB.trim();
    csvLTRB = csvLTRB.isEmpty() ? "16,16" : csvLTRB;

    //get rid of all embedded spaces
    csvLTRB = csvLTRB.replaceAll(" ", "");

    String[] margins = csvLTRB.split(",");

    if (margins.length != 2) {
      form.dispatchErrorOccurredEvent(this, "MarginsFromCsvRB",
              3000);
    }

    try {
      fabButton.withMargins(0, 0,
              Integer.valueOf(margins[0]),
              Integer.valueOf(margins[1]));
    } catch (NumberFormatException e) {
      form.dispatchErrorOccurredEvent(this, "MarginsFromCsvLTRB",
              ErrorMessages.ERROR_FLOATING_BUTTON_INVALID_MARGINS);
    }

    view.invalidate(); // force redraw
  }

  /**
   * Returns true iff the component is visible.
   *
   * @return true iff the component is visible
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public boolean Visible() {
    return this.visible;
  }

  /**
   * Specifies whether the component should be visible on the screen.  Value is true if the
   * component is showing and false if hidden.
   *
   * @param visibility desired state
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY, defaultValue = "True")
  @SimpleProperty(description = "Specifies whether the component should be visible on the screen. "
          + "Value is true if the component is showing and false if hidden.")
  public void Visible(Boolean visibility) {
    // The principle of least astonishment suggests we not offer the
    // Android option INVISIBLE.
    this.visible = visibility;
    if (visible) view.showFloatingActionButton();
    else view.hideFloatingActionButton();
//    view.setVisibility(visibility ? View.VISIBLE : View.GONE);
  }



  /**
   * Indicates a user has clicked on the button.
   */
  @SimpleEvent(description = "User tapped and released the button.")
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }


  /**
   * Specifies the background color
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = DEFAULT_VALUE_COLOR_NONE)
  @SimpleProperty
  public void BackgroundColor(int argb) {
    if (view != null) {
      view.setFloatingActionButtonColor(argb);
    }
  }


  class FloatingActionButton extends View {
    Context context;
    Paint mButtonPaint;
    Paint mDrawablePaint;
    Bitmap mBitmap;
    boolean mHidden = false;

    public FloatingActionButton(Context context) {
      super(context);
      this.context = context;
      init(Color.WHITE);
      this.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Click();
        }
      });
      this.setOnTouchListener(new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
          if (event.getAction() == MotionEvent.ACTION_UP) {
            view.setAlpha(1.0f);
//            RotateAnimation animation = new RotateAnimation(90f, 0f, getWidth()/2, getHeight()/2);
//            animation.setDuration(100);
//            animation.setInterpolator(overshootInterpolator);
//            animation.setFillAfter(true);
//            animation.start();
            TouchUp();
          } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.setAlpha(0.6f);
//            RotateAnimation animation = new RotateAnimation(0f, 90f, getWidth()/2, getHeight()/2);
//            animation.setDuration(100);
//            animation.setInterpolator(overshootInterpolator);
//            animation.start();
            TouchDown();
          }
          return false;
        }
      });
    }

    public void setFloatingActionButtonColor(int buttonColor) {
      init(buttonColor);
    }

    public void setFloatingActionButtonDrawable(Drawable FloatingActionButtonDrawable) {
      mBitmap = ((BitmapDrawable) FloatingActionButtonDrawable).getBitmap();
      invalidate();
    }

    public void init(int buttonColor) {
      setWillNotDraw(false);
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);

      mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      mButtonPaint.setColor(buttonColor);
      mButtonPaint.setStyle(Paint.Style.FILL);
//      mButtonPaint.setShadowLayer(
//              10.0f,  //float radius
//              0.0f,   //float dx
//              3.5f,   //float dy
//              Color.argb(100, 0, 0, 0)    //int color
//      );
      mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

      invalidate();
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
      setClickable(true);
      canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);
      canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
              (getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);
    }


    public void hideFloatingActionButton() {
      if (!mHidden) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1, 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(accelerateInterpolator);
        animSetXY.setDuration(100);
        animSetXY.start();
        mHidden = true;
        view.setVisibility(View.GONE);
        invalidate();
      }
    }

    public void showFloatingActionButton() {
      if (mHidden) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0, 1);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(overshootInterpolator);
        animSetXY.setDuration(200);
        animSetXY.start();
        mHidden = false;
        invalidate();
        view.setVisibility(View.VISIBLE);
      }
    }

    public boolean isHidden() {
      return mHidden;
    }

  }

  public class Builder {
//    float scale;
    //      private final Activity activity;
    int gravity = Gravity.BOTTOM | Gravity.RIGHT; // default bottom right

    Drawable drawable;
    int color = Color.WHITE;

    public Builder(Context context) {
//      scale = context.getResources().getDisplayMetrics().density;
//      size = convertToPixels(size, scale); // default size is 72dp by 72dp
//      params = new FrameLayout.LayoutParams(container.$form().convertDpToDensity(size), container.$form().convertDpToDensity(size));
      params = new FrameLayout.LayoutParams(size, size);
      params.gravity = gravity;

//        this.activity = context;
    }

    /**
     * Sets the gravity for the FAB
     */
    public Builder withGravity(int gravity) {
      this.gravity = gravity;
      return this;
    }

    /**
     * Sets the margins for the FAB in dp
     */
    public Builder withMargins(int left, int top, int right, int bottom) {
      params.setMargins(container.$form().convertDpToDensity(left)
              ,container.$form().convertDpToDensity(top)
              ,container.$form().convertDpToDensity(right)
              ,container.$form().convertDpToDensity(bottom)
              );
      return this;
    }

    /**
     * Sets the FAB drawable
     */
    public Builder withDrawable(final Drawable drawable) {
      this.drawable = drawable;
      return this;
    }

    /**
     * Sets the FAB color
     */
    public Builder withButtonColor(final int color) {
      this.color = color;
      return this;
    }

    /**
     * Sets the FAB size in dp
     */
    public Builder withButtonSize(int size) {
//      scale = context.getResources().getDisplayMetrics().density;
//      size = convertToPixels(size, scale); // default size is 72dp by 72dp
      params = new FrameLayout.LayoutParams(size, size);
      return this;
    }

    public void setImageSize(int size) {
//      scale = context.getResources().getDisplayMetrics().density;
//      size =  convertToPixels(size, scale); // default size is 72dp by 72dp
      if (params != null) {
        params.height = container.$form().convertDpToDensity(size);
        params.width = container.$form().convertDpToDensity(size);

      }
    }

    public void setGravity(int gravity) {
      if (params!= null ) {
        params.gravity = gravity;
        view.invalidate();
      }
    }

    public FloatingActionButton create() {
      Log.e(LOG_TAG, "in create view is: " + (view == null ? "null" : "not null"));
      if (view == null) {
        Log.e(LOG_TAG, "in create. creating view");
        view = new FloatingActionButton(context);
        view.setFloatingActionButtonColor(this.color);
        view.setFloatingActionButtonDrawable(this.drawable);
        params.gravity = this.gravity;
        rootViewGroup = (ViewGroup) container.$form().findViewById(android.R.id.content);

        rootViewGroup.addView(view, params);
//      if (view == null) {
//        Log.e(LOG_TAG, "Creating FloatingActionButton2");
//        rootViewGroup.addView(button, params);
//      } else {
//        Log.e(LOG_TAG, "FloatingActionButton2 view already exists");
//      }


//      container.$form().addContentView(rootViewGroup, params);
//      container.$form().addContentView(button, params);

//      rootViewGroup.addView(button, params);
      }
      return view;
    }


    // The calculation (value * scale + 0.5f) is a widely used to convert to dps to pixel units
    // based on density scale
    // see developer.android.com (Supporting Multiple Screen Sizes)
    private int convertToPixels(int dp, float scale) {
      return (int) (dp * scale + 0.5f);
    }
  }

}
