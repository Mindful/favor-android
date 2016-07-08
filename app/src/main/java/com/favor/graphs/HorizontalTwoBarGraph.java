package com.favor.graphs;

import android.content.Context;
import android.util.AttributeSet;
import com.favor.app.R;
import com.favor.library.Core;
import com.favor.library.Util;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

/**
 * Created by josh on 2/20/16.
 */
public class HorizontalTwoBarGraph extends HorizontalBarChart implements FavorTwoValueGraph {


    //Mirroring the library implementation constructors

    public HorizontalTwoBarGraph(Context context) {
        super(context);
    }

    public HorizontalTwoBarGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalTwoBarGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private static final float BAR_SIZE = 5f;




    public void setTwoValueData(String contactName, double sent, double rec){
        if (sent <= 0 || rec <= 0) {
            setInsufficientDataDisplay();
            return;
        }
        BarData data = new BarData();

        ArrayList<BarEntry> sentEntries = new ArrayList<BarEntry>();
        sentEntries.add(new BarEntry(BAR_SIZE * 0, (float) sent));
        BarDataSet sentDataSet= new BarDataSet(sentEntries, Core.getContext().getString(R.string.sent));
        sentDataSet.setColor(Util.sentColor());
        data.addDataSet(sentDataSet);

        ArrayList<BarEntry> recEntries = new ArrayList<BarEntry>();
        recEntries.add(new BarEntry(BAR_SIZE * 1, (float) rec));
        BarDataSet recDataSet= new BarDataSet(recEntries, Core.getContext().getString(R.string.received));
        recDataSet.setColor(Util.receivedColor());
        data.addDataSet(recDataSet);


        setData(data);

        data.setBarWidth(BAR_SIZE - 1);
        data.setValueTextSize(10f);
        setFitBars(true);

        getLegend().setForm(Legend.LegendForm.LINE);
        getLegend().setCustom(new int[] {Util.sentColor(), Util.receivedColor()},
                new String[] {contactName, Core.getContext().getString(R.string.sent_name)});
        getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);

        notifyDataSetChanged();
    }

    @Override
    public void setDefaults() {
        setTouchEnabled(false);
        setDrawValueAboveBar(true);
        setDescription("");
        //getLegend().setEnabled(false);
        getXAxis().setDrawGridLines(false);
        getAxisLeft().setAxisMinValue(0);
        getAxisRight().setAxisMinValue(0);
        getAxisRight().setDrawLabels(false);

        getXAxis().setDrawLabels(false);

        //TODO: this really a default? text size may vary by activity, maybe it should be a FavorGraph root method
        BarData data = getData();
        if (data != null){
            for (IBarDataSet dataSet : data.getDataSets()){
                dataSet.setValueTextSize(15);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public void setInsufficientDataDisplay() {
        //TODO: https://github.com/PhilJay/MPAndroidChart/issues/89
        //just careful we don't make it look weird if we get data for it later

        if (getData() != null ){
            clearValues();  //TODO: clearValues() vs clear()?
        }
        setNoDataTextDescription("Sorry");
        setNoDataText("Insufficient Data");
        notifyDataSetChanged();
    }
}
