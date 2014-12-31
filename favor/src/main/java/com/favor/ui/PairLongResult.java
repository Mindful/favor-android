package com.favor.ui;

import java.util.ArrayList;

/**
 * Created by josh on 12/31/14.
 */
public class PairLongResult implements GraphableResult {
    long[] sentData;
    long[] recData;

    @Override
    public Object getData(){
        long ret[][] = new long[2][];
        ret[0] = sentData;
        ret[1] = recData;
        return ret;
    }

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
            sentValues[i] = Long.toString(sentData[i]);
            recValues[i] = Long.toString(recData[i]);
        }
        return new DoubleBarGraph(sentValues, recValues);
    }

    //TODO: NYI
    @Override
    public Graph buildGraph(Graph.GraphTypes type) throws UnsupportedOperationException{
        return new BarGraph(new String[1]);
    }
}
