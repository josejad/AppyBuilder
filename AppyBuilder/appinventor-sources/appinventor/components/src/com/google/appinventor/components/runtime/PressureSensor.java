package com.google.appinventor.components.runtime;

//hossein: check following to implement pressure sensor
//   http://developer.samsung.com/android/technical-docs/Developing-Android-Application-Using-Atmospheric-Pressure-Sensor




import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@DesignerComponent(version = YaVersion.PRESSURESENSOR_COMPONENT_VERSION,
        description = "<p>Non-visible component that measures the ambient air pressure in hPa or mbar.</p> ",
        category = ComponentCategory.SENSORS,
        nonVisible = true,
        iconName = "images/pressuresensor.png")
@SimpleObject
public class PressureSensor extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable, OnPauseListener {

    private Sensor pressureSensor;

    private final SensorManager sensorManager;

    // Indicates whether the sensor should generate events
    private boolean enabled;
    private float pressure=0f;
    private float altitude=0f;

    // Indicates if the sensor should be running when screen is off (on pause)
    private boolean keepRunningWhenOnPause;

    // Cache for shake detection
    private static final int SENSOR_CACHE_SIZE = 10;
    private final Queue<Float> PRESSURE_CACHE = new LinkedList<Float>();
    private int accuracy;

    /**
     * Creates a new PressureSensor component.
     *
     * @param container  ignored (because this is a non-visible component)
     */
    public PressureSensor(ComponentContainer container) {
        super(container.$form());
        form.registerForOnResume(this);
        form.registerForOnStop(this);
        form.registerForOnPause(this);

        enabled = true;
        sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        startListening();
    }

    /**
     * Available property getter method (read-only property).
     *
     * @return {@code true} indicates that an pressure sensor is available,
     *         {@code false} that it isn't
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Available() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_PRESSURE);
        return (sensors.size() > 0);
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

    @Override
    public void onPause() {
        if (enabled && !keepRunningWhenOnPause) {
            stopListening();
        }
    }


    // Assumes that sensorManager has been initialized, which happens in constructor
    private void startListening() {
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Assumes that sensorManager has been initialized, which happens in constructor
    private void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (enabled) {
            final float[] values = sensorEvent.values.clone();
            //pressure in millibar unit
            pressure = values[0];
            altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);
            accuracy = sensorEvent.accuracy;
            PressureChanged(pressure, altitude);
        }
    }

    /**
     * todo: add comment here. Do we need this method at all?? This was taken from proximity sensor
     *
     * @return Sensor's maximum range.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float MaximumRange() {
        return pressureSensor.getMaximumRange();
    }

    /**
     * If true, the sensor will generate events.  Otherwise, no events
     * are generated.
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
     * Returns value of keepRunningWhenOnPause
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean KeepRunningWhenOnPause() {
        return keepRunningWhenOnPause;
    }

    /**
     * Specifies if sensor should still be listening when activity is not active
     *
     * @param enabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void KeepRunningWhenOnPause(boolean enabled) {

        this.keepRunningWhenOnPause = enabled;
    }

    /**
     * Returns the current atmospheric pressure
     * @return the pressure in millibar
     */
    @SimpleProperty
    public float Pressure() {
        return pressure;
    }

    /**
     * Returns the altitude in meters from the atmospheric pressure and the pressure at sea level
     * @return altitude
     */
    @SimpleProperty
    public float Altitude() {
        return altitude;
    }


    /**
     * Pressure (millibar) and altitude
     * @param pressure pressure in millibar unit
     * @param altitude altitude in meter
     */
    @SimpleEvent
    public void PressureChanged(float pressure, float altitude) {
        this.pressure = pressure;

        addToSensorCache(PRESSURE_CACHE, this.pressure);

        long currentTime = System.currentTimeMillis();

        EventDispatcher.dispatchEvent(this, "PressureChanged", pressure, altitude);
    }

    /**
     * Returns the pressure.
     * The sensor must be enabled to return meaningful values.
     *
     * @return  pressure
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float Distance() {
        return pressure;
    }

    /*
 * Updating sensor cache, replacing oldest values.
 */
    private void addToSensorCache(Queue<Float> cache, float value) {
        if (cache.size() >= SENSOR_CACHE_SIZE) {
            cache.remove();
        }
        cache.add(value);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
