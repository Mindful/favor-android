package com.favor.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.favor.library.Contact;
import com.favor.library.Debug;
import com.favor.library.Reader;

public class ContactSelectFragment extends ListFragment {

    private ContactAdapter adapter;
    public final static String CONTACT = "CONTACT";
    public final static String CONTACT_LIST = "CONTACT_LIST";


    public ContactSelectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ContactAdapter(getActivity(), Reader.contacts());
        setListAdapter(adapter);

    }

    @Override
    public void onStart(){
        super.onStart();
//        getListView().setDivider(null);
//        getListView().setDividerHeight(0);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.toggleItem(i);
                return true;
            }
        });
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent contactStatsIntent = new Intent(getActivity(), ContactStatsActivity.class);
        contactStatsIntent.putExtra(CONTACT, adapter.getItem(position));
        startActivity(contactStatsIntent);

    }

}
