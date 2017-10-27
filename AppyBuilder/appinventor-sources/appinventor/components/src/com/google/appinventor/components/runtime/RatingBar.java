package com.google.appinventor.components.runtime;

import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
        description = "Update ode message" ,
        category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class RatingBar extends AndroidViewComponent implements android.widget.RatingBar.OnRatingBarChangeListener {

    public static final int VERSION = 1;
    private ComponentContainer container;
    private android.widget.RatingBar ratingBar;
    private int stars=5;
    private float stepSize = .5f;
    private float rating = 4.5f;
    private int color=0;
    private int backgroundColor;
    private int starBackgroundColor;
    private boolean allowSelection = true;

    public RatingBar(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        ratingBar = new android.widget.RatingBar(container.$context());
//        ratingBar = new android.widget.RatingBar(container.$context(), null, android.R.attr.ratingBarStyleSmall);

        ratingBar.setOnRatingBarChangeListener(this);

        BackgroundColor(Component.COLOR_NONE);
        StarsBackgroundColor(Component.COLOR_LTGRAY);
        StarColor(Component.COLOR_RED);
        Rating(rating);  // the default in method is 4.5

        NumStars(stars);
        StepSize(stepSize);
        Rating(rating);
        AllowSelection(allowSelection);

        container.$add(this);
    }

    /**
     * Specifies the label's text color as an alpha-red-green-blue
     * integer.
     *
     * @param argb  text RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_RED)
    @SimpleProperty(description="Sets the color of the stars. ")
    public void StarColor(int argb) {
            color = argb;
            LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_CYAN)
//    @SimpleProperty(description="Sets the color of the stars. ")
//    public void StarSize(int argb) {
//        ratingBar = new android.widget.RatingBar(container.$context(), null, android.R.attr.ratingBarStyleSmall);
//    }

    @SimpleProperty(description = "Gets the current Stars color")
    public int StarColor() {
        return color;
    }


    @Override
    public View getView() {
        return ratingBar;
    }


    @SimpleEvent(description = "Event that occurs as user selects a rating")
    public void AfterSelecting(float rating) {
        EventDispatcher.dispatchEvent(this, "AfterSelecting", rating);
    }

    @DesignerProperty(defaultValue="5", editorType= PropertyTypeConstants.PROPERTY_TYPE_INTEGER)
    @SimpleProperty(description="Set the the number of stars to be displayed")
    public void NumStars(int stars) {
        this.stars = stars;
        ratingBar.setNumStars(stars);
    }

    @SimpleProperty(description="Gets the the number of stars")
    public int NumStars() {
        return this.stars;
    }

    @DesignerProperty(defaultValue=".5", editorType= PropertyTypeConstants.PROPERTY_TYPE_FLOAT)
    @SimpleProperty(description="Sets the step size (granularity) of this rating bar")
    public void StepSize(float stepSize) {
        this.stepSize = stepSize;
        ratingBar.setStepSize(stepSize);
    }

    @SimpleProperty(description="Gets the the step size")
    public float StepSize() {
        return this.stepSize;
    }

    @DesignerProperty(defaultValue="4.5", editorType= PropertyTypeConstants.PROPERTY_TYPE_FLOAT)
    @SimpleProperty(description="Sets the initial rating (the number of stars filled).")
    public void Rating(float rating) {
        this.rating = rating;
        ratingBar.setRating(rating);
    }

    @SimpleProperty(description="Gets the the number of rating")
    public float Rating() {
        return this.rating;
    }

    @DesignerProperty(defaultValue="True", editorType= PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)
    @SimpleProperty(description="Should user be allowed to change selection?")
    public void AllowSelection(boolean enabled) {
        this.allowSelection = enabled;
        ratingBar.setIsIndicator(!enabled);
    }

    @SimpleProperty(description="Is user allowed to make selection?")
    public boolean AllowSelection() {
        return !allowSelection;
    }

    /**
     * Returns the label's background color as an alpha-red-green-blue
     * integer.
     *
     * @return  background RGB color
     */
    @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Gets the current color of this component")
    public int BackgroundColor() {
        return backgroundColor;
    }

    /**
     * Specifies the label's background color as an alpha-red-green-blue
     * integer.
     *
     * @param argb  background RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
    @SimpleProperty(description = "Sets the background of this component")
    public void BackgroundColor(int argb) {
        backgroundColor = argb;
        ratingBar.setBackgroundColor(argb);
//        getView().invalidate();
    }
    @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Gets the stars background color")
    public int StarsBackgroundColor() {
        return starBackgroundColor;
    }

    /**
     * Specifies the label's background color as an alpha-red-green-blue
     * integer.
     *
     * @param argb  background RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
    @SimpleProperty(description = "Sets the stars background color")
    public void StarsBackgroundColor(int argb) {
        starBackgroundColor = argb;
        LayerDrawable progressDrawable = (LayerDrawable) ratingBar.getProgressDrawable();
        DrawableCompat.setTint(progressDrawable.getDrawable(0), argb);
//        getView().invalidate();
    }

    @Override
    public void onRatingChanged(android.widget.RatingBar ratingBar, float rating, boolean fromUser) {
        AfterSelecting(rating);
    }
}



