package com.campus.gomotion.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
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
public class CaloriesChart extends AbstractDemoChart{
    public String getName() {
        return "calories bar chart";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "The bar char for daily steps";
    }
    /**
     * draw the chart of calories
     *
     * @param context the context
     * @return View
     */
    public View execute(Context context) {
        String[] titles = new String[]{"卡路里"};
        List<Date[]> xValues = new ArrayList<>();
        List<double[]> yValues = new ArrayList<>();
        Map<Date, Double> caloriesMap = MotionStatisticService.calculateTotalCalories();
        int size = caloriesMap.size();
        Date[] dates = new Date[size];
        double[] steps = new double[size];
        Iterator<Date> iterator = caloriesMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Date key = iterator.next();
            Double value = caloriesMap.get(key);
            dates[i] = key;
            steps[i] = value;
            i++;
        }
        xValues.add(dates);
        yValues.add(steps);
        int[] colors = new int[]{Color.BLUE};
        XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        setChartSettings(renderer, "卡路里", "时间", "能耗(单位:cal)", 0, 24, 0, 1000, Color.GRAY, Color.LTGRAY);
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
