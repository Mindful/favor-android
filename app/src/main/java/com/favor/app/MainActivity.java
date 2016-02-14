package com.favor.app;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.favor.library.Core;
import com.favor.library.Debug;
import com.favor.library.Logger;
import com.favor.library.Reader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Core.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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

    public void launchContacts(View view) {
        Intent contactIntent = new Intent(this, ContactSelectActivity.class);
        startActivity(contactIntent);
    }

    public void dumpDatabase(View view){
        Debug.exportDatabase(getApplicationContext());
    }

    public void updateMessages(View view){
        Reader.accountManagers()[0].updateMessages();
    }

    public void testMethod(View view){
        Logger.info(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI.toString());
        Logger.info(Telephony.MmsSms.CONTENT_URI.toString());
        Debug.uriProperties("content://mms-sms/conversations/?simple=true", this);
    }
}
