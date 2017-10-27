package com.google.appinventor.components.runtime;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.github.ybq.android.spinkit.style.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

/**
 * Label containing a text string.
 */
@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
        description = "<p>A ProgressBar is an animated visual indicator of progress in some operation. It will allow you to setup indicator using different animation styles.</p>",
        category = ComponentCategory.USERINTERFACE)
@SimpleObject
@UsesLibraries(libraries = "spinkit.jar")
public final class ProgressBar extends AndroidViewComponent {
    private final android.widget.ProgressBar view;

    // Backing for background color
    private int backgroundColor;
    private static final String LOG_TAG = "ProgressBar";
//    private int color = COLOR_WHITE;
    private int color;
    private String style = "Wave";

    public ProgressBar(ComponentContainer container) {
        super(container);
        view = new android.widget.ProgressBar(container.$context());

        // Color will also invoke AnimationStyle
        Color(-16726330);  // cyan dark

        // Adds the component to its designated container
        container.$add(this);

        // Default property values
//        BackgroundColor(backgroundColor);
        BackgroundColor(COLOR_NONE);
//        AnimationStyle("Wave");
    }

    @Override
    public View getView() {
        return view;
    }


    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public int BackgroundColor() {
        return backgroundColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
    @SimpleProperty
    public void BackgroundColor(int argb) {
        backgroundColor = argb;
        view.setBackgroundColor(argb);
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public int Color() {
        return color;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = "-16726330") //cyan dark
    @SimpleProperty
    public void Color(int argb) {
        this.color = argb;
        AnimationStyle(this.style);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_PROGRESSBAR_OPTIONS, defaultValue = "Wave")
    @SimpleProperty(description = "Allows you to specify animation style of ProgressBar", userVisible=false)
    public void AnimationStyle(String style) {
        style=style.toLowerCase();
        this.style = style;
        if (style.equalsIgnoreCase("ChasingDots")) {
            ChasingDots chasingDots = new ChasingDots();
            chasingDots.setColor(color);
            view.setIndeterminateDrawable(chasingDots);
        } else if (style.equalsIgnoreCase("Circle")) {
            Circle circle = new Circle();
            circle.setColor(color);
            view.setIndeterminateDrawable(circle);

        } else if (style.equalsIgnoreCase("CubeGrid")) {
            CubeGrid cubeGrid = new CubeGrid();
            cubeGrid.setColor(color);
            view.setIndeterminateDrawable(cubeGrid);
        } else if (style.equalsIgnoreCase("DoubleBounce")) {
            DoubleBounce DoubleBounce = new DoubleBounce();
            DoubleBounce.setColor(color);
            view.setIndeterminateDrawable(DoubleBounce);
        } else if (style.equalsIgnoreCase("FadingCircle")) {
            FadingCircle FadingCircle = new FadingCircle();
            FadingCircle.setColor(color);
            view.setIndeterminateDrawable(FadingCircle);
        } else if (style.equalsIgnoreCase("FoldingCube")) {
            FoldingCube FoldingCube = new FoldingCube();
            FoldingCube.setColor(color);
            view.setIndeterminateDrawable(FoldingCube);
        } else if (style.equalsIgnoreCase("Pulse")) {
            DoubleBounce Pulse = new DoubleBounce();
            Pulse.setColor(color);
            view.setIndeterminateDrawable(Pulse);
        } else if (style.equalsIgnoreCase("RotatingCircle")) {
            RotatingCircle RotatingCircle = new RotatingCircle();
            RotatingCircle.setColor(color);
            view.setIndeterminateDrawable(RotatingCircle);
        } else if (style.equalsIgnoreCase("RotatingPlane")) {
            RotatingPlane RotatingPlane = new RotatingPlane();
            RotatingPlane.setColor(color);
            view.setIndeterminateDrawable(RotatingPlane);
        } else if (style.equalsIgnoreCase("ThreeBounce")) {
            ThreeBounce ThreeBounce = new ThreeBounce();
            ThreeBounce.setColor(color);
            view.setIndeterminateDrawable(ThreeBounce);
        } else if (style.equalsIgnoreCase("WanderingCubes")) {
            WanderingCubes WanderingCubes = new WanderingCubes();
            WanderingCubes.setColor(color);
            view.setIndeterminateDrawable(WanderingCubes);
        } else if (style.equalsIgnoreCase("Wave")) {
            Wave Wave = new Wave();
            Wave.setColor(color);
            view.setIndeterminateDrawable(Wave);
        } else  {
            Log.d(LOG_TAG, "Invalid AnimationStyle used:" + style);
        }
        Log.d(LOG_TAG, "Using color of:" + color);
        view.invalidate();
    }
}
