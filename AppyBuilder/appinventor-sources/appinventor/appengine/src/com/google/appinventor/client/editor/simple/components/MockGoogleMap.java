package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

import static com.google.appinventor.client.Ode.MESSAGES;

public class MockGoogleMap extends MockVisibleComponent {
    /**
     * Component type name.
     */
    public static final String TYPE = "GoogleMap";

    // Large icon image for use in designer. Smaller version is in the palette.
    private final Image largeImage = new Image(images.googleMapBig());
    private static boolean warningGiven = false; // Whether or not we have given experimental warning


    public MockGoogleMap(SimpleEditor editor){
        super(editor, TYPE, images.googlemap());
        // Initialize mock Survey UI
        SimplePanel gmapWidget = new SimplePanel();
        gmapWidget.setStylePrimaryName("ode-SimpleMockContainer");
        gmapWidget.addStyleDependentName("centerContents");
        gmapWidget.setWidget(largeImage);
        initComponent(gmapWidget);

    }

    @Override
    public int getPreferredWidth() {
        return largeImage.getWidth();
    }

    @Override
    public int getPreferredHeight() {
        return largeImage.getHeight();
    }

    /**
     * Called when the component is dropped in the Designer window
     * we give a warning that GoogleMap is still experimental.
     */

    @Override
    public void onCreateFromPalette() {
        if (!warningGiven) {
            warningGiven = true;
            Ode.getInstance().warningDialog(MESSAGES.warningDialogTitle(),
                    MESSAGES.googlemapWarning(), MESSAGES.okButton());
        }
    }
    // override the width and height hints, so that automatic will in fact be
    // fill-parent
    @Override
    int getWidthHint() {
        int widthHint = super.getWidthHint();
        if (widthHint == LENGTH_PREFERRED) {
            widthHint = LENGTH_FILL_PARENT;
        }
        return widthHint;
    }

    @Override
    int getHeightHint() {
        int heightHint = super.getHeightHint();
        if (heightHint == LENGTH_PREFERRED) {
            heightHint = LENGTH_FILL_PARENT;
        }
        return heightHint;
    }
}