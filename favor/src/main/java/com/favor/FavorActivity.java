package com.favor;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.favor.library.Core;
import com.favor.library.Debug;

/**
 * Created by josh on 12/31/14.
 */
public abstract class FavorActivity extends ActionBarActivity implements RefreshResponder {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void refreshMessages(){
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_LONG);
        //TODO: this should be more complex, and display more to the user (like a loading swirl), it should also reload the activity in most cases
        //or call some sort of method that must be overriden so activties know how to reload themselves on refresh
        Core.getCurrentAccount().updateMessages();
        messageRefreshResponse();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.action_dumpdb){
            Debug.exportDatabase(this);
        } else if (id == R.id.action_refresh){
            refreshMessages();
        }
        return super.onOptionsItemSelected(item);
    }
}
