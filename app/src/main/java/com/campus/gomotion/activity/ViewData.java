package com.campus.gomotion.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.campus.gomotion.R;
import com.campus.gomotion.constant.WifiApInfo;
import com.campus.gomotion.service.PortListenerService;
import com.campus.gomotion.service.WifiApService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Author: zhong.zhou
 * Date: 16/5/11
 * Email: muxin_zg@163.com
 */
public class ViewData extends Activity {
    private static final String TAG = "ViewData";
    private static boolean wifiSpotStatus = false;
    private static boolean synchronizeStatus = false;

    private TextView textView;
    private MyHandler handler;
    private Context context = this;
    private Switch wifiSpotSwitch;
    private Switch synchronizeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_view);
        wifiSpotSwitch = (Switch) this.findViewById(R.id.wifiSpotSwitch);
        synchronizeSwitch = (Switch) this.findViewById(R.id.synchronizeSwitch);
        textView = (TextView) this.findViewById(R.id.dataTextView);

        handler = new MyHandler(getMainLooper());
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
    }

    /**
     * 用户按下Home键或Activity异常销毁时,用于保存任务的状态信息
     * @param outState Bundle
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        wifiSpotStatus = wifiSpotSwitch.isChecked();
        synchronizeStatus = synchronizeSwitch.isChecked();
    }

    @Override
    protected void onStart() {
        super.onStart();
        wifiSpotSwitch.setChecked(wifiSpotStatus);
        synchronizeSwitch.setChecked(synchronizeStatus);
        handler = new MyHandler(getMainLooper());
    }

    private class MyHandler extends Handler {
        Looper looper;

        MyHandler(Looper looper) {
            this.looper = looper;
        }

        @Override
        public void handleMessage(Message msg) {
            if (textView.getLineCount() >= 20) {
                textView.setText("");
            }
            if (msg.what == 0x12) {
                textView.append(msg.obj.toString()+" ");
            }
        }
    }
}
