package com.favor.ui;

/**
 * Created by josh on 12/31/14.
 */
public class DoubleBarGraph implements Graph {

    private String sentValues[];
    private String recValues[];
    private String graphHtml;

    DoubleBarGraph(String sentValues[], String recValues[]) {
        this.sentValues = sentValues;
        this.recValues = recValues;
        computeHtml();
    }

    private void computeHtml() {

    }

    @Override
    public String html() {
        return graphHtml;
    }
}
