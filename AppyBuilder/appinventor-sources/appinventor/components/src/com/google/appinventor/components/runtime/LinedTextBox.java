// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html


package com.google.appinventor.components.runtime;

import android.util.AttributeSet;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import android.content.Context;
import android.graphics.Paint;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * A box in which the user can enter text.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author halabelson@google.com (Hal Abelson)
 */
@DesignerComponent(version = YaVersion.TEXTBOX_COMPONENT_VERSION,
    description = "<p>A box for the user to enter text.  The initial or " +
    "user-entered text value is in the <code>Text</code> property.  If " +
    "blank, the <code>Hint</code> property, which appears as faint text " +
    "in the box, can provide the user with guidance as to what to type.</p>" +
    "<p>The <code>MultiLine</code> property determines if the text can have" +
    "more than one line.  For a single line text box, the keyboard will close" +
    "automatically when the user presses the Done key.  To close the keyboard " +
    "for multiline text boxes, the app should use  the HideKeyboard method or " +
    " rely on the user to press the Back key.</p>" +
    "<p>The <code> NumbersOnly</code> property restricts the keyboard to accept" +
    "numeric input only.</p>" +
    "<p>Other properties affect the appearance of the text box " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be used (<code>Enabled</code>).</p>" +
    "<p>Text boxes are usually used with the <code>Button</code> " +
    "component, with the user clicking on the button when text entry is " +
    "complete.</p>" +
    "<p>If the text entered by the user should not be displayed, use " +
    "<code>PasswordTextBox</code> instead.</p>",
    category = ComponentCategory.EXPERIMENTAL)
@SimpleObject
public final class LinedTextBox extends TextBoxBase {
  /* TODO(user): this code requires Android SDK M5 or newer - we are currently on M3
  enables this when we upgrade

  // Backing for text during validation
  private String text;

  private class ValidationTransformationMethod extends TransformationMethod {
   @Override
   public CharSequence getTransformation(CharSequence source) {
     BooleanReferenceParameter accept = new BooleanReferenceParameter(false);
     Validate(source.toString, accept);

     if (accept.get()) {
       text = source.toString();
     }

     return text;
   }
 }
*/


  // If true, then accept numeric keyboard input only
  private boolean acceptsNumbersOnly;

  // If true, then text box is multiline
  private boolean multiLine;

  /**
   * Creates a new TextBox component.
   *
   * @param container  container, component will be placed in
   */
  public LinedTextBox(ComponentContainer container) {
    super(container, new EditText(container.$context()) {

      @Override
      protected void onDraw(android.graphics.Canvas canvas) {
        Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0x80000000);    //todo: also make this user-configurable

        int left = getLeft();
        int right = getRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int height = getHeight();
        int lineHeight = getLineHeight();
        int count = (height-paddingTop-paddingBottom) / lineHeight;

        for (int i = 0; i < count; i++) {
          int baseline = lineHeight * (i+1) + paddingTop;
          canvas.drawLine(left+paddingLeft, baseline, right-paddingRight, baseline, mPaint);
        }

        super.onDraw(canvas);
      }
    });

    NumbersOnly(false);
    MultiLine(true);

