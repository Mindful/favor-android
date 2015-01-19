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
    ContactDisplayAdapter adapter;
    private final static String SELECTEDNAME = "SELECTED";

    //TODO: the problem here is the fact that if we get too far out this can be recreated apparently without having its
    //oncreateview called

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidHelper.populateContacts(); //TODO: necessary? I don't think so...

        View view = inflater.inflate(R.layout.contact_select, container, false);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();

        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270){
            gridview.setNumColumns(3);
        } else {
            gridview.setNumColumns(2);
        }

        if (savedInstanceState == null) {
            this.adapter = new ContactDisplayAdapter(Core.getContext(), ContactDisplay.buildDisplays(Reader.contacts()), null);
        } else {
            this.adapter = new ContactDisplayAdapter(Core.getContext(), ContactDisplay.buildDisplays(Reader.contacts()),
                    (HashMap<Long, Boolean>)savedInstanceState.getSerializable(SELECTEDNAME));
        }
        gridview.setAdapter(adapter);


        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                adapter.toggleItem(position);
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (adapter != null) savedInstanceState.putSerializable(SELECTEDNAME, adapter.getSelected());
        else savedInstanceState.putSerializable(SELECTEDNAME, new ArrayList<Contact>());
    }

    public ArrayList<Contact> selectedContacts(){
        //This is written this way because it can end up being called when activites are created, apparently before onCreateView
        if (adapter != null ) return adapter.getSelectedContacts();
        else return new ArrayList<Contact>();
    }
}
