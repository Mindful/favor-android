package com.favor.ui;

import android.content.Context;
import android.view.View;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 12/31/14.
 */
public class DoubleResult extends GraphableResult {
    double[] data1;
    double[] data2;

    public DoubleResult(long[] data1, long[] data2){
        this.data1 = new double[data1.length];
        this.data2 = new double[data2.length];
        for (int i = 0; i < data1.length; ++i){
            this.data1[i] = (double)data1[i];
            this.data2[i] = (double)data2[i];
        }
    }

    public DoubleResult(double[] data1, double[] data2){
        this.data1 = data1;
        this.data2 = data2;
    }

    @Override
    public ArrayList<GraphTypes> getSupportedGraphs(){
        ArrayList<GraphTypes> result = new ArrayList<GraphTypes>();
        result.add(GraphTypes.DoubleBar);
        if (data1.length == 1){
            result.add(GraphTypes.Doughnut);
        }
        return result;
    }

    @Override
    public View buildDefaultGraph(Context context){
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < data1.length; ++i) {
            values = new ArrayList<SubcolumnValue>();
            values.add(new SubcolumnValue((float)data1[i]));
            values.add(new SubcolumnValue((float)data2[i]));

            Column column = new Column(values);
            column.setHasLabels(true);
            columns.add(column);
        }


        ColumnChartData data = new ColumnChartData(columns);
        List<AxisValue> names = new ArrayList<AxisValue>();
        for (int i = 0; i < data.getColumns().size(); ++i){
            names.add(new AxisValue(i, ("G"+i).toCharArray()));
        }

        return columnChart(data, names, context);

    }

    //TODO: NYI
    @Override
    public View buildGraph(GraphTypes type, Context context) throws UnsupportedOperationException{
        return buildDefaultGraph(context); //TODO: obviously, we should actually pick
    }
}
