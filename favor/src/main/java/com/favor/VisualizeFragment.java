package com.favor;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.favor.library.Contact;
import com.favor.library.Core;
import com.favor.library.Logger;
import com.favor.library.Reader;
import com.favor.ui.ContactDisplay;
import com.favor.ui.ContactDisplayAdapter;
import com.favor.ui.GraphableResult;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.ColumnChartView;
import org.parceler.Parcels;

import java.util.HashMap;

public class VisualizeFragment extends Fragment {

    GraphableResult data;
    private Chart chart;
    private final static String DATANAME = "DATA";
    private Context drawContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.info("On create vizfrag");
        super.onCreate(savedInstanceState);
        drawContext = container.getContext();

        CoreActivity parentAct = (CoreActivity) getActivity();
        data = parentAct.currentResult; //TODO: this doesn't work (or isn't sufficient, at least) because the first onCreate happens before any data is set in the parent activity

        if (savedInstanceState != null && data == null){
            data = Parcels.unwrap(savedInstanceState.getParcelable(DATANAME));
            Logger.info("Unwrap data with length "+data.getContacts().size());
        } else if (data == null) data = parentAct.currentResult;


         /*
        Note: THIS IS VERY IMPORTANT - using the context from the container and not somewhere else so the graph is the
        correct size
         */

        chart = data.buildDefaultGraph(drawContext);
        Logger.info("Chart "+chart);

        return (View) chart;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        Logger.info("VIZFRAG CREATED");

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(DATANAME, Parcels.wrap(data));
    }

    public void setData(GraphableResult result) {
        if (result != data && chart != null){
            chart.startDataAnimation();
        }
        this.data = result;
    }

    public void redrawChart(){
        //TODO: this should really be current graph type, not default
        Logger.info("Data:"+data);
        Logger.info("type:"+data.getDefaultGraphType());
        switch(data.getDefaultGraphType()){
            case Column:
            default:
                //TODO: conditional to know, somehow, if we need to redraw - compare chart data to new data?
                Logger.info("Chart precast: "+chart);
                ColumnChartView colChart = (ColumnChartView) chart;
                Logger.info("Chart postcast: "+colChart);
                Logger.info("Data:"+data.columnData());
                colChart.setColumnChartData(data.columnData());
                colChart.startDataAnimation();

        }
    }

}
