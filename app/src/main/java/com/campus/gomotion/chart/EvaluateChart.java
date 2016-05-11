package com.campus.gomotion.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.campus.gomotion.activity.MainActivity;
import com.campus.gomotion.service.MotionStatisticService;
import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.*;

/**
 * Author: zhong.zhou
 * Date: 16/5/10
 * Email: muxin_zg@163.com
 */
public class EvaluateChart extends AbstractDemoChart {
    public String getName() {
        return "evaluation bar chart";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "the bar chart for daily evaluation";
    }

    /**
     * draw the chart of evaluation
     *
     * @param context the context
     * @return View
     */
    public View execute(Context context) {
        String[] titles = new String[]{"完成量","体力感觉"};
        List<double[]> values = new ArrayList<>();
        if(MainActivity.completions!=null && !MainActivity.completions.isEmpty()){
            values.add(new double[]{Double.parseDouble(MainActivity.completions)});
        }else{
            values.add(new double[]{100});
        }
        if(MainActivity.evaluation!=null && !MainActivity.evaluation.isEmpty()){
            values.add(new double[]{Double.parseDouble(MainActivity.evaluation)});
        }else{
            values.add(new double[]{100});
        }
        int[] colors = new int[]{Color.BLUE,Color.GRAY};
        XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        setChartSettings(renderer, "评估", "时间", "分数(%)", 0, 6, 0, 200, Color.BLUE, Color.GRAY);
        renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
        renderer.getSeriesRendererAt(1).setDisplayChartValues(true);
        renderer.setXLabels(12);
        renderer.setYLabels(10);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.CENTER);
        renderer.setPanEnabled(false, false);
        renderer.setZoomRate(1.1f);
        renderer.setBarSpacing(0.5f);
        return ChartFactory.getBarChartView(context, buildBarDataset(titles, values), renderer, BarChart.Type.STACKED);
    }
}
