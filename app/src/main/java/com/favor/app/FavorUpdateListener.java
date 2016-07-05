package com.favor.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutCompat;
import com.favor.library.Core;

import java.util.Date;

/**
 * Created by josh on 4/9/16.
 */
public abstract class FavorUpdateListener extends FavorActivity {

    public static String FAVOR_UPDATE_INTENT = "favor-update";

    private long lastUpdate = 0;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver(){
        @Override
        public  void onReceive(Context context, Intent intent){
            activeUpdate();
            lastUpdate = new Date().getTime();
        }
    };

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
               updateReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (lastUpdate < Core.getLastUpdate()){
            update();
            lastUpdate = new Date().getTime();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateReceiver, new IntentFilter(FAVOR_UPDATE_INTENT));
        super.onResume();
    }



    protected abstract void update();

    protected void activeUpdate(){
        update();
    }





}
