//package com.google.appinventor.components.runtime;
//
//import android.content.res.ColorStateList;
//import android.view.View;
//import com.google.appinventor.components.annotations.*;
//import com.google.appinventor.components.common.ComponentCategory;
//import com.google.appinventor.components.common.PropertyTypeConstants;
//import com.google.appinventor.components.common.YaVersion;
//
//@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
//        description =
//                "Join AppyBuilder Community at <a href=\"http://Community.AppyBuilder.com\" target=\"ab\">http://Community.AppyBuilder.com</a> <p>" +
//                "RadioButton is a component that allows you to create RadioButtons " ,
//        category = ComponentCategory.USERINTERFACE)
//@SimpleObject
//public class RadioButton2 extends AndroidViewComponent {
//
//    private ComponentContainer container;
//    private final android.widget.RadioButton view;
//
//    public RadioButton2(ComponentContainer container) {
//        super(container);
//        this.container = container;
//        view = new android.widget.RadioButton(container.$context());
//        view.setTextColor(COLOR_BLACK);
//        view.setButtonTintList(ColorStateList.valueOf(-8205116));
//
//        container.$add(this);
//    }
//
//    @Override
//    public View getView() {
//        return view;
//    }
//
//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
//    @SimpleProperty
//    public void Text(String text) {
//        view.setText(text);
//    }
//
//    @SimpleProperty
//    public String Text() {
//        return view.getText().toString();
//    }
//
//
//
//}
//
//
//
