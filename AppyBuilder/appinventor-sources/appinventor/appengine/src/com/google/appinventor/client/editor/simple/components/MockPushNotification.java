// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock for the non-visible FirebaseDB component. This needs a separate mock
 * from other non-visible components so that some of its properties can be
 * given dynamic default values.
 *
 * @author will2596@gmail.com (William Byrne)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class MockPushNotification extends MockNonVisibleComponent {

    public static final String TYPE = "PushNotification";
    private static final String PROPERTY_NAME_DEVELOPER_BUCKET = "DeveloperBucket";
    private static final String PROPERTY_NAME_PROJECT_BUCKET = "TopicPath";
    private static final String PROPERTY_NAME_FIREBASE_TOKEN = "FirebaseToken";
    private static final String PROPERTY_NAME_FIREBASE_URL = "FirebaseURL";
    private static final String PROPERTY_NAME_DEFAULT_URL = "DefaultURL";
    private static boolean warningGiven = false; // Whether or not we have given experimental warning
    private static final String PROPERTY_NAME_VCODE = "VersionCode";

    private boolean persistToken = false;
    private String vCode="";
	private String userEmail="";
    private String devBucket="_topic_";
    private String projectName = "";

    /**
     * Creates a new instance of a non-visible component whose icon is
     * loaded dynamically (not part of the icon image bundle)
     *
     * @param editor
     * @param type
     * @param iconImage
     */
    public MockPushNotification(SimpleEditor editor, String type, Image iconImage) {
        super(editor, type, iconImage);
    }

    /**
     * Initializes the "ProjectBucket", "DeveloperBucket", "FirebaseToken"
     * properties dynamically.
     *
     * @param widget the iconImage for the MockFirebaseDB
     */
    @Override
    public final void initComponent(Widget widget) {
        super.initComponent(widget);

        // Firebase paths must not contain '.', '#', '$', '[', or ']'
        userEmail = Ode.getInstance().getUser().getUserEmail().replace(".", ":") + "";

		userEmail = Ode.getInstance().getUser().getUserEmail();
		int idx = userEmail.indexOf("@");
		userEmail = userEmail.substring(0,idx);
		
        // Since I'm allowing user to do his own notification, I'm just resetting the devBucket to ""
        devBucket="_topic_";
        DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
        projectName = "";
        if (currentProject != null) {
            projectName = currentProject.name;
        }

        // We add the version code to make it unique
//        projectName += vCode;

//        super.changeProperty(PROPERTY_NAME_DEVELOPER_BUCKET, devBucket + "/");
//        OdeLog.log("devBucket,userEmail,projectName:" +devBucket+"/"+userEmail+"/"+projectName);
        changeProperty(PROPERTY_NAME_PROJECT_BUCKET, devBucket + "/" + userEmail + "/" + projectName);
//        MockComponentsUtil.setEnabled(this, "false");
    }

    /**
     * Called when the component is dropped in the Designer window
     * we give a warning that firebase is still experimental.
     */
    @Override
    public void onCreateFromPalette() {
        if (!warningGiven) {
            warningGiven = true;
            Ode.getInstance().warningDialog(MESSAGES.warningDialogTitle(),
                    MESSAGES.pushNotificationExperimentalWarning(), MESSAGES.okButton());
        }
    }

/*@Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_VCODE)) {
        OdeLog.log("devBucket,userEmail,projectName:" +devBucket+"/"+userEmail+"/"+projectName);
        changeProperty(PROPERTY_NAME_PROJECT_BUCKET, devBucket + "/" + userEmail + "/" + projectName + "/" + newValue);
    } 
  }*/

}