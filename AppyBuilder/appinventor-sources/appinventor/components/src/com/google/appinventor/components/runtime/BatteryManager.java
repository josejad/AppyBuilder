// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Battery manager
 *
 * @author M. Hossein Amerkashi
 */
@DesignerComponent(version = YaVersion.BATTERY_STATE_COMPONENT_VERSION,
        description = "The component used to query and monitor the battery and its charging status",
        category = ComponentCategory.SENSORS, nonVisible = true, iconName = "images/batterymanager.png")
@SimpleObject
public class BatteryManager extends AndroidNonvisibleComponent implements Component,
        OnResumeListener, OnStopListener, OnDestroyListener {
    String TAG = "BatteryState";
    private String chargeStatus;

    private String health = "";
    private int level = 0;
    private String plugged;
    private boolean present = false;
    private int status = 0;
    private String technology = "";
    private float temperature = 0;
    private float voltage = 0f;
    private final ComponentContainer container;
    private boolean isMonitoring;

    public BatteryManager(ComponentContainer container) {
        super(container.$form());
        this.form.registerForOnStop(this);
        this.form.registerForOnResume(this);
        this.form.registerForOnDestroy(this);
        this.container = container;
    }

    @SimpleFunction(description = "Returns a list showing battery info for the specified key. Key can be: HEALTH, LEVEL, PLUGGED, " +
            "PRESENT, STATUS, CHARGE_STATUS, TECHNOLOGY, TEMPERATURE, VOLTAGE. " +
            "If key is empty, then all better info is returned.")
    public YailList GetBatteryInfo(String key) {
        key = key.toLowerCase().trim();
        key = key.equals("") ? "all" : key;

        List<String> aList = new ArrayList<String>();
        if (key.equals("all") || key.equals("health")) aList.add("HEALTH=" + health);
        if (key.equals("all") || key.equals("level")) aList.add("LEVEL=" + level);
        if (key.equals("all") || key.equals("plugged")) aList.add("PLUGGED=" + plugged);
        if (key.equals("all") || key.equals("present")) aList.add("PRESENT=" + present);
//        if (key.equals("all") || key.equals("level")) aList.add("SCALE=" + scale);
        if (key.equals("all") || key.equals("status")) aList.add("STATUS=" + status);
        if (key.equals("all") || key.equals("charge_status")) aList.add("CHARGE_STATUS=" + chargeStatus);
        if (key.equals("all") || key.equals("technology")) aList.add("TECHNOLOGY=" + technology);
        if (key.equals("all") || key.equals("temperature")) aList.add("TEMPERATURE=" + temperature);
        if (key.equals("all") || key.equals("voltage")) aList.add("VOLTAGE=" + voltage);

        Collections.sort(aList);
        return YailList.makeList(aList);
    }

    @SimpleProperty(description = "Indicating battery temperature in Centigrade")
    public float BatteryTemperature() {
        return temperature;
    }

    @SimpleProperty(description = "Indicating battery voltage in Volts")
    public float BatteryVoltage() {
        return voltage;
    }

    @SimpleProperty(description = "Indicating whether a battery is present")
    public boolean BatteryPresent() {
        return present;
    }

    @SimpleProperty(description = "Indicating whether the device is plugged in to a power source. Can be USB, AC or UNKNOWN")
    public String BatteryPlugged() {
        return plugged;
    }

    @SimpleProperty(description = "Returns battery percentage level")
    public int BatteryLevel() {
        return level;
    }

   @SimpleProperty(description = "Returns battery health. It can be: COLD, DEAD, GOOD, OVERHEAT, OVER_VOLTAGE, UNKNOWN")
    public String BatteryHealth() {
        return health;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Starts or stops monitoring battery data. StartMonitoring only when you need to get information.")
    public void StartMonitoring(boolean shouldMonitor) {
        this.isMonitoring = shouldMonitor;
        if (isMonitoring) {
            IntentFilter batteryFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
            container.$context().registerReceiver(this.batteryInfoReceiver, batteryFilter);
        } else {
            stopMonitoring();
        }
    }


    @Override
    public void onDestroy() {
        stopMonitoring();
    }

    @Override
    public void onResume() {
        StartMonitoring(isMonitoring);
    }

    @Override
    public void onStop() {
        stopMonitoring();
    }

    private void stopMonitoring() {
        isMonitoring = false;
        if (batteryInfoReceiver != null) {
            try {
                container.$context().unregisterReceiver(this.batteryInfoReceiver);
            } catch (Exception e) {
                //no-op - just making sure we don't throw exception if receiver IS NOT registered
            }
        }
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            health = convHealth(intent.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, 0));
            int chargePlug = intent.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, 0);
            if (chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_USB) plugged = "USB";
            else if (chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_AC) plugged = "AC";
            else plugged = "UNKNOWN";

            present = intent.getExtras().getBoolean(android.os.BatteryManager.EXTRA_PRESENT);

            //Scale gives maximum battery level. We convert level to percentage
            int scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 0);

            level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 0 ) * 100 / (scale==0?1:scale);
            status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, 0);
            chargeStatus = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
            status == android.os.BatteryManager.BATTERY_STATUS_FULL ? "IS_CHARGING" : "NOT_CHARGING";
            technology = intent.getExtras().getString(android.os.BatteryManager.EXTRA_TECHNOLOGY);

            //temperature is like 356. Divide by 10 to get the decmial like 35.6 degree centigerade
            temperature = intent.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) / 10f;
            voltage = intent.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0f;

        }
    };

    private String convHealth(int health) {
        String result;
        switch (health) {
            case android.os.BatteryManager.BATTERY_HEALTH_COLD:
                result = "COLD";
                break;
            case android.os.BatteryManager.BATTERY_HEALTH_DEAD:
                result = "DEAD";
                break;
            case android.os.BatteryManager.BATTERY_HEALTH_GOOD:
                result = "GOOD";
                break;
            case android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT:
                result = "OVERHEAT";
                break;
            case android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                result = "OVER_VOLTAGE";
                break;
            case android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN:
                result = "UNKNOWN";
                break;
            case android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                result = "UNSPECIFIED_FAILURE";
                break;
            default:
                result = "UNKNOWN";
        }

        return result;
    }
}
