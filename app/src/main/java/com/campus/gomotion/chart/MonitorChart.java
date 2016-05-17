package com.campus.gomotion.chart;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import com.campus.gomotion.kind.Falling;
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
public class MonitorChart extends AbstractDemoChart {
    public String getName() {
        return "bar chart";
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
     * draw the chart of monitorDate
     *
     * @param context the context
     * @return View
     */
    public View execute(Context context) {
        String[] titles = new String[]{"跌倒监测"};
        List<double[]> xValues = new ArrayList<>();
        List<double[]> yValues = new ArrayList<>();
        Map<Time, Falling> fallingMap = MotionStatisticService.fallingMap;
        int size = fallingMap.size();
        double[] time = new double[size];
        double[] fallingCount = new double[size];
        Iterator<Time> iterator = fallingMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Time key = iterator.next();
            double value = fallingMap.get(key).getCount();
            time[i] = TypeConvertUtil.timeToDouble(key);
            fallingCount[i] = value;
            i++;
        }
        xValues.add(time);
        yValues.add(fallingCount);
        int[] colors = new int[]{Color.BLUE};
        XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        renderer.setChartTitle("跌倒监测");
        renderer.setChartTitleTextSize(60);
        renderer.setXTitle("时间(hh:mm:ss)");
        renderer.setYTitle("跌倒次数(次)");
        renderer.setAxisTitleTextSize(40);
        renderer.setLabelsColor(Color.GREEN);
        renderer.setAxesColor(Color.BLUE);
        renderer.setXLabels(24);
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(12);
        renderer.setYLabels(20);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(20);
        return ChartFactory.getBarChartView(context, buildDataset(titles, xValues, yValues), renderer, BarChart.Type.STACKED);
    }
}
