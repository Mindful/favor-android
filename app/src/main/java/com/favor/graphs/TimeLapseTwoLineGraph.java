package com.favor.graphs;

import android.content.Context;
import android.util.AttributeSet;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by josh on 2/20/16.
 */
public class TimeLapseTwoLineGraph extends LineChartView implements FavorTimeLapseTwoValueGraph {

    //Mirroring the library implementation constructors

    public TimeLapseTwoLineGraph(Context context) {
        super(context);
    }

    public TimeLapseTwoLineGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeLapseTwoLineGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }




    @Override
    public void setDefaults() {
        setValueTouchEnabled(false); //TODO: this right? +add all other defaults
    }

    @Override
    public void setInsufficientDataDisplay() {
        //TODO
    }
}
