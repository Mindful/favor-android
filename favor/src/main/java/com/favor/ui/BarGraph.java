package com.favor.ui;

import com.favor.library.Core;
import com.favor.library.Logger;

import java.io.IOException;
import java.io.InputStream;

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

    private String getBaseHtml(){
        try{
            InputStream is = Core.getContext().getAssets().open("bar.html");
            byte[] buffer = new byte[is.available()];
            is.read(buffer, 0, buffer.length);
            String html = new String(buffer);
            is.close();
            return html;
        } catch (IOException e){
            Logger.error("Failed to load html for bar graph: "+e.getMessage());
            return "<b>Load error!</b>";
        }
    }

    private void computeHtml(){
        graphHtml = getBaseHtml(); //TODO; obviously no, we need to make changes to it
    }

    @Override
    public String html(){
        return graphHtml;
    }


}
