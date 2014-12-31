package com.favor.ui;

/**
 * Created by josh on 12/31/14.
 */
public class DoughnutGraph implements Graph {
    private String values[];
    private String graphHtml;

    DoughnutGraph(String values[]){
        this.values = values;
        if (values.length != 2)throw new IllegalArgumentException("Must contain only two values");
        computeHtml();
    }

    private void computeHtml(){

    }

    @Override
    public String html(){
        return graphHtml;
    }
}
