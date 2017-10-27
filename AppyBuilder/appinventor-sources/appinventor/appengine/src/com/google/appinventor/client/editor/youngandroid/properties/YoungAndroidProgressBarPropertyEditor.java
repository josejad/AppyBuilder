package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for elements sort options in list picker
 *
 * @author kashi01@gmail.com (M. Hossein Amerkashi)
 */
public class YoungAndroidProgressBarPropertyEditor extends ChoicePropertyEditor {

  // No sort order
  public static final String ChasingDots = "ChasingDots";
  public static final String Circle = "Circle";
  public static final String CubeGrid = "CubeGrid";
  public static final String DoubleBounce = "DoubleBounce";
  public static final String FadingCircle = "FadingCircle";
  public static final String FoldingCube = "FoldingCube";
  public static final String Pulse = "Pulse";
  public static final String RotatingCircle = "RotatingCircle";
  public static final String RotatingPlane = "RotatingPlane";
  public static final String ThreeBounce = "ThreeBounce";
  public static final String WanderingCubes = "WanderingCubes";
  public static final String Wave = "Wave";

  private static final Choice[] sortOptions = new Choice[] {
          new Choice("Wave", Wave),
          new Choice("ChasingDots", ChasingDots),
          new Choice("Circle", Circle),
          new Choice("DoubleBounce", DoubleBounce),
          new Choice("FadingCircle", FadingCircle),
          new Choice("FoldingCube", FoldingCube),
          new Choice("Pulse", Pulse),
          new Choice("RotatingCircle", RotatingCircle),
          new Choice("RotatingPlane", RotatingPlane),
          new Choice("ThreeBounce", ThreeBounce),
          new Choice("WanderingCubes", WanderingCubes),
  };


  public YoungAndroidProgressBarPropertyEditor() {
    super(sortOptions);
  }
}

