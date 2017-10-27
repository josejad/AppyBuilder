package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

//import com.google.gwt.user.client.ui.ToggleButton;
// http://www.gwtproject.org/javadoc/latest/index.html?com/google/gwt/user/client/ui/package-summary.html

/**
 * Mock Toggler component.
 */
public class MockToggler extends MockVisibleComponent {

    private final Image largeImage = new Image(images.togglerbig());

    /**
     * Component type name.
     */
    public static final String TYPE = "Toggler";

    /**
     * Creates a new Mock component.
     * @param editor  editor of source file the component belongs to
     */
    public MockToggler(SimpleEditor editor) {
        super(editor, TYPE, images.toggler());

        // Initialize mock  UI
        SimplePanel widget = new SimplePanel();
        widget.setStylePrimaryName("ode-SimpleMockContainer");
        widget.addStyleDependentName("centerContents");
        widget.setWidget(largeImage);
        initComponent(widget);
    }

    @Override
    public int getPreferredWidth() {
        return largeImage.getWidth();
    }

    // override the width and height hints, so that automatic will in fact be fill-parent
//    @Override
//    int getWidthHint() {
//        int widthHint = super.getWidthHint();
//        if (widthHint == LENGTH_PREFERRED) {
//            widthHint = LENGTH_FILL_PARENT;
//        }
//        return widthHint;
//    }

//    @Override
//    int getHeightHint() {
//        int heightHint = super.getHeightHint();
//        if (heightHint == LENGTH_PREFERRED) {
//            heightHint = LENGTH_FILL_PARENT;
//        }
//        return heightHint;
//    }
    @Override
    protected boolean isPropertyVisible(String propertyName) {

//        if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
        if (propertyName.equals("Height")) {
            return false;
        }
        return super.isPropertyVisible(propertyName);
    }


}
