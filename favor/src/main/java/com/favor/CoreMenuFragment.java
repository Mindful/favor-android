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

        String selected = (String) parent.getItemAtPosition(pos);
        if (view.getId() == R.id.metric){
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
            }
        } else if (view.getId() == R.id.graph){
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
