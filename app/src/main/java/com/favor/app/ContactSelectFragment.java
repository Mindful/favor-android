package com.favor.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.favor.library.Contact;
import com.favor.library.Debug;
import com.favor.library.Logger;
import com.favor.library.Reader;

public class ContactSelectFragment extends ListFragment {

    private ArrayAdapter<Contact> contacts;
    public final static String CONTACT = "CONTACT";


    public ContactSelectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contacts = new ArrayAdapter<Contact>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, Reader.contacts());


        setListAdapter(contacts);

    }

    @Override
    public void onStart(){
        super.onStart();
        //TODO: this needs to be set, but can't be set here because we're not done inflating this view yet
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Debug.debugToast("LONG CLICK", getActivity().getApplicationContext());
                return true;
            }
        });
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent contactStatsIntent = new Intent(getActivity(), ContactStatsActivity.class);
        contactStatsIntent.putExtra(CONTACT, contacts.getItem(position));
        startActivity(contactStatsIntent);


//        if (null != mListener) {
//            // Notify the active callbacks interface (the activity, if the
//            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
//        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
