// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@DesignerComponent(version = YaVersion.TEMPERATURESENSOR_COMPONENT_VERSION,
        description = "<p>Non-visible component that measures the temperature of the device in degrees Celsius. " +
                "This sensor implementation varies across devices. </p> ",
        category = ComponentCategory.SENSORS,
        nonVisible = true,
        iconName = "images/temperaturesensor.png")
@SimpleObject
public class TemperatureSensor extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {

    private Sensor temperatureSensor;

    private final SensorManager sensorManager;

    // Indicates whether the sensor should generate events
    private boolean enabled;
    private float temperature;

    // Cache for shake detection
    private static final int SENSOR_CACHE_SIZE = 10;
    private final Queue<Float> TEMPERATURE_CACHE = new LinkedList<Float>();
    private int accuracy;

    /**
     * Creates a new TemperatureSensor component.
     *
     * @param container  ignored (because this is a non-visible component)
     */
    public TemperatureSensor(ComponentContainer container) {
        super(container.$form());
        form.registerForOnResume(this);
        form.registerForOnStop(this);

        enabled = true;
        sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
        temperatureSensor = getTemperatureSensor();
        startListening();
    }

    private Sensor getTemperatureSensor() {
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sensor != null) {
            return sensor;
        } else {
            return sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        }


    }
    /**
     * Available property getter method (read-only property).
     *
     * @return {@code true} indicates that an temperature sensor is available,
     *         {@code false} that it isn't
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Available() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sensors.size() > 0) {
            return true;
        }

        //Now try the TYPE_TEMPERATURE
        sensors = sensorManager.getSensorList(Sensor.TYPE_TEMPERATURE);
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
        sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Assumes that sensorManager has been initialized, which happens in constructor
    private void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (enabled) {
            final float[] values = sensorEvent.values.clone();
            temperature = values[0];
            accuracy = sensorEvent.accuracy;
            TemperatureChanged(temperature);
        }
    }

    /**
     *
     * @return Sensor's maximum range.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float MaximumRange() {
        return temperatureSensor.getMaximumRange();
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

    @SimpleEvent
    public void TemperatureChanged(float temperature) {
        this.temperature = temperature;

        addToSensorCache(TEMPERATURE_CACHE, this.temperature);

        long currentTime = System.currentTimeMillis();

        EventDispatcher.dispatchEvent(this, "TemperatureChanged", this.temperature);
    }

    /**
     * Returns the temperature.
     * The sensor must be enabled to return meaningful values.
     *
     * @return  temperature
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float Temperature() {
        return temperature;
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
