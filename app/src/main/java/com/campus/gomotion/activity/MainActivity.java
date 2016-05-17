package com.campus.gomotion.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.campus.gomotion.R;
import com.campus.gomotion.sensorData.Quaternion;
import com.campus.gomotion.service.MotionStatisticService;
import com.campus.gomotion.service.SynchronizeService;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;

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
    private Button communicationButton;
    private Button sportButton;
    private Button monitorButton;
    private Button assessButton;
    private Button synchronizationButton;
    private Button dataViewButton;

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
        communicationButton = (Button) this.findViewById(R.id.communication);
        sportButton = (Button) this.findViewById(R.id.sport);
        monitorButton = (Button) this.findViewById(R.id.monitor);
        assessButton = (Button) this.findViewById(R.id.assess);

        synchronizationButton = (Button) this.findViewById(R.id.synchronization);
        dataViewButton = (Button) this.findViewById(R.id.dataView);

        walkTime = (TextView) this.findViewById(R.id.walkTime);
        walkDistance = (TextView) this.findViewById(R.id.walkDistance);
        runTime = (TextView) this.findViewById(R.id.runTime);
        runDistance = (TextView) this.findViewById(R.id.runDistance);

        fallingCount = (TextView) this.findViewById(R.id.fallingCount);
        averageTime = (TextView) this.findViewById(R.id.averageTime);

        movingTarget = (EditText) this.findViewById(R.id.movingTarget);
        completion = (TextView) this.findViewById(R.id.completion);
        selfEvaluation = (EditText) this.findViewById(R.id.selfEvaluation);

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


        communicationButton.setOnClickListener(this);
        sportButton.setOnClickListener(this);
        monitorButton.setOnClickListener(this);
        assessButton.setOnClickListener(this);

        /**
         * start timer task
         */
        timer = new Timer(true);
        setTimerTask();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.communication:
                Intent connectionIntent = new Intent();
                connectionIntent.setClass(MainActivity.this, ViewData.class);
                startActivity(connectionIntent);
                break;
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
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (SynchronizeService.quaternions.size() >= 50) {
                    ArrayDeque<Quaternion> quaternions = SynchronizeService.quaternions;
                    motionStatisticService.motionStatistic(quaternions);
                    SynchronizeService.quaternions.clear();
                }
                Log.v(TAG, "timer task for counting motion");
            }
        };
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
        timer.schedule(timerTask, 10000, 1000);
        timer.schedule(timerTask1, 15000, 1000 * 60);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
