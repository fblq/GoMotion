package com.campus.gomotion.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;
import com.campus.gomotion.service.MotionStatisticService;
import com.campus.gomotion.util.TypeConvertUtil;
import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.sql.Time;
import java.util.*;

public class StepChart extends AbstractDemoChart {


    public String getName() {
        return " bar chart";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "The bar chart for daily steps";
    }

    /**
     * draw the chart of steps
     *
     * @param context the context
     * @return View
     */
    public View execute(Context context) {
        String[] titles = new String[]{"步数"};
        List<double[]> xValues = new ArrayList<>();
        List<double[]> yValues = new ArrayList<>();
        Map<Time, Double> stepMap = MotionStatisticService.calculateTotalStep();
        int size = stepMap.size();
        double[] time = new double[size];
        double[] steps = new double[size];
        Iterator<Time> iterator = stepMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Time key = iterator.next();
            Double value = stepMap.get(key);
            time[i] = TypeConvertUtil.timeToDouble(key);
            steps[i] = value;
            i++;
        }
        xValues.add(time);
        yValues.add(steps);
        int[] colors = new int[]{Color.BLUE};
        XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        renderer.setChartTitle("总步数");
        renderer.setChartTitleTextSize(60);
        renderer.setXTitle("时间(hh:mm:ss)");
        renderer.setYTitle("步数(步)");
        renderer.setAxisTitleTextSize(40);
        renderer.setLabelsColor(Color.GREEN);
        renderer.setAxesColor(Color.BLUE);
        renderer.setXLabels(24);
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(12);
        renderer.setYLabels(10);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(1000);
        return ChartFactory.getBarChartView(context, buildDataset(titles, xValues, yValues), renderer, BarChart.Type.STACKED);
    }
}
