package com.favor.ui;

/**
 * Created by josh on 12/31/14.
 */
public class DoubleBarGraph implements Graph {

    private String sentValues[];
    private String recValues[];
    private String graphHtml;

    DoubleBarGraph(String sentValues[], String recValues[]){
        this.sentValues = sentValues;
        this.recValues = recValues;
        if (sentValues.length != recValues.length) throw new IllegalArgumentException("Sent and received values must be equal lengths");
        computeHtml();
    }

    private void computeHtml(){

    }

    @Override
    public String html(){
        return graphHtml;
    }

}
