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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import com.favor.library.Core;
import com.favor.library.Logger;
import com.favor.util.Querier;


public class CoreMenuFragment extends Fragment implements AdapterView.OnItemSelectedListener  {


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using

        CoreActivity act = (CoreActivity) getActivity();
        Logger.info("ONITEMSELECTED");

        String selected = (String) parent.getItemAtPosition(pos);
        if (parent.getId() == R.id.metric){
            Logger.info("Metric Selected");
            switch (selected){
                case "Character Count":
                    act.setAnalytic(Querier.AnalyticType.Charcount);
                    break;
                case "Response Time":
                    act.setAnalytic(Querier.AnalyticType.ResponseTime);
                    break;
                case "Message Count":
                    act.setAnalytic(Querier.AnalyticType.Messagecount);
                    break;
                case "Conversational Response Time":
                    act.setAnalytic(Querier.AnalyticType.ConversationalResponseTime);
                    break;
            }
        } else if (parent.getId() == R.id.graph){
            Logger.info("Graph Selected");
            switch (selected){
                case "Bar Graph":
                case "Doughnut Graph":
            }
        }
        Logger.info(selected);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        //Logger.info("NOTHINGSELECTED");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.core_settings, container, false);

        Spinner spinner = (Spinner) view.findViewById(R.id.metric);
        spinner.setOnItemSelectedListener(this);
        spinner = (Spinner) view.findViewById(R.id.graph);
        spinner.setOnItemSelectedListener(this);

        CoreActivity act = (CoreActivity) getActivity();
        if (act.getEndDate() != -1) ((Button) view.findViewById(R.id.end_date)).setText(CoreActivity.dateFormatter.format(act.getEndDate()));
        if (act.getStartDate() != -1) ((Button) view.findViewById(R.id.start_date)).setText(CoreActivity.dateFormatter.format(act.getStartDate()));

        return view;
    }

}
