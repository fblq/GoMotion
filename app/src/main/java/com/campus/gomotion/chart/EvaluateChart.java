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
        return "bar chart";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "the bar chart for daily evaluationData";
    }

    /**
     * draw the chart of evaluationData
     *
     * @param context the context
     * @return View
     */
    public View execute(Context context) {
        String[] titles = new String[]{"完成量", "体力感觉"};
        List<double[]> xValues = new ArrayList<>();
        xValues.add(new double[]{2, 4, 6});
        xValues.add(new double[]{2, 4, 6});
        List<double[]> yValues = new ArrayList<>();
        if (MainActivity.completions != null && !MainActivity.completions.isEmpty()) {
            yValues.add(new double[]{Double.parseDouble(MainActivity.completions), 0, 0});
        } else {
            yValues.add(new double[]{0, 0, 0});
        }
        if (MainActivity.evaluation != null && !MainActivity.evaluation.isEmpty()) {
            yValues.add(new double[]{0, 0, Double.parseDouble(MainActivity.evaluation)});
        } else {
            yValues.add(new double[]{0, 0, 0});
        }
        int[] colors = new int[]{Color.BLUE, Color.GRAY};
        XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
        renderer.getSeriesRendererAt(0).setChartValuesTextSize(30);
        renderer.setXLabelsAlign(Paint.Align.LEFT);// 数据从左到右显示
        renderer.setYLabelsAlign(Paint.Align.LEFT);
        renderer.setPanEnabled(true, false);
        renderer.setChartTitle("评估");
        renderer.setChartTitleTextSize(60);
        renderer.setAxisTitleTextSize(40);
        renderer.setLabelsTextSize(20);
        renderer.setXTitle("拟定日期(天)");
        renderer.setYTitle("百分比(%)");
        renderer.setLabelsColor(Color.GREEN);
        renderer.setAxesColor(Color.BLUE);
        renderer.setXLabels(8);
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(8);
        renderer.setYLabels(10);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(100);
        return ChartFactory.getBarChartView(context, buildDataset(titles, xValues, yValues), renderer, BarChart.Type.STACKED);
    }
}
