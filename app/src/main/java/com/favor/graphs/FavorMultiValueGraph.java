package com.favor.graphs;

/**
 * Created by josh on 2/20/16.
 */
public interface FavorMultiValueGraph extends FavorGraph {
    public void setValueData(String[] contactNames, double[] sent, double[] rec);
    public void setValueData(String[] contactNames, long[] sent, long[] rec);
}
