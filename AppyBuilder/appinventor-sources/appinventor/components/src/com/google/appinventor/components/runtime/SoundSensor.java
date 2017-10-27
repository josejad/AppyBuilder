package com.google.appinventor.components.runtime;
// https://code.google.com/p/android-labs/source/browse/trunk/NoiseAlert/src/com/google/android/noisealert/SoundMeter.java
import android.media.MediaRecorder;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.TimerInternal;

@DesignerComponent(category= ComponentCategory.SENSORS,
        description="<p>Physical world component that can detect such data as: " +
                "sound amplitude (measurement of the degree of change [positive or negative] ).</p>",
        iconName="images/soundsensor.png", nonVisible=true, version=1)
@SimpleObject
@UsesPermissions(permissionNames="android.permission.RECORD_AUDIO")
public class SoundSensor extends AndroidNonvisibleComponent
        implements AlarmHandler, OnStopListener, OnResumeListener, Deleteable
{
    private MediaRecorder mRecorder = null;
    private double maxSoundLevel = 100d;

    // continuously record the sound level at intervals of 100 times per second
    private TimerInternal timerInternal = new TimerInternal(this, false, 100);
    private String TAG ="SoundSensor";
    private boolean enabled=false;
    private double soundLevel =0d;
    private static final int MAX_AMPLITUTE = 32768;

    public SoundSensor(ComponentContainer container)
    {
        super(container.$form());
    }

    @SimpleProperty
    public double MaxSoundlevel()
    {
        return this.maxSoundLevel;
    }

    @DesignerProperty(editorType= PropertyTypeConstants.PROPERTY_TYPE_INTEGER,defaultValue = "100")
    @SimpleProperty
    public void MaxSoundlevel(int value)
    {
        this.maxSoundLevel = value;
    }

    @SimpleEvent(description = "Triggered when the sound level has changed")
    public void SoundChanged(double value)
    {
//        Object[] arrayOfObject = new Object[1];
//        arrayOfObject[0] = value;
        EventDispatcher.dispatchEvent(this, "SoundChanged", value);
    }


    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Starts or Stops listening to sound changes")
    public void Listen(boolean enabled)
    {
        this.enabled = enabled;
        if (!enabled) {
            stopListening();
            return;
        }

        //should start listening
        if (this.mRecorder == null)
        {
            this.mRecorder = new MediaRecorder();
            this.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            this.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //For the analysis of sound without saving all you need is use mRecorder.setOutputFile("/dev/null")
            this.mRecorder.setOutputFile("/dev/null");
        }
        try
        {
            this.mRecorder.prepare();
            this.mRecorder.start();
            this.timerInternal.Enabled(true);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error with sound. Error is:" + e.getMessage());
        }
    }

    /**
     * Returns a boolean indicating if the sensor is listening or not
     *
     * @return {@code true} indicates that the sensor generates events,
     *         {@code false} that it doesn't
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description="Returns true if listening to sound changes, else false")
    public boolean Listen() {
        return enabled;
    }
    /**
     * Stops the sound listener
     */
    public void stopListening()
    {
        if (this.mRecorder != null)
        {
            this.timerInternal.Enabled(false);
            this.mRecorder.stop();
            this.mRecorder.release();
            this.mRecorder = null;
        }
    }

    /**
     * SoundLevel property getter method (read-only property).
     *
     * <p>To return meaningful values the sensor must be enabled.</p>
     *
     * @return  current SoundLevel
     */

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public double SoundLevel() {
        return soundLevel;
    }

    public void alarm()
    {
        if (this.mRecorder != null)  {
            //Convert the amplitude to user requested maxSoundLevel.
            soundLevel = Amplitude() * (this.maxSoundLevel / MAX_AMPLITUTE);
            SoundChanged(soundLevel);
        }
    }

    /**
     * Returns the real sound amplitude which can be between 0 to 32768
     * @return real sound amplitude which can be between 0 to 32768
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns the real sound amplitude which can be between 0 to 32768")
    public double Amplitude() {
        if (mRecorder != null) {
            return  mRecorder.getMaxAmplitude();
        }

        return 0;
    }

    /**
     * Returns the decibel by converting Amplitude to Decibel
     */
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public double Decibel() {
        return 20 * Math.log10(Amplitude()); // / MAX_AMPLITUTE);
    }

    @Override
    public void onResume() {
        //resume with whatever the last value of enabled was.
        // i.e. if user had enabled the sensor, it will restart, else won't
        Listen(enabled);
    }

    @Override
    public void onStop() {
        if (enabled) {
            stopListening();
        }
    }

    // Deletable implementation

    @Override
    public void onDelete() {
        if (enabled) {
            stopListening();
        }
    }
}