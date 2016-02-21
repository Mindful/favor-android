package com.favor.graphs;

import android.content.Context;
import android.util.AttributeSet;
import com.favor.app.R;
import com.favor.library.Core;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

/**
 * Created by josh on 2/20/16.
 */
public class HorizontalTwoValueGraph extends HorizontalBarChart implements FavorTwoValueGraph {

    //Mirroring the library implementation constructors

    public HorizontalTwoValueGraph(Context context) {
        super(context);
    }

    public HorizontalTwoValueGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalTwoValueGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTwoValueData(String contactName, double sent, double rec){
        if (sent <= 0 || rec <= 0) {
            setInsufficientDataDisplay();
            return;
        }
        BarData data = new BarData();

        ArrayList<BarEntry> sentEntries = new ArrayList<BarEntry>();
        sentEntries.add(new BarEntry((float) sent, 0));
        BarDataSet sentDataSet= new BarDataSet(sentEntries, Core.getContext().getString(R.string.sent));
        sentDataSet.setColor(Core.getContext().getResources().getColor(R.color.sent));
        data.addDataSet(sentDataSet);

        ArrayList<BarEntry> recEntries = new ArrayList<BarEntry>();
        recEntries.add(new BarEntry((float) rec, 1));
        BarDataSet recDataSet= new BarDataSet(recEntries, Core.getContext().getString(R.string.received));
        recDataSet.setColor(Core.getContext().getResources().getColor(R.color.rec));
        data.addDataSet(recDataSet);

        data.addXValue(Core.getContext().getString(R.string.sent_name));
        data.addXValue(contactName);

        setData(data);
        notifyDataSetChanged();
    }

    public void setGraphName(String name){
        setDescription(name);
    }

    @Override
    public void setDefaults() {
        setTouchEnabled(false);
        getXAxis().setDrawGridLines(false);

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
