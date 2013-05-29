package com.favor.ui;
import com.favor.R;
import com.favor.util.DataHandler;
import com.favor.widget.OptionsMenu;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
 
public class MainActivity extends ListActivity {
 
	static final String[] MENU_ITEMS = new String[] { "List By Contacts", "List By Groups", "List All" };
	
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
		DataHandler db = DataHandler.initialize(this);
		db.update();
 
		setListAdapter(new ArrayAdapter<String>(this, R.layout.activity_main,MENU_ITEMS));
 
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
			}
			
		});
		//listView.setOnItemClickListener(new OnItemClickListener() {)
 
	}
 
}