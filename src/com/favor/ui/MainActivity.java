package com.favor.ui;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
//import com.actionbarsherlock.view.MenuInflater;

import com.favor.R;
import com.favor.develop.Benchmark;

import data.DataHandler;
import data.Debug;

import android.app.Activity;
//import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
 
public class MainActivity extends SherlockListActivity {
 
	static final String[] MENU_ITEMS = new String[] { "List By Contacts", "Dump Database", "Test Mail"};
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return OptionsMenu.onCreateOptionsMenu(this, menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		return OptionsMenu.onOptionsItemSelected(item);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Debug.strictMode();
		DataHandler db = DataHandler.initialize(this);
		db.update();
		final Activity temp = this; //Only used for database dump toasting
		setListAdapter(new ArrayAdapter<String>(this, R.layout.entry_main,MENU_ITEMS));
 
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
 
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			    
			    String str = ((TextView) view).getText().toString();
			    if(str=="List By Contacts"){
			    	//Intent myIntent = new Intent(getBaseContext(), LoadFromContacts.class);
					//startActivity(myIntent);
					startActivity(new Intent(getBaseContext(), ContactsActivity.class));
			    }
			    else if (str=="Dump Database"){
			    	Debug.writeDatabase(temp);
			    }
			    else if (str=="Test Mail"){
			    	DataHandler sdb = DataHandler.get();
			    	//sdb.updateEmail();
			    }
//			    else if (str=="Test Query Equality"){
//			    	Debug.queryEquality();
//			    }
//			    else if (str=="Benchmark Queries"){
//			    	Benchmark.setUpDatabase();
//			    	Benchmark.multiQueryBenchmarks(150);
//			    }
//			    else if (str=="Compare Averages"){
//			    	Debug.averageTest();
//			    }
			}
			
		});
		//listView.setOnItemClickListener(new OnItemClickListener() {)
 
	}
 
}