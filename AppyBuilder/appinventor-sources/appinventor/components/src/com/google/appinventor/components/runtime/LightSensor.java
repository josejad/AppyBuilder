package com.google.appinventor.components.runtime;

//hossein: check following to implement light sensor
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@DesignerComponent(version = YaVersion.LIGHTSENSOR_COMPONENT_VERSION,
        description = "<p>Non-visible component that measures the ambient light level (illumination) in lx</p> ",
        category = ComponentCategory.SENSORS,
        nonVisible = true,
        iconName = "images/lightsensor.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.hardware.sensor.light")
public class LightSensor extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {

    private Sensor lightSensor;

    private final SensorManager sensorManager;

    // Indicates whether the sensor should generate events
    private boolean enabled;
    private float light =0f;

    // Cache for shake detection
    private static final int SENSOR_CACHE_SIZE = 10;
    private final Queue<Float> LUX_CACHE = new LinkedList<Float>();
    private int accuracy;

    /**
     * Creates a new LightSensor component.
     *
     * @param container  ignored (because this is a non-visible component)
     */
    public LightSensor(ComponentContainer container) {
        super(container.$form());
        form.registerForOnResume(this);
        form.registerForOnStop(this);

        enabled = true;
        sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        startListening();
    }

    /**
     * Available property getter method (read-only property).
     *
     * @return {@code true} indicates that a light sensor is available,
     *         {@code false} that it isn't
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Available() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
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

    // Assumes that sensorManager has been initialized, which happens in constructor
    private void startListening() {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Assumes that sensorManager has been initialized, which happens in constructor
    private void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (enabled) {
            final float[] values = sensorEvent.values.clone();
            light = values[0];
            accuracy = sensorEvent.accuracy;
            LightChanged(light);
        }
    }

    /**
     * todo: add comment here. Do we need this method at all?? This was taken from proximity sensor
     *
     * @return Sensor's maximum range.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float MaximumRange() {
        return lightSensor.getMaximumRange();
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
     * Light (lux)
     * @param light light in lux unit
     */
    @SimpleEvent
    public void LightChanged(float light) {
        this.light = light;

        addToSensorCache(LUX_CACHE, this.light);

//        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        EventDispatcher.dispatchEvent(this, "LightChanged", light);
    }


    /**
     * Returns the light.
     * The sensor must be enabled to return meaningful values.
     *
     * @return value of light in light unit
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float Light() {
        return light;
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
