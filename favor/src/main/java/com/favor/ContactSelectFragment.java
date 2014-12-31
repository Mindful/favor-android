package com.favor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.favor.library.*;
import com.favor.ui.ContactDisplay;
import com.favor.ui.ContactDisplayAdapter;

/**
 * Created by josh on 12/27/14.
 */
public class ContactSelectFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidHelper.populateContacts();

        View view = inflater.inflate(R.layout.contact_select, container, false);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();

        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270){
            gridview.setNumColumns(3);
        } else {
            gridview.setNumColumns(2);
        }

        final ContactDisplayAdapter adapter = new ContactDisplayAdapter(Core.getContext(), ContactDisplay.buildDisplays(Reader.contacts()));
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ContactDisplay disp = (ContactDisplay) adapter.getItem(position);
                Toast.makeText(getActivity(), "Click "+disp.getName()+". Rec: "+disp.getCharCountReceived()+ " Sent: "+disp.getCharCountSent(), Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}