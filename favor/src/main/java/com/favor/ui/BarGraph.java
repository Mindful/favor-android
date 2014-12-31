package com.favor.ui;

/**
 * Created by josh on 12/31/14.
 */
public class BarGraph implements Graph {

    private String values[];
    private String graphHtml;

    BarGraph(String values[]){
        this.values = values;
        computeHtml();
    }

    private void computeHtml(){

    }

    @Override
    public String html(){
        return graphHtml;
    }


}
