package com.favor.ui;

import java.util.ArrayList;

/**
 * Created by josh on 12/31/14.
 */
public class BasicResult<T> implements GraphableResult {
    T[] data;

    @Override
    public ArrayList<Graph.GraphTypes> getSupportedGraphs(){
        ArrayList<Graph.GraphTypes> result = new ArrayList<Graph.GraphTypes>();
        result.add(Graph.GraphTypes.Bar);
        if (data.length == 2){
            result.add(Graph.GraphTypes.Doughnut);
        }
        return result;
    }

    @Override
    public Graph buildDefaultGraph(){
        String values[] = new String[data.length];
        for (int i = 0; i < data.length; ++i){
            values[i] = ""+data[i];
        }
        return new BarGraph(values);
    }

    //TODO: NYI
    @Override
    public Graph buildGraph(Graph.GraphTypes type) throws UnsupportedOperationException{
        return new BarGraph(new String[1]);
    }
}
