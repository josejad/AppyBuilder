package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class MockXYChart extends MockVisibleComponent {

    private final Image largeImage = new Image(images.xychartbig());

    /**
     * Component type name.
     */
    public static final String TYPE = "XYChart";

    /**
     * Creates a new Mock component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockXYChart(SimpleEditor editor) {
        super(editor, TYPE, images.xychart());

        // Initialize mock UI
        SimplePanel myWidget = new SimplePanel();
        myWidget.setStylePrimaryName("ode-SimpleMockContainer");
        myWidget.addStyleDependentName("centerContents");
        myWidget.setWidget(largeImage);
        initComponent(myWidget);
    }

//    @Override
//    protected boolean isPropertyVisible(String propertyName) {
//        //We don't want to allow user to change the height. S/he can only change the
//        //width
//        if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
//            return false;
//        }
//        return super.isPropertyVisible(propertyName);
//    }

    @Override
    public int getPreferredWidth() {
        return largeImage.getHeight();
    }

    // override the width and height hints, so that automatic will in fact be fill-parent
    @Override
    int getWidthHint() {
        int widthHint = super.getWidthHint();
        if (widthHint == LENGTH_PREFERRED) {
            widthHint = LENGTH_FILL_PARENT;
        }
        return widthHint;
    }

    @Override int getHeightHint() {
        int heightHint = super.getHeightHint();
        if (heightHint == LENGTH_PREFERRED) {
            heightHint = LENGTH_FILL_PARENT;
        }
        return heightHint;
    }


}
