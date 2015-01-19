package com.favor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.favor.library.Core;
import com.favor.library.Logger;
import com.favor.library.Reader;
import com.favor.ui.ContactDisplay;
import com.favor.ui.ContactDisplayAdapter;
import com.favor.ui.GraphableResult;
import lecho.lib.hellocharts.view.Chart;
import org.parceler.Parcels;

import java.util.HashMap;

public class VisualizeFragment extends Fragment {

    private GraphableResult data;
    private Chart chart;
    private final static String DATANAME = "DATA";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            data = Parcels.unwrap(savedInstanceState.getParcelable(DATANAME));
        }

         /*
        Note: THIS IS VERY IMPORTANT - using the context from the container and not somewhere else so the graph is the
        correct size
         */

        chart = data.buildDefaultGraph(container.getContext());

        return (View) chart;
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

}
