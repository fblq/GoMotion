package com.campus.gomotion.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.campus.gomotion.R;
import com.campus.gomotion.service.MotionStatisticService;
import org.w3c.dom.Text;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: zhong.zhou
 * Date: 16/4/21
 * Email: muxin_zg@163.com
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static String target;
    public static String evaluation;
    public static String completions;

    private TabHost tabHost;
    private ImageButton imageButton;
    private Button sportButton;
    private Button monitorButton;
    private Button assessButton;
    //private Timer timer;

    private TextView walkTime;
    private TextView walkDistance;
    private TextView runTime;
    private TextView runDistance;

    private TextView fallingCount;
    private TextView averageTime;

    private EditText movingTarget;
    private TextView completion;
    private EditText selfEvaluation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        tabHost = (TabHost) this.findViewById(R.id.tabhost);
        imageButton = (ImageButton) this.findViewById(R.id.imageButton);
        sportButton = (Button) this.findViewById(R.id.sportButton);
        monitorButton = (Button) this.findViewById(R.id.monitorButton);
        assessButton = (Button) this.findViewById(R.id.assessButton);

        walkTime = (TextView) this.findViewById(R.id.walkTime);
        walkDistance = (TextView) this.findViewById(R.id.walkDistance);
        runTime = (TextView) this.findViewById(R.id.runTime);
        runDistance = (TextView) this.findViewById(R.id.runDistance);

        fallingCount = (TextView) this.findViewById(R.id.fallingCount);
        averageTime = (TextView) this.findViewById(R.id.averageTime);

        movingTarget = (EditText) this.findViewById(R.id.movingTarget);
        completion = (TextView) this.findViewById(R.id.completion);
        selfEvaluation = (EditText) this.findViewById(R.id.selfEvaluation);

        TextView interval = (TextView)this.findViewById(R.id.interval);
        /**
         * start the timer task
         */
        /*timer = new Timer(true);
        setTimerTask();
*/
        /**
         * initial table host
         */
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("moving", getResources().
                getDrawable(R.drawable.ic_accessibility_black_36dp)).setContent(R.id.view1));
        tabHost.addTab(tabHost.newTabSpec("tb2").setIndicator("monitor", getResources().
                getDrawable(R.drawable.ic_alarm_add_black_36dp)).setContent(R.id.view2));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("evaluation", getResources().
                getDrawable(R.drawable.ic_assignment_black_36dp)).setContent(R.id.view3));

        /**
         * view the process of synchronizing data
         */
        imageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Connection.class);
                startActivity(intent);
            }
        });
        /**
         * view the detail of moving
         */
        sportButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Movement.class);
                startActivity(intent);
            }
        });

        /**
         * view the detail of monitor
         */
        monitorButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Monitor.class);
                startActivity(intent);
            }
        });

        /**
         * view the detail of evaluation
         */
        assessButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Evaluation.class);
                startActivity(intent);
            }
        });
    }

   /* public void setTimerTask() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
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
                if(!target.isEmpty()){
                    completion.setText(String.valueOf((float) MotionStatisticService.calculateCompletion() / Float.parseFloat(target)));
                }
                completions = completion.getText().toString();
                evaluation = selfEvaluation.getText().toString();

                Log.v(TAG, "update data");
            }
        };
        timer.schedule(timerTask, 10000, 1000 * 60 * 30);
    }
*/
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
        if(!target.isEmpty()){
            completion.setText(String.valueOf((float) MotionStatisticService.calculateCompletion() / Float.parseFloat(target)));
        }
        completions = completion.getText().toString();
        evaluation = selfEvaluation.getText().toString();

        Log.v(TAG, "update data");
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
