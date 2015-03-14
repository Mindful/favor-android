/*
 * Copyright (C) 2015  Joshua Tanner (mindful.jt@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.favor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.favor.library.Logger;
import com.favor.ui.GraphableResult;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.ColumnChartView;

public class VisualizeFragment extends Fragment {

    private GraphableResult data;
    private Chart chart;
    private final static String DATANAME = "DATA";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CoreActivity parentAct = (CoreActivity) getActivity();
        data = parentAct.getResult();

        chart = data.buildDefaultGraph(container.getContext(), true); //It's important to use the container context and not a different one, for sizing reasons

        return (View) chart;
    }

    public void redrawChart(GraphableResult newData){
        if (!newData.equals(data)){
            Logger.info("Redraw chart because of change in data");
            data = newData;
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

}
