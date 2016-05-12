package com.campus.gomotion.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.campus.gomotion.R;
import com.campus.gomotion.adapter.ViewPagerAdapter;
import com.campus.gomotion.chart.StepChart;
import com.campus.gomotion.service.MotionStatisticService;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Movement extends Activity {
    private final static String TAG = "Movement";
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewpager;
    private ArrayList<View> pageViews;
    private RadioGroup radioGroup;
    private RadioButton rb_one, rb_two;

    private TextView walkTime;
    private TextView walkDistance;
    private TextView walkSteps;
    private TextView walkCalories;
    private TextView runTime;
    private TextView runDistance;
    private TextView runSteps;
    private TextView runCalorie;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movement_chart);
        radioGroup = (RadioGroup) findViewById(R.id.tab_group);
        rb_one = (RadioButton) findViewById(R.id.step);
        rb_two = (RadioButton) findViewById(R.id.calories);

        walkTime = (TextView) this.findViewById(R.id.walkTime);
        walkDistance = (TextView) this.findViewById(R.id.walkDistance);
        walkSteps = (TextView) this.findViewById(R.id.walkSteps);
        walkCalories = (TextView) this.findViewById(R.id.walkCalorie);

        runTime = (TextView) this.findViewById(R.id.runTime);
        runDistance = (TextView) this.findViewById(R.id.runDistance);
        runSteps = (TextView) this.findViewById(R.id.runSteps);
        runCalorie = (TextView) this.findViewById(R.id.runCalorie);

        viewpager = (ViewPager) findViewById(R.id.mainviewpager);
        LayoutInflater inflater = getLayoutInflater();
        pageViews = new ArrayList<>();

        pageViews.add(inflater.inflate(R.layout.step_chart, null));
        pageViews.add(inflater.inflate(R.layout.calories_chart, null));
        viewPagerAdapter = new ViewPagerAdapter(pageViews);
        viewpager.setAdapter(viewPagerAdapter);
        viewpager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                switch (arg0) {
                    case 0:
                        rb_one.performClick();
                        break;
                    case 1:
                        rb_two.performClick();
                        break;
                }

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.step:
                        viewpager.setCurrentItem(0);
                        break;
                    case R.id.calories:
                        viewpager.setCurrentItem(1);
                        break;

                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (MotionStatisticService.totalWalking != null) {
            walkTime.setText(String.valueOf(MotionStatisticService.totalWalking.getTime()));
            walkDistance.setText(String.valueOf(MotionStatisticService.totalWalking.getDistance()));
            walkSteps.setText(String.valueOf(MotionStatisticService.totalWalking.getStep()));
            walkCalories.setText(String.valueOf(MotionStatisticService.totalWalking.getEnergyConsumption()));
        }
        if (MotionStatisticService.totalRunning != null) {
            runTime.setText(String.valueOf(MotionStatisticService.totalRunning.getTime()));
            runDistance.setText(String.valueOf(MotionStatisticService.totalRunning.getDistance()));
            runSteps.setText(String.valueOf(MotionStatisticService.totalRunning.getStep()));
            runCalorie.setText(String.valueOf(MotionStatisticService.totalRunning.getEnergyConsumption()));

        }
        Log.v(TAG, "refresh data succeed");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem mi) {
        if (mi.isCheckable()) {
            mi.setChecked(true);
        }
        return true;

    }
}