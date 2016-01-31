package com.favor.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.favor.library.Contact;

public class ContactStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_stats);
        Contact contact = null;
        if (getIntent().getSerializableExtra(ContactSelectFragment.CONTACT) != null){
            contact = (Contact) getIntent().getSerializableExtra(ContactSelectFragment.CONTACT);
            TextView textView = (TextView) findViewById(R.id.contact_title_text);
            textView.setText("Stats page for contact with id: "+contact.getId()+" and name "+contact.getDisplayName());
        } else {
            //TODO: no contact included, that's a problem
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
