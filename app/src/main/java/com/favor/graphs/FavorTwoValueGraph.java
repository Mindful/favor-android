package com.favor.graphs;

import com.github.mikephil.charting.charts.BarChart;

/**
 * Created by josh on 2/20/16.
 */
public interface FavorTwoValueGraph extends FavorGraph {

    public void setTwoValueData(String contactName, double sent, double rec);
}
