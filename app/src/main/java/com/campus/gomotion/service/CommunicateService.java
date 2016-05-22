package com.campus.gomotion.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;
import com.campus.gomotion.constant.WifiApInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Author: zhong.zhou
 * Date: 16/5/9
 * Email: muxin_zg@163.com
 */
public class CommunicateService extends Service {
    private final static String TAG = "CommunicateService";
    private WifiApService wifiApService;
    private ExecutorService executorService;
    private ChannelListenerService channelListenerService;
    private Handler handler; //可以让Activity传过来

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wifiApService = new WifiApService(this);
        executorService = Executors.newSingleThreadExecutor();
        channelListenerService = new ChannelListenerService(WifiApInfo.SERVICE_SPORT, handler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (wifiApService.createWifiAp(WifiApInfo.WIFI_AP_NAME, WifiApInfo.WIFI_AP_PASSWORD)) {
            Log.v(TAG, "createWifiAp succeed");
            executorService = Executors.newSingleThreadExecutor();
            FutureTask<String> futureTask = new FutureTask<>(channelListenerService);
            executorService.submit(futureTask);
            executorService.shutdown();
        } else {
            Log.v(TAG, "createWifiAp failed");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        channelListenerService.closeServerSocket();
        if (wifiApService.isWifiApEnabled()) {
            wifiApService.closeWifiAp();
        }
        super.onDestroy();
    }
}