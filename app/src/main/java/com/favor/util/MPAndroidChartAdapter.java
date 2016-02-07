package com.favor.util;

import android.graphics.Color;
import com.favor.app.R;
import com.favor.library.Core;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.*;

import java.util.ArrayList;

/**
 * Created by josh on 2/6/16.
 */
public class MPAndroidChartAdapter {
    public static void sentReceivedBar(BarChart chart, double sent, double rec){
        BarData data = new BarData();
        data.addXValue("You");
        data.addXValue("Them"); //TODO: should be contact name

        ArrayList<BarEntry> sentEntries = new ArrayList<BarEntry>();
        sentEntries.add(new BarEntry((float) sent, 0));
        BarDataSet sentDataSet= new BarDataSet(sentEntries, Core.getContext().getString(R.string.sent));
        sentDataSet.setColor(Color.RED);
        data.addDataSet(sentDataSet);

        ArrayList<BarEntry> recEntries = new ArrayList<BarEntry>();
        recEntries.add(new BarEntry((float) rec, 1));
        BarDataSet recDataSet= new BarDataSet(recEntries, Core.getContext().getString(R.string.received));
        recDataSet.setColor(Color.BLUE);
        data.addDataSet(recDataSet);

        chart.setData(data);
        chart.notifyDataSetChanged();

        chart.getXAxis().setDrawGridLines(false);
     //   chart.getXAxis().getSpaceBetweenLabels()
//        chart.getAxisLeft().setDrawGridLines(false);
//        chart.getAxisRight().setDrawGridLines(false);
    }
}
