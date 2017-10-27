package com.google.appinventor.components.runtime;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CompoundButton;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.TextViewUtil;

/**
 * This class is used to display a ToggleButton.
 * <p>A ToggleButton Displays checked/unchecked states as a button with a 'light' indicator and by default
 * accompanied with the text 'ON' or 'OFF'.
 *
 * @author kashi01@gmail.com (M. Hossein Amerkashi)
 */
@DesignerComponent(version = YaVersion.TOGGLER_COMPONENT_VERSION,
    description = "Update ODE ",
    category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class Switch extends AndroidViewComponent implements CompoundButton.OnCheckedChangeListener{
  private final static String LOG_TAG = "Switch";
  private android.widget.Switch view;
    private int textColor;
    private String switchText = "Switch: ";

    /**
   * Creates a new Switch component.
   *
   * @param container container that the component will be placed in
   */
  public Switch(ComponentContainer container) {
      super(container);
      view = new android.widget.Switch(container.$context());
      container.$add(this);

      view.setOnCheckedChangeListener(this);
      Text(switchText);
      FontSize(Component.FONT_DEFAULT_SIZE);
      TextColor(Component.COLOR_BLACK);
      ThumbColor(Component.COLOR_RED);
      TrackColor(Component.COLOR_LTGRAY);
  }

  @Override
  public View getView() {
    return view;
  }


    /**
     * The text for the button when it is checked
     * @param text  The text for the button when it is checked
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Switch: ")
    @SimpleProperty(description = "Text for switch")
    public void Text(String text) {
        switchText = text;
        view.setText(text);
    }

    @SimpleProperty(description = "Gets the value of Switch text")
    public String Text() {
        return switchText;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty
    public void TextColor(int argb) {
        textColor = argb;
        if (argb != Component.COLOR_DEFAULT) {
            TextViewUtil.setTextColor(view, argb);
        } else {
            TextViewUtil.setTextColor(view, Component.COLOR_BLACK);
        }
    }

    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public int TextColor() {
        return textColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
    public void FontSize(float size) {
        TextViewUtil.setFontSize(view, size);
    }


    /**
     * enables or disables the component
     *
     * @param enabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Enables or disables the component")
    public void Enabled(boolean enabled) {
       view.setEnabled(enabled);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_RED)
    @SimpleProperty(description = "The thumb color")
    public void ThumbColor(int argb) {
//        int tint = Color.parseColor(String.valueOf(argb));  //don't do this, crashes
        Drawable drawable =  view.getThumbDrawable();
        drawable.setColorFilter(argb, PorterDuff.Mode.MULTIPLY);
    }

     @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
    @SimpleProperty(description = "The track color")
    public void TrackColor(int argb) {
         Drawable drawable =  view.getTrackDrawable();
//         drawable.setColorFilter(argb, PorterDuff.Mode.MULTIPLY);   //thumb color won't change between on, off state
         drawable.setColorFilter(argb, PorterDuff.Mode.LIGHTEN);
//        int tint = Color.parseColor(String.valueOf(argb));
//        view.getTrackDrawable().setColorFilter(tint, PorterDuff.Mode.MULTIPLY);
    }

    /**
     * Returns true if the toggler is on.
     *
     * @return  {@code true} indicates toggler is in ON state, {@code false} OFF state
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Checked() {
        return view.isChecked();
    }

    /**
     * Specifies whether the toggler should be in ON state or OFF state
     *
     * @param enabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Sets state to checked or unchecked")
    public void Checked(boolean enabled) {
       view.setChecked(enabled);
    }

    /**
     * Default Changed event handler.
     */
    @SimpleEvent(description = "Triggered when state of Switch changes. Use isChecked to determine if checked or not-checked")
    public void Click(boolean isChecked) {
        EventDispatcher.dispatchEvent(this, "Click", isChecked);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
        view.setChecked(enabled);
        Click(enabled);
    }


}
