package com.favor;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.favor.library.*;
import com.favor.ui.ContactDisplay;
import com.favor.ui.ContactDisplayAdapter;

import java.util.ArrayList;

/**
 * Created by josh on 12/27/14.
 */
public class contacts extends FavorActivity  {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        AndroidHelper.populateContacts();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270){
            gridview.setNumColumns(3);
        } else {
            gridview.setNumColumns(2);
        }

        final ContactDisplayAdapter adapter = new ContactDisplayAdapter(this, ContactDisplay.buildDisplays(Reader.contacts()));
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ContactDisplay disp = (ContactDisplay) adapter.getItem(position);
                Toast.makeText(contacts.this, "Click "+disp.getName()+". Rec: "+disp.getCharCountReceived()+ " Sent: "+disp.getCharCountSent(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}