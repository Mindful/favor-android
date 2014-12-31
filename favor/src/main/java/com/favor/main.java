package com.favor;

import android.accounts.Account;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.favor.library.*;

import java.util.ArrayList;


public class main extends ActionBarActivity {

    //Must be declared in this activity to play nice with the XMl activity declaration
    public void beginClick(View view){
//        Address[] addrs = Reader.allAddresses(false);
//        for (int  i =0; i < addrs.length; ++i){
//            Address addr = addrs[i];
//            Logger.info("Address "+addr.getAddr()+" count:"+addr.getCount());
//        }


        //TODO: this shouldn't be here, it should be somewhere else
        AccountManager[] accounts = Reader.accountManagers();
        for (int i = 0; i < accounts.length; ++i){
            accounts[i].updateMessages();
        }
        Intent intent = new Intent(this, contacts.class);
        startActivity(intent);
        //Core.buildDefaultTextManager(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = (Button) findViewById(R.id.begin);
        //startButton.setOnClickListener();
        Core.initialize(getApplicationContext());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, settings.class));
        } else if (id == R.id.action_dumpdb){
           Debug.exportDatabase(this);
        } else if (id == R.id.action_refresh){
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_LONG);
        }
        return super.onOptionsItemSelected(item);
    }
}
