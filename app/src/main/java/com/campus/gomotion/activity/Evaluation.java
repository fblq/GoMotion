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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluation_chart);
        completion = (TextView) this.findViewById(R.id.completion);
        selfEvaluation = (TextView) this.findViewById(R.id.selfEvaluation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (MainActivity.target != null && !MainActivity.target.isEmpty()) {
            completion.setText(String.valueOf(100 * (float) MotionStatisticService.calculateCompletion() / Float.parseFloat(MainActivity.target)));
        }
        if (MainActivity.evaluation != null && !MainActivity.evaluation.isEmpty()) {
            selfEvaluation.setText(MainActivity.evaluation);
        }
        Log.v(TAG, "refresh evaluation data succeed");
    }
}
