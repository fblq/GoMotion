package com.campus.gomotion.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.campus.gomotion.classification.Falling;
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
public class MonitorChart extends AbstractDemoChart{
    public String getName() {
        return "monitor bar chart";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "The bar chart for daily monitoring";
    }
    /**
     * draw the chart of monitor
     *
     * @param context the context
     * @return View
     */
    public View execute(Context context) {
        String[] titles = new String[]{"跌倒监测"};
        List<Date[]> xValues = new ArrayList<>();
        List<double[]> yValues = new ArrayList<>();
        Map<Date, Falling> fallingMap = MotionStatisticService.fallingMap;
        int size = fallingMap.size();
        Date[] dates = new Date[size];
        double[] steps = new double[size];
        Iterator<Date> iterator = fallingMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Date key = iterator.next();
            double value = fallingMap.get(key).getCount();
            dates[i] = key;
            steps[i] = value;
            i++;
        }
        xValues.add(dates);
        yValues.add(steps);
        int[] colors = new int[]{Color.BLUE};
        XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        setChartSettings(renderer, "跌倒监测", "时间", "跌倒次数(单位:次)", 0, 24, 0, 50, Color.GRAY, Color.LTGRAY);
        renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
        renderer.setXLabels(12);
        renderer.setYLabels(10);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.CENTER);
        renderer.setPanEnabled(false, false);
        renderer.setZoomRate(1.1f);
        renderer.setBarSpacing(0.5f);
        return ChartFactory.getBarChartView(context, buildDateDataset(titles, xValues, yValues), renderer, BarChart.Type.STACKED);
    }
}
