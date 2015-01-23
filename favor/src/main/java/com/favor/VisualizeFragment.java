package com.favor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.favor.ui.GraphableResult;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.ColumnChartView;
import org.parceler.Parcels;

public class VisualizeFragment extends Fragment {

    GraphableResult data;
    private Chart chart;
    private final static String DATANAME = "DATA";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CoreActivity parentAct = (CoreActivity) getActivity();
        data = parentAct.result;

        chart = data.buildDefaultGraph(container.getContext()); //It's important to use the container context and not a different one, for sizing reasons

        return (View) chart;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(DATANAME, Parcels.wrap(data));
    }

    public void redrawChart(){
        //TODO: this should really be current graph type, not default
        switch(data.getDefaultGraphType()){
            case Column:
                ColumnChartView colChart = (ColumnChartView) chart;
                colChart.setColumnChartData(data.columnData(true));
                colChart.startDataAnimation();
                return;
            default:
                return;

        }
    }

}
