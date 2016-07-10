package com.favor.graphs;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.favor.app.R;
import com.favor.library.Core;
import com.favor.library.Logger;
import com.favor.library.Util;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.HorizontalBarHighlighter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer;
import com.github.mikephil.charting.renderer.XAxisRendererHorizontalBarChart;
import com.github.mikephil.charting.renderer.YAxisRendererHorizontalBarChart;
import com.github.mikephil.charting.utils.HorizontalViewPortHandler;
import com.github.mikephil.charting.utils.TransformerHorizontalBarChart;

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

    protected final float barWidth = 4; //Doesn't work right if it's not an even number
    protected final float barGap = 1;

    protected float barLocation(int index){
        return (float)(Math.floor(barWidth / 2) + barGap) + index * (barWidth + barGap);
    }

    protected float barMax(int count){
        return barGap + count * (barWidth + barGap);
    }

    @Override
    protected void init() {
        super.init();
        mRenderer = new HorizontalBarChartSmartValueRenderer(this, mAnimator, mViewPortHandler);
    }


    public void setTwoValueData(String contactName, double sent, double rec){
        if (sent <= 0 || rec <= 0) {
            setInsufficientDataDisplay();
            return;
        }
        BarData data = new BarData();

        ArrayList<BarEntry> sentEntries = new ArrayList<BarEntry>();
        sentEntries.add(new BarEntry(barLocation(0), (float) sent));
        BarDataSet sentDataSet= new BarDataSet(sentEntries, Core.getContext().getString(R.string.sent));
        sentDataSet.setColor(Util.sentColor());
        data.addDataSet(sentDataSet);

        ArrayList<BarEntry> recEntries = new ArrayList<BarEntry>();
        recEntries.add(new BarEntry(barLocation(1), (float) rec));
        BarDataSet recDataSet= new BarDataSet(recEntries, Core.getContext().getString(R.string.received));
        recDataSet.setColor(Util.receivedColor());


        data.addDataSet(recDataSet);
        data.setValueFormatter(new LargeValueFormatter());
        data.setDrawValues(true);
        data.setValueTextSize(10f);
        data.setBarWidth(barWidth);
        setData(data);

        getXAxis().setAxisMinValue(0);
        getXAxis().setAxisMaxValue(barMax(2));
        notifyDataSetChanged();
    }

    @Override
    public void setDefaults() {
        setTouchEnabled(false);
        setDescription("");
        setFitBars(true);
        getLegend().setEnabled(false);
        getXAxis().setDrawGridLines(false);
        getXAxis().setDrawLabels(false);
        getAxisLeft().setAxisMinValue(0);
        getAxisRight().setAxisMinValue(0);
        getAxisLeft().setDrawLabels(false);
        setDrawValueAboveBar(true);

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
