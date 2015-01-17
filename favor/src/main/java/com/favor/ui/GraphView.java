package com.favor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

import java.util.ArrayList;
import java.util.List;


//Largely copied from the previous favor iteration
public class GraphView extends LineChartView {


//    @Override
//    protected void onSizeChanged (int w, int h, int ow, int oh)
//    {
//        if (w==0 && h==0) return;
//        else GraphActivity.getGraph().show();
//    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GraphView(Context context) {
        super(context);
        init(context);
    }


    private void init(Context context)
    {
        List<PointValue> values = new ArrayList<PointValue>();
        new PointValue(3.5f, 4.5f);
        values.add(new PointValue(0, 2));
        values.add(new PointValue(1, 4));
        values.add(new PointValue(2, 3));
        values.add(new PointValue(3, 4));

        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(values).setColor(Color.BLUE).setCubic(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        setLineChartData(data);
    }


}