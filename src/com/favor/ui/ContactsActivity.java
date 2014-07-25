package com.favor.ui;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

//import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.favor.ui.ContactArrayAdapter.ContactViewHolder;
import com.favor.util.Contact;
import com.favor.R;

import data.DataHandler;

public class ContactsActivity extends SherlockListActivity {

	private ContactArrayAdapter contactArrayAdapter;
	private MenuItem graphItem;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DataHandler db = DataHandler.get();
		//setContentView(R.layout.load_contacts);

		//ContactArrayAdapter.resetCount();
		contactArrayAdapter = new ContactArrayAdapter(this, R.layout.entry_contact, db.contacts());

		//ViewAnimator va = (ViewAnimator) findViewById(R.id.viewAnimator1);
		//va.setInAnimation(inFromLeftAnimation());
		//va.setOutAnimation(outToRightAnimation());

		ListView view = getListView();
		view.setAdapter(contactArrayAdapter);
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View item,
					int position, long id) {
				Contact contact = contactArrayAdapter.getItem(position);
				contact.setSelected(!contact.isSelected());
				ContactViewHolder viewHolder = (ContactViewHolder) item
						.getTag();
				viewHolder.getCheckBox().setChecked(contact.isSelected());
			}
		});

		//new PopulateContactListTask().execute();
		ContactArrayAdapter.setSingleton(contactArrayAdapter);
	}

	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.graph_button, menu);
		graphItem = menu.findItem(R.id.graph);
		graphItem.setEnabled(contactArrayAdapter.computeSelected());
		return OptionsMenu.onCreateOptionsMenu(this, menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		if (item.getItemId()==R.id.graph)
		{			
			Intent intent = new Intent(this, GraphActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		else return OptionsMenu.onOptionsItemSelected(item);
	}

	public MenuItem getGraphItem() {
		return graphItem;
	}

}