    // We need to set the IME options here.  Otherwise, Android's default
    // behavior is that the action button will be Done or Next, depending on
    // whether there is a next textbox on the screen,  That might be convenient,
    // but it seems a little obscure to be the standard behavior for
    // App Inventor.  Perhaps we could make that a property.
    // This same line must be in to PasswordTextBox.  We could have put it into
    // TextBoxBase, but we might later be adding
    // other flavors of text boxes that might want their own defaults.
    view.setImeOptions(EditorInfo.IME_ACTION_DONE);
  }


  /**
   * NumbersOnly property getter method.
   *
   * @return {@code true} indicates that the textbox accepts numbers only, {@code false} indicates
   *         that it accepts any text
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "If true, then this text box accepts only numbers as keyboard input.  " +
      "Numbers can include a decimal point and an optional leading minus sign.  " +
      "This applies to keyboard input only.  Even if NumbersOnly is true, you " +
      "can use [set Text to] to enter any text at all.")
  public boolean NumbersOnly() {
    return acceptsNumbersOnly;
  }

  /**
   * NumersOnly property setter method.
   *
   * @param acceptsNumbersOnly {@code true} restricts input to numeric,
   * {@code false} allows any text
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(
      description = "If true, then this text box accepts only numbers as keyboard input.  " +
      "Numbers can include a decimal point and an optional leading minus sign.  " +
      "This applies to keyboard input only.  Even if NumbersOnly is true, you " +
      "can use [set Text to] to enter any text at all.")
  public void NumbersOnly(boolean acceptsNumbersOnly) {
    if (acceptsNumbersOnly) {
      view.setInputType(
          InputType.TYPE_CLASS_NUMBER |
          InputType.TYPE_NUMBER_FLAG_SIGNED |
          InputType.TYPE_NUMBER_FLAG_DECIMAL);
    } else {
      view.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }
    this.acceptsNumbersOnly = acceptsNumbersOnly;
  }

//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
//  @SimpleProperty(userVisible = false)
//  public void ShowNotepadLines(boolean enabled) {
//    this.showNotepadLines = enabled;
//  }
//
//  @SimpleProperty(description = "Indicates if component will contain notepad lines in the background")
//  public boolean ShowNotepadLines() {
//    return this.showNotepadLines;
//  }

  /**
   * Hide the soft keyboard
   */
  @SimpleFunction(
      description = "Hide the keyboard.  Only multiline text boxes need this. " +
      "Single line text boxes close the keyboard when the users presses the Done key.")
  public void HideKeyboard() {
    InputMethodManager imm =
      (InputMethodManager) container.$context().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  /**
   * Multi line property getter method.
   *
   * @return {@code true} indicates that the textbox accepts multiple lines
   *         {@code false} lines that the textbox  accepts only a single line of input,
   */
//  @SimpleProperty(
//      category = PropertyCategory.BEHAVIOR,
//      description = "If true, then this text box accepts multiple lines of input, which are " +
//                    "entered using the return key.  For single line text boxes there is a Done " +
//                    "key instead of a return key, and pressing Done hides the keyboard.  " +
//                    "The app should call the HideKeyboard method to hide the keyboard for " +
//                    "a mutiline text box.")
  public boolean MultiLine() {
    return multiLine;
  }

  /**
   * MultiLine property setter method.
   *
   * @param multiLine {@code true} lets to textbox accept multiple lines of input
   * {@code false} restricts the textbox to only a single line
   */
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
//  @SimpleProperty()
  public void MultiLine(boolean multiLine) {
    this.multiLine = multiLine;
    view.setSingleLine(!multiLine);
  }

  // ============================
  public class LinedEditText extends EditText {
    private Paint mPaint = new Paint();

    public LinedEditText(Context context) {
      super(context);
      initPaint();
    }

    public LinedEditText(Context context, AttributeSet attrs) {
      super(context, attrs);
      initPaint();
    }

    public LinedEditText(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      initPaint();
    }

    private void initPaint() {
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setColor(0x80000000);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
      int left = getLeft();
      int right = getRight();
      int paddingTop = getPaddingTop();
      int paddingBottom = getPaddingBottom();
      int paddingLeft = getPaddingLeft();
      int paddingRight = getPaddingRight();
      int height = getHeight();
      int lineHeight = getLineHeight();
      int count = (height-paddingTop-paddingBottom) / lineHeight;

      for (int i = 0; i < count; i++) {
        int baseline = lineHeight * (i+1) + paddingTop;
        canvas.drawLine(left+paddingLeft, baseline, right-paddingRight, baseline, mPaint);
      }

      super.onDraw(canvas);
    }
  }

}
