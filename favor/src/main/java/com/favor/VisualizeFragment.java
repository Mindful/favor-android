package com.favor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.favor.library.Contact;
import com.favor.library.Logger;
import com.favor.ui.DoubleResult;
import com.favor.ui.SingleResult;

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

        //SingleResult res = new SingleResult(new long[] {3,9,10,25,37,35,25});
        DoubleResult res = new DoubleResult(new long[] {3,9,10,25,37,35,25}, new long[] {46,29,10,8,22,8,15});
        

        /*
        Note: THIS IS VERY IMPORTANT - using the context from the container and not somewhere else so the graph is the
        correct size
         */
        return res.buildDefaultGraph(container.getContext());
    }

    public void setContacts(ArrayList<Contact> input){
        contacts = input;
    }

}
