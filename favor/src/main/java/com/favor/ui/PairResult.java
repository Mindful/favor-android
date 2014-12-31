package com.favor.ui;

import java.util.ArrayList;

/**
 * Created by josh on 12/31/14.
 */
public class PairResult<T> implements GraphableResult {
    T[] sentData;
    T[] recData;

    @Override
    public ArrayList<Graph.GraphTypes> getSupportedGraphs(){
        ArrayList<Graph.GraphTypes> result = new ArrayList<Graph.GraphTypes>();
        result.add(Graph.GraphTypes.Bar);
        if (sentData.length == 2){
            result.add(Graph.GraphTypes.Doughnut);
        }
        return result;
    }

    @Override
    public Graph buildDefaultGraph(){
        String sentValues[] = new String[sentData.length];
        String recValues[] = new String[recData.length];

        for (int i = 0; i < sentData.length; ++i){
            sentValues[i] = "" + sentData[i];
            recValues[i] = "" + recData[i];
        }
        return new DoubleBarGraph(sentValues, recValues);
    }

    //TODO: NYI
    @Override
    public Graph buildGraph(Graph.GraphTypes type) throws UnsupportedOperationException{
        return new BarGraph(new String[1]);
    }
}
