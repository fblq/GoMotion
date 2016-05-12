package com.campus.gomotion.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.campus.gomotion.service.MotionStatisticService;
import com.campus.gomotion.util.TypeConvertUtil;
import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.sql.Time;
import java.util.*;

/**
 * Author: zhong.zhou
 * Date: 16/5/10
 * Email: muxin_zg@163.com
 */
public class CaloriesChart extends AbstractDemoChart {
    public String getName() {
        return "bar chart";
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
     * draw the chart of caloriesData
     *
     * @param context the context
     * @return View
     */
    public View execute(Context context) {
        String[] titles = new String[]{"卡路里"};
        List<double[]> xValues = new ArrayList<>();
        List<double[]> yValues = new ArrayList<>();
        Map<Time, Double> caloriesMap = MotionStatisticService.calculateTotalCalories();
        int size = caloriesMap.size();
        double[] time = new double[size];
        double[] calories = new double[size];
        Iterator<Time> iterator = caloriesMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Time key = iterator.next();
            Double value = caloriesMap.get(key);
            time[i] = TypeConvertUtil.timeToDouble(key);
            calories[i] = value;
            i++;
        }
        xValues.add(time);
        yValues.add(calories);
        int[] colors = new int[]{Color.BLUE};
        XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        renderer.setChartTitle("卡路里");
        renderer.setChartTitleTextSize(60);
        renderer.setXTitle("时间(hh:mm:ss)");
        renderer.setYTitle("能耗(cal)");
        renderer.setAxisTitleTextSize(40);
        renderer.setLabelsColor(Color.GREEN);
        renderer.setAxesColor(Color.BLUE);
        renderer.setXLabels(48);
        renderer.setYLabels(10);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(100);
        return ChartFactory.getBarChartView(context, buildDataset(titles, xValues, yValues), renderer, BarChart.Type.STACKED);
    }
}
