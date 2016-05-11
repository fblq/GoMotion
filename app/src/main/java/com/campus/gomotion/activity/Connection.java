package com.campus.gomotion.activity;

import android.app.Activity;
import android.content.Context;
import android.os.*;
import android.util.Log;
import android.widget.*;
import com.campus.gomotion.R;
import com.campus.gomotion.constant.WifiApInfo;
import com.campus.gomotion.service.MotionStatisticService;
import com.campus.gomotion.service.PortListenerService;
import com.campus.gomotion.service.SynchronizeService;
import com.campus.gomotion.service.WifiApService;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Author: zhong.zhou
 * Date: 16/5/9
 * Email: muxin_zg@163.com
 */
public class Connection extends Activity {
    private final static String TAG = "Connection";
    private Context context = this;
    private Switch wifiSpotSwitch;
    private Switch synchronizeSwitch;
    private TextView textView;
    private Handler handler;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection);
        wifiSpotSwitch = (Switch) this.findViewById(R.id.wifiSpotSwitch);
        synchronizeSwitch = (Switch) this.findViewById(R.id.synchronizeSwitch);
        textView = (TextView) this.findViewById(R.id.dataTextView);
        /**
         * start timer task
         */
        timer = new Timer(true);
        setTimerTask();
        /**
         * wifi spot switch
         */
        wifiSpotSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            private WifiApService wifiApService = new WifiApService(context);

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (wifiApService.createWifiAp(WifiApInfo.WIFI_AP_NAME, WifiApInfo.WIFI_AP_PASSWORD)) {
                        Log.v(TAG, "createWifiAp success");
                    } else {
                        Log.v(TAG, "createWifiAp failed");
                    }
                } else {
                    if (wifiApService.isWifiApEnabled()) {
                        wifiApService.closeWifiAp();
                    }
                    if (synchronizeSwitch != null) {
                        synchronizeSwitch.setChecked(false);
                    }
                }
            }
        });

        /**
         * data synchronize switch
         */
        synchronizeSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            private ExecutorService executor = Executors.newSingleThreadExecutor();
            private PortListenerService portListenerService = new PortListenerService(WifiApInfo.SERVICE_SPORT, handler);
            private FutureTask<String> futureTask = new FutureTask<>(portListenerService);

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (executor.isShutdown()) {
                    executor = Executors.newSingleThreadExecutor();
                }
                if (futureTask.isCancelled()) {
                    portListenerService = new PortListenerService(WifiApInfo.SERVICE_SPORT, handler);
                    futureTask = new FutureTask<>(portListenerService);
                }
                if (isChecked) {
                    if (wifiSpotSwitch != null && !wifiSpotSwitch.isChecked()) {
                        Log.v(TAG, "please open wifiAp first");
                    }
                    executor.submit(futureTask);
                    executor.shutdown();
                    Log.v(TAG, "synchronizing data");
                } else {
                    /**
                     * interrupt the listen service
                     */
                    portListenerService.closeServerSocket();
                    if (futureTask.cancel(true)) {
                        Log.v(TAG, "cancel listen service succeed");
                    } else {
                        Log.v(TAG, "cancel listen service failed");
                    }
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (textView.getLineCount() >= 20) {
                            textView.setText("");
                        }
                        if (msg.what == 0x12) {
                            textView.append("\n" + msg.obj.toString());
                        }
                    }
                };
                Looper.loop();
            }
        }).start();
    }

    private void setTimerTask() {
        final MotionStatisticService motionStatisticService = new MotionStatisticService();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(!SynchronizeService.minuteCache.isEmpty()){
                    motionStatisticService.motionStatistic(SynchronizeService.minuteCache);
                    SynchronizeService.minuteCache.clear();
                }
                Log.v(TAG, "timer task for counting motion");
            }
        };
        TimerTask timerTask1 = new TimerTask() {
            @Override
            public void run() {
                motionStatisticService.loadDataToCache();
                /**
                 * clear cache at 0:00:00 every day
                 */
                long t = System.currentTimeMillis();
                if (t % (1000 * 60) == 0 && t % (1000 * 60 * 60) == 0 && t % (1000 * 60 * 60 * 24) == 0) {
                    motionStatisticService.clearCache();
                }
                Log.v(TAG, "load data to cache");
            }
        };
        timer.schedule(timerTask, 10000, 1000 * 60);
        timer.schedule(timerTask1, 15000, 1000 * 60 * 15);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        if (timer != null) {
            timer = null;
        }
    }
}