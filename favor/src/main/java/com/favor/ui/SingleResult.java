package com.favor.ui;

import android.content.Context;
import android.view.View;
import lecho.lib.hellocharts.model.*;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 12/31/14.
 */
public class SingleResult extends GraphableResult {
    double[] data;

    public SingleResult(long[] data){
        this.data = new double[data.length];
        for (int i = 0; i < data.length; ++i){
            this.data[i] = (double)data[i];
        }
    }

    public SingleResult(double[] data){
        this.data = data;
    }

    @Override
    public ArrayList<GraphTypes> getSupportedGraphs(){
        ArrayList<GraphTypes> result = new ArrayList<GraphTypes>();
        result.add(GraphTypes.Bar);
        if (data.length == 2){
            result.add(GraphTypes.Doughnut);
        }
        return result;
    }

    @Override
    public View buildDefaultGraph(Context context){
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < data.length; ++i) {
            values = new ArrayList<SubcolumnValue>();
            values.add(new SubcolumnValue((float)data[i]));

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
