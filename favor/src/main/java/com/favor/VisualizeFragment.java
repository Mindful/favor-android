package com.favor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.favor.library.Contact;
import com.favor.library.Logger;
import com.favor.ui.GraphView;
import com.favor.ui.GraphableResult;
import com.favor.ui.LongResult;

import java.util.ArrayList;

public class VisualizeFragment extends Fragment {

    private ArrayList<Contact> contacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (contacts == null) contacts = new ArrayList<Contact>();

        for (Contact c: contacts){
            Logger.info(c.getDisplayName());
        }

        LongResult res = new LongResult(new long[] {3,9,10,25,37,35,25});


//        View view = inflater.inflate(R.layout.visualize, container, false);
//        GraphView gview = (GraphView) view.findViewById(R.id.graph_view);

        return res.buildDefaultGraph(getActivity());
    }

    public void setContacts(ArrayList<Contact> input){
        contacts = input;
    }

}
