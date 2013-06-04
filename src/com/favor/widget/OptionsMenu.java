package com.favor.widget;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

//import android.app.Activity;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;

import com.favor.R;

public class OptionsMenu 
{
	public static boolean onCreateOptionsMenu(SherlockListActivity act, Menu menu) {
	    MenuInflater inflater = act.getSupportMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}

	public static boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.about:
	    	// startActivity(new Intent(this, About.class));
	    	return true;
	    case R.id.help:
	    	// startActivity(new Intent(this, Help.class));
	    	return true;
	    default:
	    	return false; //means the menu button doesn't work and the menu doesn't close
	}
		//return true;
	    //respond to menu item selection
	}
}
