package com.campus.gomotion.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.campus.gomotion.R;
import com.campus.gomotion.constant.MotionEnum;
import com.campus.gomotion.constant.UIData;
import com.campus.gomotion.constant.WifiApInfo;
import com.campus.gomotion.sensorData.DataPack;
import com.campus.gomotion.service.ChannelListenerService;
import com.campus.gomotion.service.MotionStatisticService;
import com.campus.gomotion.service.SynchronizeService;
import com.campus.gomotion.service.WifiApService;
import com.campus.gomotion.util.BasicConversionUtil;
import com.campus.gomotion.util.PhysicalConversionUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Author: zhong.zhou
 * Date: 16/4/21
 * Email: muxin_zg@163.com
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    public static String target;
    public static String evaluation;
    public static String completions;

    private TabHost tabHost;
    private Switch communicationSwitch;
    private Switch motionLogSwitch;
    private Button sportButton;
    private Button monitorButton;
    private Button assessButton;

    private Timer timer;

    private TextView walkTime;
    private TextView walkDistance;
    private TextView runTime;
    private TextView runDistance;

    private TextView fallingCount;
    private TextView averageTime;

    private EditText movingTarget;
    private EditText selfEvaluation;

    private LinearLayout dataView;
    private LinearLayout motionView;
    private TextView dataText;
    private TextView motionText;
    private MyHandler handler;
    private Context context = this;
    private Switch wifiSpotSwitch;
    private Switch synchronizeSwitch;

    private CircleBar circleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        tabHost = (TabHost) this.findViewById(R.id.tabhost);
        communicationSwitch = (Switch) this.findViewById(R.id.communication);
        motionLogSwitch = (Switch) this.findViewById(R.id.motion_log);
        sportButton = (Button) this.findViewById(R.id.sport);
        monitorButton = (Button) this.findViewById(R.id.monitor);
        assessButton = (Button) this.findViewById(R.id.assess);

        dataView = (LinearLayout) this.findViewById(R.id.dataView);
        motionView = (LinearLayout) this.findViewById(R.id.motionView);
        wifiSpotSwitch = (Switch) this.findViewById(R.id.wifiSpotSwitch);
        synchronizeSwitch = (Switch) this.findViewById(R.id.synchronizeSwitch);
        dataText = (TextView) this.findViewById(R.id.dataText);
        motionText = (TextView) this.findViewById(R.id.motionText);
        walkTime = (TextView) this.findViewById(R.id.walkTime);
        walkDistance = (TextView) this.findViewById(R.id.walkDistance);
        runTime = (TextView) this.findViewById(R.id.runTime);
        runDistance = (TextView) this.findViewById(R.id.runDistance);

        fallingCount = (TextView) this.findViewById(R.id.fallingCount);
        averageTime = (TextView) this.findViewById(R.id.averageTime);

        movingTarget = (EditText) this.findViewById(R.id.movingTarget);
        selfEvaluation = (EditText) this.findViewById(R.id.selfEvaluation);
        circleBar = (CircleBar) this.findViewById(R.id.circle);
        handler = new MyHandler(getMainLooper());

        /**
         * initial table host
         */
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("", getResources().
                getDrawable(R.drawable.sport00)).setContent(R.id.view1));
        tabHost.addTab(tabHost.newTabSpec("tb2").setIndicator("", getResources().
                getDrawable(R.drawable.kid5)).setContent(R.id.view2));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("", getResources().
                getDrawable(R.drawable.note)).setContent(R.id.view3));

        communicationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dataView.setVisibility(View.VISIBLE);
                } else {
                    dataView.setVisibility(View.INVISIBLE);
                }
            }
        });

        motionLogSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    motionView.setVisibility(View.VISIBLE);
                } else {
                    motionView.setVisibility(View.INVISIBLE);
                }
            }
        });

        sportButton.setOnClickListener(this);
        monitorButton.setOnClickListener(this);
        assessButton.setOnClickListener(this);

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
            private ChannelListenerService channelListenerService = new ChannelListenerService(WifiApInfo.SERVICE_SPORT, handler);
            private FutureTask<String> futureTask = new FutureTask<>(channelListenerService);

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (executor.isShutdown()) {
                    executor = Executors.newSingleThreadExecutor();
                }
                if (futureTask.isCancelled()) {
                    channelListenerService = new ChannelListenerService(WifiApInfo.SERVICE_SPORT, handler);
                    futureTask = new FutureTask<>(channelListenerService);
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
                    channelListenerService.closeServerSocket();
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
                MotionStatisticService motionStatisticService = new MotionStatisticService(handler);
                try {
                    while (true) {
                        ArrayDeque<DataPack> dataPacks = SynchronizeService.dataPacks.takeAll();
                        motionStatisticService.motionStatistic(dataPacks);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }).start();

        /**
         * start timer task
         */
        timer = new Timer(true);
        setTimerTask();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sport:
                Intent movementIntent = new Intent();
                movementIntent.setClass(MainActivity.this, Movement.class);
                startActivity(movementIntent);
                break;
            case R.id.monitor:
                Intent monitorIntent = new Intent();
                monitorIntent.setClass(MainActivity.this, Monitor.class);
                startActivity(monitorIntent);
                break;
            case R.id.assess:
                Intent evaluationIntent = new Intent();
                evaluationIntent.setClass(MainActivity.this, Evaluation.class);
                startActivity(evaluationIntent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        target = movingTarget.getText().toString();
        if (!target.isEmpty()) {
            circleBar.setMaxStepNumber(Integer.parseInt(target));
        } else {
            circleBar.setMaxStepNumber(1000);
        }
        long currentCompletion = MotionStatisticService.calculateCompletion();
        if (!target.isEmpty()) {
            completions = String.valueOf(100 * (float) currentCompletion / Float.parseFloat(target));
        } else {
            completions = String.valueOf(100 * (float) currentCompletion / 1000);
        }
        evaluation = selfEvaluation.getText().toString();
        circleBar.update((int) currentCompletion, 800);
    }

    @Override
    protected void onPause() {
        super.onPause();
        evaluation = selfEvaluation.getText().toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        if (timer != null) {
            timer = null;
        }
    }

    private void setTimerTask() {
        final MotionStatisticService motionStatisticService = new MotionStatisticService(handler);
        TimerTask loadData = new TimerTask() {
            @Override
            public void run() {
                motionStatisticService.loadDataToCache();
                Log.v(TAG, "load data to cache");
            }
        };
        timer.schedule(loadData, 10000, 1000 * 60);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyHandler extends Handler {
        Looper looper;

        MyHandler(Looper looper) {
            this.looper = looper;
        }

        @Override
        public void handleMessage(Message msg) {
            long now = System.currentTimeMillis();
            Time currentTime = new Time(now);
            String motionKind = MotionEnum.UNKNOW.getName();
            Bundle bundle = msg.getData();
            if (bundle != null && bundle.size() > 0) {
                walkTime.setText((String) bundle.get(UIData.WALK_TIME));
                walkDistance.setText((String) bundle.get(UIData.WALK_DISTANCE));
                runTime.setText((String) bundle.get(UIData.RUN_TIME));
                runDistance.setText((String) bundle.get(UIData.RUN_DISTANCE));
                fallingCount.setText((String) (bundle.get(UIData.FALLING_COUNT)));
                averageTime.setText((String) (bundle.get(UIData.FALLING_AVERAGE_TIME)));
                motionKind = (String) bundle.get(UIData.MOTION_KING);
            }
            if (motionKind != null && !motionKind.equals(MotionEnum.UNKNOW.getName())) {
                if (motionText.getLineCount() >= 20) {
                    motionText.setText("");
                } else {
                    motionText.append(String.valueOf(currentTime) + " --》 " + motionKind + "\n");
                }
            }
            if (dataText.getLineCount() >= 20) {
                dataText.setText("");
            }
            if (msg.what == 0x12) {
                dataText.append(msg.obj.toString() + " ");
            }
            Log.v(TAG, "refresh ui succeed");
        }
    }
}
