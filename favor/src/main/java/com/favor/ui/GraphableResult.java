package com.favor.ui;

import android.content.Context;
import android.view.View;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.ColumnChartView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 12/31/14.
 */
public abstract class GraphableResult {
    enum GraphTypes {Bar, DoubleBar, Doughnut}
    String metricName;

    abstract View buildDefaultGraph(Context context);
    abstract ArrayList<GraphTypes> getSupportedGraphs();
    abstract View buildGraph(GraphTypes type, Context context);

    protected ColumnChartView columnChart(ColumnChartData data, List<AxisValue> names, Context context){
        Axis x = new Axis().setName("Contact");
        x.setValues(names);
        Axis y = new Axis().setHasLines(true).setName(metricName);
        data.setAxisXBottom(x);
        data.setAxisYLeft(y);

        ColumnChartView chart = new ColumnChartView(context);
        chart.setColumnChartData(data);

        //chart.setOnValueTouchListener
        //chart.startDataAnimation
        return chart;

    }

}
