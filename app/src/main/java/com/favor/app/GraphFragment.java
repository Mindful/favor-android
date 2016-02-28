package com.favor.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.favor.graphs.FavorGraph;

/**
 * Created by josh on 2/27/16.
 */
public class GraphFragment extends Fragment {

    FavorGraph graph;

    public GraphFragment(FavorGraph inputGraph){
        graph = inputGraph;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        try {
            if (graph == null){
                throw new NullPointerException("Cannot create graph fragment from null graph");
            }
            View graphView = (View) graph;
            return graphView;

//            LinearLayout root = new LinearLayout(container.getContext());
//            root.addView(graphView);
//            return root;
        } catch (ClassCastException e){
            throw new ClassCastException("Cannot create fragment from graph that is not a view");
        }
    }
}