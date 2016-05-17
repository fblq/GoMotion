package com.campus.gomotion.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.campus.gomotion.R;
import com.campus.gomotion.service.MotionStatisticService;

import java.sql.Time;
import java.util.*;

/**
 * Author: zhong.zhou
 * Date: 16/5/9
 * Email: muxin_zg@163.com
 */
public class Monitor extends Activity {
    private static final String TAG = "Monitor";
    private Context context;
    private TextView fallingTime;
    private TextView interval;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_chart);
        tableLayout = (TableLayout) this.findViewById(R.id.fallingDetail);
        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Map<Time, Float> fallingLog = MotionStatisticService.fallingLog;
        if (fallingLog != null) {
            Iterator<Time> iterator = fallingLog.keySet().iterator();
            while (iterator.hasNext()) {
                Time key = iterator.next();
                Float value = fallingLog.get(key);
                TableRow tableRow = new TableRow(context);
                TextView textView = new TextView(context);
                TextView textView1 = new TextView(context);
                textView.setText(String.valueOf(key));
                textView1.setText(String.valueOf(value));
                tableRow.addView(textView);
                tableRow.addView(textView1);
                tableLayout.addView(tableRow);
            }
        }
        Log.v(TAG, "refresh data succeed");
    }
}
