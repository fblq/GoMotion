package com.campus.gomotion.service;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Author: zhong.zhou
 * Date: 16/4/21
 * Email: muxin_zg@163.com
 */
public class WifiApService {
    private static final String TAG = "WifiApService";
    /**
     * manage the service of wifi
     */
    private WifiManager wifiManager;

    public WifiApService(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * create wifi spot
     *
     * @return Boolean
     */
    public Boolean createWifiAp(String name, String password) {
        /**
         * ensure close wifi
         */
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        /**
         * ensure close wifi spot that exists
         */
        if (isWifiApEnabled()) {
            if (closeWifiAp()) {
                Log.v(TAG, "closeWifiAp succeed");
            } else {
                Log.v(TAG, "closeWifiAp failed");
            }
        }
        try {
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            WifiConfiguration configuration = new WifiConfiguration();
            configuration.SSID = name;
            configuration.preSharedKey = password;
            return (Boolean) method.invoke(wifiManager, configuration, true);
        } catch (Exception e) {
            Log.v(TAG, "createWifiAp:", e);
            return false;
        }
    }

    /**
     * close wifi spot
     *
     * @return Boolean
     */
    public Boolean closeWifiAp() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration wifiConfiguration = (WifiConfiguration) method.invoke(wifiManager);
            Method method1 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            return (Boolean) method1.invoke(wifiManager, wifiConfiguration, false);
        } catch (Exception e) {
            Log.v(TAG, "closeWifiAp", e);
            return false;
        }
    }

    /**
     * checkout the status of wifi spot
     *
     * @return Boolean
     */
    public Boolean isWifiApEnabled() {
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            return (Boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            Log.v(TAG, "isWifiApEnabled", e);
            return false;
        }
    }
}
