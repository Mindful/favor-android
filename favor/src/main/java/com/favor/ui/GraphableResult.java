package com.favor.ui;

import java.util.ArrayList;

/**
 * Created by josh on 12/31/14.
 */
public interface GraphableResult {
    Object getData();
    Graph buildDefaultGraph();
    ArrayList<Graph.GraphTypes> getSupportedGraphs();
    Graph buildGraph(Graph.GraphTypes type);
}
