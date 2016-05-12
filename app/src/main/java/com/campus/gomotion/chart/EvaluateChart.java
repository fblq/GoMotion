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
        renderer.setChartTitle("评估");
        renderer.setChartTitleTextSize(60);
        renderer.setXTitle("时间(hh:mm:ss)");
        renderer.setYTitle("分数(%)");
        renderer.setAxisTitleTextSize(40);
        renderer.setLabelsColor(Color.GREEN);
        renderer.setAxesColor(Color.BLUE);
        renderer.setXLabels(7);
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(7);
        renderer.setYLabels(10);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(100);
        return ChartFactory.getBarChartView(context, buildBarDataset(titles, values), renderer, BarChart.Type.STACKED);
    }
}
