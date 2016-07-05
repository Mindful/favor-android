package com.favor.graphs;

import android.content.Context;
import android.util.AttributeSet;
import com.favor.library.Logger;
import com.favor.library.Util;
import lecho.lib.hellocharts.model.*;
import lecho.lib.hellocharts.view.ColumnChartView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 2/27/16.
 */
public class MultiBarGraph extends ColumnChartView implements FavorMultiValueGraph {

    //Mirroring the library implementation constructors

    public MultiBarGraph(Context context) {
        super(context);
    }

    public MultiBarGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiBarGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setValueData(String[] contactNames, long[] sent, long[] rec){
        List<Column> columns = new ArrayList<Column>();
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i = 0; i < sent.length; ++i){
            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>(2);
            values.add(new SubcolumnValue((float)sent[i], Util.sentColor()));
            values.add(new SubcolumnValue((float)rec[i], Util.receivedColor()));
            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
            axisValues.add(new AxisValue(i).setLabel(contactNames[i]));
        }

        ColumnChartData data = new ColumnChartData(columns);
        Axis axisY = new Axis().setHasLines(true);
        Axis axisX = new Axis(axisValues);
        data.setAxisYLeft(axisY);
        data.setAxisXBottom(axisX);

        setColumnChartData(data);
    }

    @Override
    public void setValueData(String[] contactNames, double[] sent, double[] rec) {
        List<Column> columns = new ArrayList<Column>();
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i = 0; i < sent.length; ++i){
            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>(2);
            values.add(new SubcolumnValue((float)sent[i], Util.sentColor()));
            values.add(new SubcolumnValue((float)rec[i], Util.receivedColor()));
            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
            axisValues.add(new AxisValue(i).setLabel(contactNames[i]));
        }

        ColumnChartData data = new ColumnChartData(columns);
        Axis axisY = new Axis().setHasLines(true);
        Axis axisX = new Axis(axisValues);
        data.setAxisYLeft(axisY);
        data.setAxisXBottom(axisX);

        setColumnChartData(data);
    }

    @Override
    public void setDefaults() {
        setValueTouchEnabled(false);
    }

    @Override
    public void setInsufficientDataDisplay() {
        //TODO:
    }
}
