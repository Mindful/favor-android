package com.favor.graphs;

import android.content.Context;
import com.favor.library.Util;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 2/27/16.
 */
public class MultiBarGraph extends ColumnChartView implements FavorMultiValueGraph {

    public MultiBarGraph(Context context) {
        super(context);
    }

    @Override
    public void setValueData(String[] contactNames, double[] sent, double[] rec) {
        List<Column> columns = new ArrayList<Column>();
        for (int i = 0; i < sent.length; ++i){
            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>(2);
            values.add(new SubcolumnValue((float)sent[i], Util.sentColor()));
            values.add(new SubcolumnValue((float)rec[i], Util.receivedColor()));
            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
        }

        ColumnChartData data = new ColumnChartData(columns);
        Axis axisY = new Axis().setHasLines(true);
        data.setAxisYLeft(axisY);

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
