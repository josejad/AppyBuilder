package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class MockGalleryViewer extends MockVisibleComponent {

    private final Image largeImage = new Image(images.galleryviewerbig());

    /**
     * Component type name.
     */
    public static final String TYPE = "GalleryViewer";

    /**
     * Creates a new MockGalleryViewer component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockGalleryViewer(SimpleEditor editor) {
        super(editor, TYPE, images.galleryviewer());

        // Initialize mock GalleryViewer UI
        SimplePanel myWidget = new SimplePanel();
        myWidget.setStylePrimaryName("ode-SimpleMockContainer");
        myWidget.addStyleDependentName("centerContents");
        myWidget.setWidget(largeImage);
        initComponent(myWidget);
    }

//    @Override
//    protected boolean isPropertyVisible(String propertyName) {
//        //We don't want to allow user to change the galleryViewer height. S/he can only change the
//        //galleryViewer width
//        if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
//            return false;
//        }
//        return super.isPropertyVisible(propertyName);
//    }

    @Override
    public int getPreferredWidth() {
        return largeImage.getHeight();
    }

    //overwrite and make the length to fill parent
    @Override
    int getWidthHint() {
        int widthHint = super.getWidthHint();
        if (widthHint == LENGTH_PREFERRED) {
            widthHint = LENGTH_FILL_PARENT;
        }
        return widthHint;
    }
//
//    @Override int getHeightHint() {
//        int heightHint = super.getHeightHint();
//        if (heightHint == LENGTH_PREFERRED) {
//            heightHint = LENGTH_FILL_PARENT;
//        }
//        return heightHint;
//    }


}
