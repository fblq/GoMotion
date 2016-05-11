package com.campus.gomotion.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;
import com.campus.gomotion.service.MotionStatisticService;
import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.*;

public class StepChart extends AbstractDemoChart {


    public String getName() {
        return "step bar chart";
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
        List<Date[]> xValues = new ArrayList<>();
        List<double[]> yValues = new ArrayList<>();
        Map<Date, Double> stepMap = MotionStatisticService.calculateTotalStep();
        int size = stepMap.size();
        Date[] dates = new Date[size];
        double[] steps = new double[size];
        Iterator<Date> iterator = stepMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Date key = iterator.next();
            Double value = stepMap.get(key);
            dates[i] = key;
            steps[i] = value;
            i++;
        }
        xValues.add(dates);
        yValues.add(steps);
        int[] colors = new int[]{Color.BLUE};
        XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        setChartSettings(renderer, "总步数", "时间", "步数(单位:步)", 0, 24, 0, 24000, Color.GRAY, Color.LTGRAY);
        renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
        renderer.setXLabels(12);
        renderer.setYLabels(10);
        renderer.setXLabelsAlign(Align.CENTER);
        renderer.setYLabelsAlign(Align.CENTER);
        renderer.setPanEnabled(false, false);
        renderer.setZoomRate(1.1f);
        renderer.setBarSpacing(0.5f);
        return ChartFactory.getBarChartView(context, buildDateDataset(titles, xValues, yValues), renderer, BarChart.Type.STACKED);
    }
}
