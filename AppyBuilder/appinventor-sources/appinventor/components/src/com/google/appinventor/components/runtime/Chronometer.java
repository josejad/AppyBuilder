package com.google.appinventor.components.runtime;

import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

import java.io.IOException;


/**
 * Chronometer class
 *
 * @author M. Hossein Amerkashi (kkashi01@gmail.com)
 */
@DesignerComponent(version = YaVersion.CHRONOMETER_COMPONENT_VERSION,
        category = ComponentCategory.USERINTERFACE,
        description = "<p>A Chronometer is a simple timer that can be started or stopped</p>")
@SimpleObject
public class Chronometer extends AndroidViewComponent
        implements OnClickListener, OnFocusChangeListener, OnLongClickListener
{

    private static final String LOG_TAG = "Chronometer";
    private boolean hasStarted=false;

    protected android.widget.Chronometer view;
    private long lastPause=0;
    // Backing for background color
    private int backgroundColor;

    // Image path
    private String imagePath = "";

    // This is our handle on Android's nice 3-d default button.
    private Drawable defaultButtonDrawable;

    private long time=0;

    // This is the Drawable corresponding to the Image property.
    // If an Image has never been set or if the most recent Image
    // could not be loaded, this is null.
    private Drawable backgroundImageDrawable;
    private boolean shouldResume=false;
    private boolean shouldStart=false;
    // Backing for font typeface
    private int fontTypeface;

    // Backing for font bold
    private boolean bold;

    // Backing for font italic
    private boolean italic;

    // Backing for text color
    private int textColor;

    /**
     * Creates a new ButtonBase component.
     *
     * @param container container, component will be placed in
     */
    public Chronometer(ComponentContainer container)
    {
        super(container);
        view = new android.widget.Chronometer(container.$context());
//        view = new AnalogClock(container.$context());

        defaultButtonDrawable = view.getBackground();

        // Adds the component to its designated container
        container.$add(this);

        // Listen to clicks and focus changes
        view.setOnClickListener(this);
        view.setOnFocusChangeListener(this);
        view.setOnLongClickListener(this);
        // BackgroundColor and Image are dangerous properties:
        // Once either of them is set, the 3D bevel effect for the button is
        // irretrievable, except by reloading defaultButtonDrawable, defined above.
        BackgroundColor(COLOR_NONE);
        TextColor(Component.COLOR_BLACK);
        FontSize(Component.FONT_DEFAULT_SIZE);
        Started(false);

        Image("");
    }


    @Override
    public View getView()
    {
        return view;
    }

    /**
     * Default GotFocus event handler.
     */
    @SimpleEvent
    public void GotFocus()
    {
        EventDispatcher.dispatchEvent(this, "GotFocus");
    }

    /**
     * Default LostFocus event handler.
     */
    @SimpleEvent
    public void LostFocus()
    {
        EventDispatcher.dispatchEvent(this, "LostFocus");
    }


    /**
     * Returns the path of the button's image.
     *
     * @return  the path of the button's image
     */
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public String Image()
    {
        return imagePath;
    }

    /**
     * Specifies the path of the button's image.
     *
     * <p/>See {@link MediaUtil#determineMediaSource} for information about what
     * a path can be.
     *
     * @param path  the path of the button's image
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
            defaultValue = "")
    @SimpleProperty(description = "Specifies the path of the button's image.  " +
            "If there is both an Image and a BackgroundColor, only the Image will be " +
            "visible.")
    public void Image(String path)
    {
        // If it's the same as on the prior call and the prior load was successful,
        // do nothing.
        if (path.equals(imagePath) && backgroundImageDrawable != null)
        {
            return;
        }

        imagePath = (path == null) ? "" : path;

        // Clear the prior background image.
        backgroundImageDrawable = null;

        // Load image from file.
        if (imagePath.length() > 0)
        {
            try
            {
                backgroundImageDrawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
            } catch (IOException ioe)
            {
                // TODO(user): Maybe raise Form.ErrorOccurred.
                Log.e(LOG_TAG, "Unable to load " + imagePath);
                // Fall through with a value of null for backgroundImageDrawable.
            }
        }

        // Update the appearance based on the new value of backgroundImageDrawable.
        updateAppearance();
    }

    /**
     * Returns the button's background color as an alpha-red-green-blue
     * integer.
     *
     * @return  background RGB color with alpha
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Returns the button's background color")
    public int BackgroundColor()
    {
        return backgroundColor;
    }

    /**
     * Specifies the button's background color as an alpha-red-green-blue
     * integer.  If the parameter is {@link Component#COLOR_DEFAULT}, the
     * original beveling is restored.  If an Image has been set, the color
     * change will not be visible until the Image is removed.
     *
     * @param argb background RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
    @SimpleProperty(description = "Specifies the button's background color. " +
            "The background color will not be visible if an Image is being displayed.")
    public void BackgroundColor(int argb)
    {
        backgroundColor = argb;
        view.setBackgroundColor(backgroundColor);
    }

    // Update appearance based on values of backgroundImageDrawable and backgroundColor.
    // Images take precedence over background colors.
    private void updateAppearance()
    {
        // If there is no background image, the appearance depends solely on the background color.
        if (backgroundImageDrawable == null)
        {
            if (backgroundColor == COLOR_DEFAULT)
            {
                // Restore original 3D bevel appearance.
                ViewUtil.setBackgroundDrawable(view, defaultButtonDrawable);
            }
            else
            {
                // Clear the background image.
                ViewUtil.setBackgroundDrawable(view, null);
            }
            return;
        }

        ViewUtil.setBackgroundImage(view, backgroundImageDrawable);
    }

    public void click() {
        Click();
    }

    public boolean longClick() {
        return LongClick();
    }

    // OnClickListener implementation

    @Override
    public void onClick(View view)
    {
        click();
    }

    // OnFocusChangeListener implementation

    @Override
    public void onFocusChange(View previouslyFocused, boolean gainFocus)
    {
        if (gainFocus)
        {
            GotFocus();
        }
        else
        {
            LostFocus();
        }
    }

    // OnLongClickListener implementation

    @Override
    public boolean onLongClick(View view)
    {
        return longClick();
    }


    /**
     * Toggles chronometer start / stop state.
     */
    @SimpleEvent
    public void Click() {
        if (hasStarted)
        {
            Stop();
        }
        else {
            Started();
        }
        hasStarted=!hasStarted;
        EventDispatcher.dispatchEvent(this, "Click");
    }


    /**
     * Resets chronometer to 0
     * @return
     */
    @SimpleEvent
    public boolean LongClick() {
//        return EventDispatcher.dispatchEvent(this, "LongClick");
        Stop();
        view.setBase(SystemClock.elapsedRealtime());
        lastPause=0;
        hasStarted=false;
        return EventDispatcher.dispatchEvent(this, "ClockReset");
    }




    /**
     * Specifies the chronometer's text's font size.
     *
     * @param size  font size
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
    @SimpleProperty
    public void FontSize(float size) {
        TextViewUtil.setFontSize(view, size);
    }

    @SimpleProperty
    public float FontSize() {
        return TextViewUtil.getFontSize(view);
    }


    /**
     * Returns true if the textbox is active and useable.
     *
     * @return  {@code true} indicates enabled, {@code false} disabled
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Whether the user can enter text into this input box.  " +
                    "By default, this is true.")
    public boolean Enabled() {
        return TextViewUtil.isEnabled(view);
    }

    /**
     * Specifies whether the textbox should be active and useable.
     *
     * @param enabled  {@code true} for enabled, {@code false} disabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty
    public void Enabled(boolean enabled) {
        TextViewUtil.setEnabled(view, enabled);
    }

    /**
     * Returns true if the button's text should be bold.
     * If bold has been requested, this property will return true, even if the
     * font does not support bold.
     *
     * @return  {@code true} indicates bold, {@code false} normal
     */
    @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
    public boolean FontBold() {
        return bold;
    }

    /**
     * Specifies whether the button's text should be bold.
     * Some fonts do not support bold.
     *
     * @param bold  {@code true} indicates bold, {@code false} normal
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(
            userVisible = false)
    public void FontBold(boolean bold) {
        this.bold = bold;
        TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
    }


    /**
     * Returns true if the button's text should be italic.
     * If italic has been requested, this property will return true, even if the
     * font does not support italic.
     *
     * @return  {@code true} indicates italic, {@code false} normal
     */
    @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
    public boolean FontItalic() {
        return italic;
    }

    /**
     * Specifies whether the button's text should be italic.
     * Some fonts do not support italic.
     *
     * @param italic  {@code true} indicates italic, {@code false} normal
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(userVisible = false)
    public void FontItalic(boolean italic) {
        this.italic = italic;
        TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
    }

    /**
     * Returns the label's text color as an alpha-red-green-blue
     * integer.
     *
     * @return  text RGB color with alpha
     */
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public int TextColor() {
        return textColor;
    }

    /**
     * Specifies the label's text color as an alpha-red-green-blue
     * integer.
     *
     * @param argb  text RGB color with alpha
     */
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

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean ShouldResume() {
        return shouldResume;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShouldResume(boolean value) {
        shouldResume = value;
        if (value==true) {
            time = view.getBase() - SystemClock.elapsedRealtime();
        }
    }

    @SimpleProperty
    public long ElapsedTimeInSec(){
//        return view.getBase() - SystemClock.elapsedRealtime();
        int elapsedTime = Math.round((SystemClock.elapsedRealtime() - view.getBase()) / 1000);
        return elapsedTime;

    }

    /**
     * Starts the chronometer
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "false")
    @SimpleProperty
    public void Started(boolean shouldStart)
    {
//        view.start();
//        view.setBase(SystemClock.elapsedRealtime() - lastPause);

        this.shouldStart = shouldStart;
        if (shouldStart) {
            view.setBase(SystemClock.elapsedRealtime() + time);
            view.start();
        } else {
            Stop();
        }
    }

    @SimpleProperty
    public boolean Started()
    {
        return shouldStart;
    }

    /**
     * Stops the chronometer
     */
    public void Stop()
    {
//        view.stop();
//        lastPause = (int)(SystemClock.elapsedRealtime() - view.getBase());
        if (shouldResume) {
            time = view.getBase() - SystemClock.elapsedRealtime();
        } else {
            time =0;
        }
        view.stop();

    }
}
