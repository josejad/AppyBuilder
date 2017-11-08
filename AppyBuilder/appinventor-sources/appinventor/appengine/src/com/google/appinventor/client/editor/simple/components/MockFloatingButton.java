// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.ui.Image;

public class MockFloatingButton extends MockNonVisibleComponent {

//    private final Image largeImage = new Image(images.admobbig());

    /**
     * Component type name.
     */
    public static final String TYPE = "FloatingButton";

    /**
     * Creates a new Mock component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockFloatingButton(SimpleEditor editor) {
        super(editor, TYPE, new Image(images.floatingButton()));
        OdeLog.log("MockFloatingButton " + editor.getStyleName() + "/" + editor.getFileId());

//        SimplePanel widget = new SimplePanel();
//        widget.setStylePrimaryName("ode-SimpleMockContainer");
//        widget.addStyleDependentName("centerContents");
//        initComponent(widget);
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue){
        super.onPropertyChange(propertyName, newValue);
//        OdeLog.log("MockFloatingButton property changed. property:" +
//                propertyName+ ", value: " + newValue);

        // we need to refresh by removing this widget and then refreshing again
//        removeFromParent();
//        ((HasWidgets)getWidget().getParent()).remove(getWidget()); // doesn't work throws class cast exception
//        getWidget().removeFromParent();  // doesn't work and throws exception

//        initComponent(new Image(images.floatingButton()));
        // Force to refresh/repaint the form
        refreshForm();

    }

}
