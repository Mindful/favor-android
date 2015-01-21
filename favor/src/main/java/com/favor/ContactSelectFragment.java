package com.favor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import com.favor.library.*;
import com.favor.ui.ContactDisplay;
import com.favor.ui.ContactDisplayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by josh on 12/27/14.
 */
public class ContactSelectFragment extends Fragment {
    private ContactDisplayAdapter adapter;
    private HashMap<Long, Boolean> selected;
    private final static String SELECTEDNAME = "SELECTED";

    //TODO: the problem here is that when this fragment is at 2 offscreen, it never loads its bundle so if it saves more than once we lose all the info

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For first time initialization
        processBundle(savedInstanceState);
        this.adapter = new ContactDisplayAdapter(Core.getContext(), ContactDisplay.buildDisplays(Reader.contacts()), selected);

        View view = inflater.inflate(R.layout.contact_select, container, false);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();

        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270){
            gridview.setNumColumns(3);
        } else {
            gridview.setNumColumns(2);
        }

        gridview.setAdapter(adapter);


        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Logger.info("Click item");
                adapter.toggleItem(position);
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Logger.info("Save contact instance state");
        if (selected != null) savedInstanceState.putSerializable(SELECTEDNAME, selected);
        else Logger.warning("Warning, null selected, possible data loss");
    }

    private void freshSelection(){
        selected = new HashMap<Long, Boolean>();
        for (Contact c : Reader.contacts()){
            selected.put(c.getId(), false);
        }
    }

    private void processBundle(Bundle savedInstanceState){
        //If this looks strange it's because the fragment is torn down to varying degrees at various points;
        //The PagerAdapater sometimes gives us empty bundles but we still have variable state in cases of scrolling
        if (selected == null){
            if (savedInstanceState == null || !savedInstanceState.containsKey(SELECTEDNAME) ) {
                Logger.info("Generating fresh selection for no bundle");
                freshSelection();
            } else {
                Logger.info("Using from bundle");
                selected = (HashMap<Long, Boolean>)savedInstanceState.getSerializable(SELECTEDNAME);
            }
        } else Logger.info("using from variable");

    }


    //This is necessary in addition to the processing we do in onCreateView for cases where the phone is turned while
    //the fragment is far enough offscreen to have already been destroyed (and also not viewed. obviously). Also it
    //MUST be the "onCreate" method as none of the other methods are called early enough in the lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        Logger.info("onCreate");
        processBundle(savedInstanceState);
    }

    public ArrayList<Contact> selectedContacts(){
        //This is written this way because it can end up being called when activites are created, apparently before onCreateView
        if (adapter != null ) return adapter.getSelectedContacts();
        else return new ArrayList<Contact>();
    }
}
