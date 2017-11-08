// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides the primary API for managing aspects of Wi-Fi connectivity.
 *
 * @author lmercer@mit.edu (Logan Mercer)
 *
 */
@DesignerComponent(version = YaVersion.WIFI_MANAGER_COMPONENT_VERSION,
                   description = "Component that provides the primary API for managing aspects of Wi-Fi connectivity. " +
                           "NOTE: This is partial implementation. Have suggestions? Contact us.",
                   category = ComponentCategory.CONNECTIVITY,
                   nonVisible = true,
                   iconName = "images/phoneip.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.ACCESS_WIFI_STATE,android.permission.CHANGE_WIFI_STATE")
public class WiFiManager extends AndroidNonvisibleComponent implements Component, OnDestroyListener {

  private static Activity activity;
  private static final String LOG_TAG = "WiFiManager";
  private final Form form;
  private boolean enabled=false;
    private WifiManager wifiManager=null;
    private WifiReceiver receiverWifi;

    public WiFiManager(ComponentContainer container) {
    super(container.$form());
    this.form = container.$form();
    activity = container.$context();
    form.registerForOnDestroy(this);
  }

  @SimpleFunction(description = "Returns the IP address of the device in the form of a String")
  public String GetWifiIpAddress() {
    DhcpInfo ip;
//    Object wifiManager = activity.getSystemService(Context.WIFI_SERVICE);
    ip = getWiFiManager().getDhcpInfo();
    int s_ipAddress= ip.ipAddress;
    String ipAddress;
    if (IsConnected()) {
      ipAddress = intToIp(s_ipAddress);
      this.enabled = true;
    }
    else {
      ipAddress = "Error: No Wifi Connection";
      this.enabled = false;
    }
    return ipAddress;
  }

 @SimpleFunction(description = "Returns the MAC address of the device")
  public String GetMacAddress() {
   getWiFiManager();
   WifiInfo wInfo = wifiManager.getConnectionInfo();
   return wInfo.getMacAddress();
  }

  private WifiManager getWiFiManager() {
      if (wifiManager == null) {
          wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
      }
      return wifiManager;
  }
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns true if connected to WiFi else false")
  public boolean Enabled() {
    return this.enabled;
  }

  public void ConnectToSSID(String networkSSID, String networkPass) {
    WifiConfiguration conf = new WifiConfiguration();
    conf.SSID = "\"" + networkSSID + "\"";
    conf.wepKeys[0] = "\"" + networkPass + "\"";
    conf.wepTxKeyIndex = 0;
    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

  }

  @SimpleProperty(description = "Enable or disable Wi-Fi")
  public void Enabled(boolean enabled) {
   this.enabled = enabled;
   getWiFiManager();
   wifiManager.setWifiEnabled(enabled);
  }

  @SimpleFunction(description = "Returns TRUE if the phone is on Wifi, FALSE otherwise")
  public boolean IsConnected() {
    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService("connectivity");
    NetworkInfo networkInfo = null;
    if (connectivityManager != null) {
      networkInfo =
        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }
    return networkInfo != null && networkInfo.isConnected();
  }


  @SimpleFunction(description = "Obtain the Android Application Version")
  public String GetVersionName() {
    try {
      PackageInfo pInfo = form.getPackageManager().getPackageInfo(form.getPackageName(), 0);
      return (pInfo.versionName);
    } catch (NameNotFoundException e) {
      Log.e(LOG_TAG, "Exception fetching package name.", e);
      return ("");
    }
  }

    @SimpleFunction(description = "Retrieves list of Available WiFi. For each WiFi it will be in format of: SSID=value,LEVEL=value,MAC=value,FREQ=value")
    public void WiFiList() {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncWiFiList();
            }
        });
    }

    private void asyncWiFiList() {
        receiverWifi = new WifiReceiver();
        activity.registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if(!getWiFiManager().isWifiEnabled()) {
            getWiFiManager().setWifiEnabled(true);
        }
        getWiFiManager().startScan();
    }

  public static String intToIp(int i) {
    return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >>24) & 0xFF);
  }

  private void unregister() {
      if (receiverWifi != null && activity !=null) {
          activity.unregisterReceiver(receiverWifi);
      }
  }
    @Override
    public void onDestroy() {
        unregister();
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<String> connections=new ArrayList<String>();
//            ArrayList<Float> Signal_Strenth= new ArrayList<Float>();

//            StringBuilder sb = new StringBuilder();
            List<ScanResult> wifiList = getWiFiManager().getScanResults();
            for (ScanResult aWifiList : wifiList) {
                int level = aWifiList.level;
                String macAddr = aWifiList.BSSID;
                connections.add("SSID="+aWifiList.SSID+",LEVEL="+level+",MAC="+macAddr+",FREQ="+aWifiList.frequency);
            }
            Collections.sort(connections);
            GotWiFiList(connections);

        }
    }

    @SimpleEvent(description="Triggered when WiFiList has been retrieved")
    public void GotWiFiList(List<String> wifiList) {
        EventDispatcher.dispatchEvent(this, "GotWiFiList", wifiList);
        unregister();
    }
}
