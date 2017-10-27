package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for gender type
 *
 * @author kashi01@gmail.com (M. Hossein Amerkashi)
 */
public class YoungAndroidGenderPropertyEditor extends ChoicePropertyEditor {

  public static final String ALL = ComponentConstants.GENDER_ALL;
  public static final String FEMALE = ComponentConstants.GENDER_FEMALE;
  public static final String MALE = ComponentConstants.GENDER_MALE;

  private static final Choice[] genderOptions = new Choice[] {
      new Choice(MESSAGES.genderAll(), ALL),
      new Choice(MESSAGES.genderFemale(), FEMALE),
      new Choice(MESSAGES.genderMale(), MALE)
  };


  public YoungAndroidGenderPropertyEditor() {
    super(genderOptions);
  }
}

