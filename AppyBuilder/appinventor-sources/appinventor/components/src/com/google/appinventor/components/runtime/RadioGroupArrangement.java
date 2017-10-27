//
//package com.google.appinventor.components.runtime;
//
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import com.google.appinventor.components.annotations.*;
//import com.google.appinventor.components.common.ComponentCategory;
//import com.google.appinventor.components.common.ComponentConstants;
//import com.google.appinventor.components.common.PropertyTypeConstants;
//import com.google.appinventor.components.common.YaVersion;
//
//import java.security.acl.Group;
//
///**
// * A container for components that arranges them linearly, either
// * horizontally or vertically.
// *
// * @author sharon@google.com (Sharon Perl)
// * @author kkashi01@gmail.com (Hossein Amerkashi) (added Image and BackgroundColors)
// */
//
//@DesignerComponent(version = YaVersion.HORIZONTALARRANGEMENT_COMPONENT_VERSION,
//        description = "<p>A formatting element in which to place components " +
//                "that should be displayed from left to right.  If you wish to have " +
//                "components displayed one over another, use " +
//                "<code>VerticalArrangement</code> instead.</p>",
//        category = ComponentCategory.LAYOUT)
//@SimpleObject
//public class RadioGroupArrangement extends HVArrangement implements RadioGroup.OnCheckedChangeListener  {
//
//  private RadioGroup radioGroup;
//  private int groupID=10;
//  private int groupOrientation=1;
//
//  /**
//   * Creates a new HVArrangement component.
//   *
//   * @param container   container, component will be placed in
//   */
//
//  public RadioGroupArrangement(ComponentContainer container) {
//    super(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
//            ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
//
//    Log.d("RadioGroup", "initializing radio group arrangement");
//
//    radioGroup = new android.widget.RadioGroup(container.$context());
//    radioGroup.setOnCheckedChangeListener(this);
//    GroupID(groupID);
//  }
//
//
//
//  public RadioGroupArrangement(ComponentContainer container, int orientation) {
//    super(container, orientation, false);
//
//    radioGroup = new android.widget.RadioGroup(container.$context());
//
//    radioGroup.setOnCheckedChangeListener(this);
//
////    android.widget.RadioButton rb = new android.widget.RadioButton(container.$context());
////    rb.setText("foo aloo");
////    radioGroup.addView(rb);
//  }
//
//
//  /**
//   * Sets the RadioGroup ID
//   */
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "10")
//  @SimpleProperty(description = "Set a UNIQUE integer id for group of RadioButtons")
//  public void GroupID(int groupID) {
//    this.groupID=groupID;
//    Log.d("RadioGroup", "started GroupID:" + groupID );
//    radioGroup.setId(groupID);
//
//    if (true) return;
//    if (viewLayout == null) return;
//
////    radioGroup = new android.widget.RadioGroup(container.$context());
//    final int childCount = viewLayout.getLayoutManager().getChildCount();
//    Log.d("RadioGroup", "viewlayout  of children:" + childCount);
//    Log.d("RadioGroup", "framelayout number of children:" + frameContainer.getChildCount());
//    for (int i = 0; i < childCount; i++) {
//      View v = viewLayout.getLayoutManager().getChildAt(i);
//
//      Log.d("RadioGroup", "got view of instance of:" + v + " / its parent is:" + v.getParent());
//
//      try {
//
//        frameContainer.addView(v);
//
//        ((ViewGroup) v.getParent()).removeView(v);
//        radioGroup.addView(v);
////        ((ViewGroup)container.getView()).addView(radioGroup, params);
//
//        Log.d("RadioGroup", "added view");
//
//      } catch (Exception e) {
//        //no-op
//        Log.d("RadioGroup", "wasn't instance of radio button. error:" + e.getMessage());
//
//      }
//
////      if (v instanceof RadioButton) {
//////        int id = v.getId();
////        radioGroup.addView(v);
////      }
//
//    }
//  }
//
//  @SimpleProperty(description = "Set a UNIQUE integer id for group of RadioButtons")
//  public int GroupID() {
//    return this.groupID;
//  }
//
// /**
//   * Sets the RadioGroup ID
//   */
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "1")
//  @SimpleProperty(description = "How the RadioButtons should be arranged. 1=Vertically, 0=Horizontally")
//  public void GroupOrientation(int orientation) {
//
//    this.groupOrientation = orientation;
//    if (orientation == 1) radioGroup.setOrientation(RadioGroup.VERTICAL);
//    else if (orientation == 0) radioGroup.setOrientation(RadioGroup.HORIZONTAL);
//    else {
//      //todo: dispatch error
//      return;
//    }
//
//    viewLayout = new LinearLayout(container.$form(), orientation,
//            ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH,
//            ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT);
//  }
//
//  @Override
//  public void onCheckedChanged(RadioGroup radioGroup, int i) {
//    Changed(i);
//  }
//
//  @SimpleEvent
//  public void Changed(int radioButtonId) {
//    EventDispatcher.dispatchEvent(this, "Changed", radioButtonId);
//  }
//}
