package com.google.appinventor.components.runtime;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.MAGNETIC_FIELD_COMPONENT_VERSION,
        description = "<p>Non-visible component that measures the ambient geomagnetic field for all " +
                "three physical axes (x, y, z) in Tesla https://en.wikipedia.org/wiki/Tesla_(unit). </p> ",
        category = ComponentCategory.SENSORS,
        nonVisible = true,
        iconName = "images/magneticsensor.png")
@SimpleObject
public class MagneticFieldSensor extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, OnPauseListener, SensorComponent, SensorEventListener, Deleteable {

    private Sensor magneticSensor;

    private final SensorManager sensorManager;

    // Indicates whether the sensor should generate events
    private boolean enabled;

    // Cache for shake detection
    private float xStrength;
    private float yStrength;
    private float zStrength;
    private boolean listening;
    private double absoluteStrength;

    /**
     * Creates a new MagneticSensor component.
     *
     * @param container  ignored (because this is a non-visible component)
     */
    public MagneticFieldSensor(ComponentContainer container) {
        super(container.$form());
        form.registerForOnResume(this);
        form.registerForOnStop(this);
        form.registerForOnPause(this);

        enabled = true;
        sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        startListening();
    }

    private Sensor getMagneticSensor() {
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor != null) {
            return sensor;
        } else {
            return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }


    }
    /**
     * Available property getter method (read-only property).
     *
     * @return {@code true} indicates that an magnetic sensor is available,
     *         {@code false} that it isn't
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Available() {
        return sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size() > 0;
    }

    // OnResumeListener implementation

    @Override
    public void onResume() {
        if (enabled) {
            startListening();
        }
    }

    // OnStopListener implementation

    @Override
    public void onStop() {
        if (enabled) {
            stopListening();
        }
    }

    // Deleteable implementation

    @Override
    public void onDelete() {
        if (enabled) {
            stopListening();
        }
    }

    // OnPauseListener implementation

    public void onPause() {
        stopListening();
    }


    // Assumes that sensorManager has been initialized, which happens in constructor
    private void startListening() {
        if (!listening) {
            sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
            listening = true;
        }
    }

    // Assumes that sensorManager has been initialized, which happens in constructor
    private void stopListening() {
        if (listening) {
            sensorManager.unregisterListener(this);
            listening = false;

            // Throw out sensor information that will go stale.
            xStrength = 0;
            yStrength = 0;
            zStrength = 0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (enabled && sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            final float[] values = sensorEvent.values.clone();
            xStrength = (float) Math.toDegrees(sensorEvent.values[0]);
            yStrength = (float) Math.toDegrees(sensorEvent.values[1]);
            zStrength = (float) Math.toDegrees(sensorEvent.values[2]);
            absoluteStrength = Math.sqrt(xStrength * this.xStrength + yStrength * yStrength + zStrength * zStrength);
            MagneticChanged(xStrength, yStrength, zStrength, absoluteStrength);
        }
    }

    /**
     *
     * @return Sensor's maximum range.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float MaximumRange() {
        return magneticSensor.getMaximumRange();
    }

    /**
     * If true, the sensor will generate events.  Otherwise, no events
     * are generated .
     *
     * @return {@code true} indicates that the sensor generates events,
     *         {@code false} that it doesn't
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Enabled() {
        return enabled;
    }

    /**
     * Specifies whether the sensor should generate events.  If true,
     * the sensor will generate events.  Otherwise, no events are
     * generated.
     *
     * @param enabled  {@code true} enables sensor event generation,
     *                 {@code false} disables it
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty
    public void Enabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        if (enabled) {
            startListening();
        } else {
            stopListening();
        }
    }

    /**
     * MagneticChanged event handler.
     */
    @SimpleEvent(description = "Indicates that the magnetic sensor data has changed. ")
    public void MagneticChanged(float xStrength, float yStrength, float zStrength, double absoluteStrength) {
        EventDispatcher.dispatchEvent(this, "MagneticChanged", xStrength, yStrength, zStrength, absoluteStrength);
    }


    /**
     * Returns the X (northward) component of the magnetic field in nanoteslas.
     * The sensor must be enabled to return meaningful values.
     *
     * @return  magnetic
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public double AbsoluteStrength() {
        return absoluteStrength;
    }

    /**
     * Returns the X (northward) component of the magnetic field in nanoteslas.
     * The sensor must be enabled to return meaningful values.
     *
     * @return  magnetic
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float Xstrength() {
        return xStrength;
    }

    /**
     * Returns the Y (eastward) component of the magnetic field in nanoteslas.
     * The sensor must be enabled to return meaningful values.
     *
     * @return  magnetic
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float Ystrength() {
        return yStrength;
    }

    /**
     * Returns the Z (downward) component of the magnetic field in nanoteslas.
     * The sensor must be enabled to return meaningful values.
     *
     * @return  magnetic
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float Zstrength() {
        return zStrength;
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
