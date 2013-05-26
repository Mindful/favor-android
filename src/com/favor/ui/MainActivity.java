package com.favor.ui;
import com.favor.R;
import com.favor.util.DataHandler;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
 
public class MainActivity extends ListActivity {
 
	static final String[] FRUITS = new String[] { "List By Contacts", "List By Groups", "List All" };
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.menu, menu);
	    return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.about:
	   // startActivity(new Intent(this, About.class));
	    return true;
	    case R.id.help:
	   // startActivity(new Intent(this, Help.class));
	    return true;
	    default:
	    return super.onOptionsItemSelected(item);
	}
		//return true;
	    //respond to menu item selection
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DataHandler db = DataHandler.initialize(this);
		db.update();
		//Debug.remakeDB();
		//Debug.testData("3607087506");
		//Debug.queryTest("3607087506");
		//Debug.writeDatabase(this); //with indices, this takes a LONG (minute+) time
 
		// no more this
		// setContentView(R.layout.list_fruit);
 
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_fruit,FRUITS));
 
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
 
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			    // When clicked, show a toast with the TextView text
			    
			    String str = ((TextView) view).getText().toString();
			    if(str=="List By Contacts"){
			    	//Intent myIntent = new Intent(getBaseContext(), LoadFromContacts.class);
					//startActivity(myIntent);
					startActivity(new Intent(getBaseContext(), LoadFromContacts.class));

			    Toast.makeText(getApplicationContext(),
						((TextView) view).getText(), Toast.LENGTH_SHORT).show();
			    }
			}
			
		});
		//listView.setOnItemClickListener(new OnItemClickListener() {)
 
	}
 
}