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


public class main extends FavorActivity {

    //Must be declared in this activity to play nice with the XMl activity declaration
    public void beginClick(View view){
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

}
