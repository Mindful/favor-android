package com.favor.ui;

import android.content.Context;
import com.favor.library.Contact;
import lecho.lib.hellocharts.model.*;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.ColumnChartView;
import org.parceler.ParcelConstructor;
import org.parceler.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by josh on 12/31/14.
 */
@org.parceler.Parcel
public class GraphableResult {
    public static enum GraphTypes {Column, Doughnut}
    public static enum AnalyticType {Charcount, Messagecount, ResponseTime}


    GraphTypes defaultType = GraphTypes.Column;
    double[] data1;
    double[] data2; //Will frequently be left unused.

    private long startDate;
    private long endDate;
    private GraphableResult.AnalyticType analyticType;
    ArrayList<Contact> contacts;

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public GraphTypes getDefaultGraphType() {
        return defaultType;
    }

    public GraphableResult(ArrayList<Contact> contacts){
        this.contacts = contacts;
    }

    public GraphableResult(ArrayList<Contact> contacts, long[] data1, long[] data2){
        this.contacts = (ArrayList<Contact>) contacts.clone();
        this.data1 = new double[data1.length];
        this.data2 = new double[data2.length];
        for (int i = 0; i < data1.length; ++i){
            this.data1[i] = (double)data1[i];
            this.data2[i] = (double)data2[i];
        }
    }

    public GraphableResult(ArrayList<Contact> contacts, long[] data){
        this.contacts = (ArrayList<Contact>) contacts.clone();
        this.data1 = new double[data.length];
        for (int i = 0; i < data.length; ++i){
            this.data1[i] = (double)data[i];
        }
    }

    @ParcelConstructor
    public GraphableResult(ArrayList<Contact> contacts, double[] data1, double[] data2){
        this.contacts = (ArrayList<Contact>) contacts.clone();
        this.data1 = data1;
        this.data2 = data2;
    }

    public GraphableResult(ArrayList<Contact> contacts, double[] data){
        this.contacts = (ArrayList<Contact>) contacts.clone();
        this.data1 = data;
    }

    public ArrayList<GraphTypes> getSupportedGraphs(){
        ArrayList<GraphTypes> result = new ArrayList<GraphTypes>();
        result.add(GraphTypes.Column);
        if (data2 == null){
            if (data1.length == 1) result.add(GraphTypes.Doughnut);
        }
        else if (data1.length == 2) result.add(GraphTypes.Doughnut);

        return result;
    }

    public Chart buildDefaultGraph(Context context, boolean animate){
        return buildGraph(defaultType, context, animate);
    }


    public Chart buildGraph(GraphTypes type, Context context, boolean animate){
        switch (type){
            case Column:
                return columnChart(context, animate);
            case Doughnut: //TODO: dougnut graph
            default:
                return columnChart(context, animate);
        }
    }

    public ColumnChartData columnData(boolean forAnimation){
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;

        float max = 0;
        if (forAnimation){
            for (int i = 0; i < data1.length; ++i){
                if (data1[i] > max) max = (float)data1[i];
                if (data2 != null){
                    if (data2[i] > max) max = (float)data2[i];
                }
            }
        }

        for (int i = 0; i < data1.length; ++i) {
            values = new ArrayList<SubcolumnValue>();


            if (forAnimation){
                values.add(new SubcolumnValue((float)Math.random() * max).setTarget((float)data1[i]));
                if (data2 != null) values.add(new SubcolumnValue(((float)Math.random() * max)).setTarget((float) data2[i]));
            } else {
                values.add(new SubcolumnValue((float)data1[i]));
                if (data2 != null) values.add(new SubcolumnValue((float)data2[i]));
            }

            Column column = new Column(values);
            column.setHasLabels(true);
            columns.add(column);
        }


        ColumnChartData data = new ColumnChartData(columns);

        Axis x = new Axis().setName("Contact");
        List<AxisValue> names = new ArrayList<AxisValue>();
        for (int i = 0; i < contacts.size(); ++i){
            names.add(new AxisValue(i, contacts.get(i).getDisplayName().toCharArray()));
        }
        x.setValues(names);
        Axis y = new Axis().setHasLines(true).setName("Metric Name Here"); //TODO: metric name
        data.setAxisXBottom(x);
        data.setAxisYLeft(y);

        return data;
    }

    private ColumnChartView columnChart(Context context, boolean animate){
        ColumnChartData data = columnData(animate);

        ColumnChartView chart = new ColumnChartView(context);
        chart.setColumnChartData(data);
        if (animate) chart.startDataAnimation();


        //chart.setOnValueTouchListener
        //chart.startDataAnimation
        return chart;

    }

}
