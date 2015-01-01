package com.favor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.favor.ui.GraphView;

public class VisualizeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.visualize, container, false);
        GraphView gview = (GraphView) view.findViewById(R.id.graph_view);

        return view;
    }

}
