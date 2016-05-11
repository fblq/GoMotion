package com.campus.gomotion.activity;

import android.app.Activity;
import android.os.Bundle;
import android.test.suitebuilder.TestMethod;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import com.campus.gomotion.R;
import com.campus.gomotion.service.MotionStatisticService;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: zhong.zhou
 * Date: 16/5/9
 * Email: muxin_zg@163.com
 */
public class Evaluation extends Activity {
    private final static String TAG = "Evaluation";
    private TextView completion;
    private TextView selfEvaluation;
    //private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluation);
        completion = (TextView) this.findViewById(R.id.completion);
        selfEvaluation = (TextView) this.findViewById(R.id.selfEvaluation);

        /*timer = new Timer(true);
        setTimerTask();*/
    }

    /*public void setTimerTask() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!MainActivity.target.isEmpty()) {
                    completion.setText(String.valueOf((float) MotionStatisticService.calculateCompletion() / Float.parseFloat(MainActivity.target)));
                }
                selfEvaluation.setText(MainActivity.evaluation);
                Log.v(TAG, "update the data of evaluation");
            }
        };
        timer.schedule(timerTask, 10000, 1000 * 60 * 30);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        if (!MainActivity.target.isEmpty()) {
            completion.setText(String.valueOf((float) MotionStatisticService.calculateCompletion() / Float.parseFloat(MainActivity.target)));
        }
        selfEvaluation.setText(MainActivity.evaluation);
        Log.v(TAG, "update the data of evaluation");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //timer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
