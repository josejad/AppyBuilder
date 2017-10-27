package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class MockRatingBar extends MockVisibleComponent {

    private final Image largeImage = new Image(images.ratingbarbig());

    /**
     * Component type name.
     */
    public static final String TYPE = "RatingBar";

    /**
     * Creates a new Mock component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockRatingBar(SimpleEditor editor) {
        super(editor, TYPE, images.ratingbar());

        // Initialize mock UI
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
    @Override
    int getWidthHint() {
        int widthHint = super.getWidthHint();
        if (widthHint == LENGTH_PREFERRED) {
            widthHint = LENGTH_FILL_PARENT;
        }
        return widthHint;
    }

}
