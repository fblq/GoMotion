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
import com.campus.gomotion.constant.WifiApInfo;
import com.campus.gomotion.sensorData.AttitudeAngle;
import com.campus.gomotion.sensorData.DataPack;
import com.campus.gomotion.sensorData.Quaternion;
import com.campus.gomotion.service.MotionStatisticService;
import com.campus.gomotion.service.PortListenerService;
import com.campus.gomotion.service.SynchronizeService;
import com.campus.gomotion.service.WifiApService;
import com.campus.gomotion.util.FileUtil;
import com.campus.gomotion.util.PhysicalConversionUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.StringTokenizer;
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

    //private static String file = "/data/data/com.campus.gomotion/myFile.txt";
    private static String file = "/storage/emulated/0/amotion/er.txt";

    public static String target;
    public static String evaluation;
    public static String completions;

    private TabHost tabHost;
    private Switch communicationSwitch;
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
    private TextView completion;
    private EditText selfEvaluation;

    private static boolean wifiSpotStatus = false;
    private static boolean synchronizeStatus = false;

    private LinearLayout dateView;
    private TextView textView;
    private MyHandler handler;
    private Context context = this;
    private Switch wifiSpotSwitch;
    private Switch synchronizeSwitch;

    private CircleBar circleBar;
    private EditText setnum;
    private Button setnumbutton;
    int i = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        circleBar = (CircleBar) findViewById(R.id.circle);
        setnum = (EditText) findViewById(R.id.setnum);
        setnumbutton = (Button) findViewById(R.id.setnumbutton);
        circleBar.setMaxstepnumber(10000);//在此处设置目标步数
        setnumbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /**
                 * 点击设置步数
                 */
                circleBar.update(Integer.parseInt(setnum.getText().toString()),
                        700);
            }
        });

        tabHost = (TabHost) this.findViewById(R.id.tabhost);
        communicationSwitch = (Switch) this.findViewById(R.id.communication);
        sportButton = (Button) this.findViewById(R.id.sport);
        monitorButton = (Button) this.findViewById(R.id.monitor);
        assessButton = (Button) this.findViewById(R.id.assess);

        walkTime = (TextView) this.findViewById(R.id.walkTime);
        walkDistance = (TextView) this.findViewById(R.id.walkDistance);
        runTime = (TextView) this.findViewById(R.id.runTime);
        runDistance = (TextView) this.findViewById(R.id.runDistance);

        fallingCount = (TextView) this.findViewById(R.id.fallingCount);
        averageTime = (TextView) this.findViewById(R.id.averageTime);

        movingTarget = (EditText) this.findViewById(R.id.movingTarget);
        completion = (TextView) this.findViewById(R.id.completion);
        selfEvaluation = (EditText) this.findViewById(R.id.selfEvaluation);

        dateView = (LinearLayout) this.findViewById(R.id.dataView);
        wifiSpotSwitch = (Switch) this.findViewById(R.id.wifiSpotSwitch);
        synchronizeSwitch = (Switch) this.findViewById(R.id.synchronizeSwitch);
        textView = (TextView) this.findViewById(R.id.dataTextView);
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
                    dateView.setVisibility(View.VISIBLE);
                } else {
                    dateView.setVisibility(View.INVISIBLE);
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

        /**
         * start timer task
         */
        timer = new Timer(true);
        setTimerTask();

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileWriter fileWriter = null;
                PrintWriter printWriter = null;
                //MotionStatisticService motionStatisticService = new MotionStatisticService();
                try {
                    fileWriter = new FileWriter(file);
                    printWriter = new PrintWriter(fileWriter);
                    while (true) {
                        ArrayDeque<DataPack> dataPacks = SynchronizeService.dataPacks.takeAll();
                        for (DataPack dataPack : dataPacks) {
                           /* AttitudeAngle attitudeAngle = PhysicalConversionUtil.quaternionToAttitudeAngle(dataPack.getQuaternion());
                            printWriter.print(attitudeAngle.getYaw());
                            printWriter.print(",");
                            printWriter.print(attitudeAngle.getPitch());
                            printWriter.print(",");
                            printWriter.println(attitudeAngle.getRoll());*/
                            printWriter.print("0A ");
                            printWriter.println(dataPack.toString());
                        }
                        //motionStatisticService.motionStatistic(dataPacks);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    try {
                        if (printWriter != null) {
                            printWriter.close();
                        }
                        if (fileWriter != null) {
                            fileWriter.close();
                        }
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
        }).start();
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
        if (MotionStatisticService.totalWalking != null) {
            walkTime.setText(String.valueOf(MotionStatisticService.totalWalking.getTime()));
            walkDistance.setText(String.valueOf(MotionStatisticService.totalWalking.getDistance()));
        }
        if (MotionStatisticService.totalRunning != null) {
            runTime.setText(String.valueOf(MotionStatisticService.totalRunning.getTime()));
            runDistance.setText(String.valueOf(MotionStatisticService.totalRunning.getDistance()));
        }
        fallingCount.setText(String.valueOf(MotionStatisticService.calculateFallingTotalCount()));
        averageTime.setText(String.valueOf(MotionStatisticService.calculateAverageFallingTime()));

        target = movingTarget.getText().toString();
        if (!target.isEmpty()) {
            completion.setText(String.valueOf((float) MotionStatisticService.calculateCompletion() / Float.parseFloat(target)));
        }
        completions = completion.getText().toString();
        evaluation = selfEvaluation.getText().toString();
        Log.v(TAG, "refresh data succeed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        completions = completion.getText().toString();
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
        final MotionStatisticService motionStatisticService
                = new MotionStatisticService();
        /**
         * time interval is 1s
         */
       /* TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (SynchronizeService.quaternions.size() >= 50) {
                    CopyOnWriteArrayList<Quaternion> quaternions = SynchronizeService.quaternions;
                    motionStatisticService.motionStatistic(quaternions);
                    SynchronizeService.quaternions.clear();
                }
                Log.v(TAG, "timer task for counting motion");
            }
        };*/
        /**
         * time interval is one minute
         */
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
        //timer.schedule(timerTask, 10000, 1000);
        timer.schedule(timerTask1, 15000, 1000 * 60);
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
            if (textView.getLineCount() >= 20) {
                textView.setText("");
            }
            if (msg.what == 0x12) {
                textView.append(msg.obj.toString() + " ");
            }
        }
    }
}
