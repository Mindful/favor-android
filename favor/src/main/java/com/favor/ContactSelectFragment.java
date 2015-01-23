package com.favor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import com.favor.library.*;
import com.favor.ui.ContactDisplay;
import com.favor.ui.ContactDisplayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by josh on 12/27/14.
 */

//TODO: handling new contacts - they'll have to be added to the list, but also added to selected and marked as false
public class ContactSelectFragment extends Fragment {
    private ContactDisplayAdapter adapter;

    //The HashMap and ArrayList are both shared with the adapter, but need to exist independently so that they stay alive
    //when we lose the adapter (for example when the fragment is torn down)
    private HashMap<Long, Boolean> selected;
    private ArrayList<ContactDisplay> contacts = ContactDisplay.buildDisplays(Reader.contacts());
    public final static String SELECTEDNAME = "SELECTED";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For first time initialization
//        processBundle(savedInstanceState); previously used, but pretty sure this is redundant. TODO: delete with confidence

        this.adapter = new ContactDisplayAdapter(Core.getContext(), contacts, selected);

        View view = inflater.inflate(R.layout.contact_select, container, false);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();

        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) gridview.setNumColumns(3);
        else gridview.setNumColumns(2);

        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                adapter.toggleItem(position);
                ((CoreActivity) getActivity()).setContacts(selectedContacts());
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (selected != null) savedInstanceState.putSerializable(SELECTEDNAME, selected);
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
                freshSelection();
            } else {
                selected = (HashMap<Long, Boolean>)savedInstanceState.getSerializable(SELECTEDNAME);
            }
        }

    }


    //This is necessary in addition to the processing we do in onCreateView for cases where the phone is turned while
    //the fragment is far enough offscreen to have already been destroyed (and also not viewed. obviously). Also it
    //MUST be the "onCreate" method as none of the other methods are called early enough in the lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        processBundle(savedInstanceState);
    }

    public ArrayList<Contact> selectedContacts(){
        ArrayList<Contact> ret = new ArrayList<Contact>();
        if (selected == null) return ret; //TODO: this might possibly be redundant

        for (ContactDisplay disp : contacts){
            if (selected.get(disp.getId())){
                ret.add(disp.getContact());
            }
        }
        return ret;
    }
}
