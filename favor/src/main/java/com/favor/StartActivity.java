package com.favor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.favor.library.*;


public class StartActivity extends FavorActivity {

    //Must be declared in this activity to play nice with the XMl activity declaration
    public void beginClick(View view){
        Intent intent = new Intent(this, CoreActivity.class);
        startActivity(intent);
        //Core.buildDefaultTextManager(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Button startButton = (Button) findViewById(R.id.begin);
        //startButton.setOnClickListener();
        Core.initialize(getApplicationContext());

    }

    @Override
    public void messageRefreshResponse(){
    }

    @Override
    public void addressRefreshResponse() {
    }

}
