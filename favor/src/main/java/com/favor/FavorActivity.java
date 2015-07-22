/*
 * Copyright (C) 2015  Joshua Tanner (mindful.jt@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.favor;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.favor.library.Core;
import com.favor.library.Debug;
import com.favor.library.Processor;

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
        Processor.clearCache();
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
