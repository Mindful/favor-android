package com.favor.ui;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import java.util.ArrayList;

//import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.favor.widget.Contact;
import com.favor.widget.ContactArrayAdapter;
import com.favor.widget.OptionsMenu;
import com.favor.widget.ContactArrayAdapter.ContactViewHolder;
import com.favor.R;

public class ContactsActivity extends SherlockListActivity {

	private ContactArrayAdapter contactArrayAdapter;
	private MenuItem graphItem;
	
	private static ArrayList<Contact> contactsList;
	
	public static void refreshContacts(Context context)
	{
		//TODO: Eventually this (and the contacts class, together) should be able to fuse multiple
		//numbers into one contact, so that we can do contact-based queries
		//also, eventually, DataHandler should hold the contacts list - so it can come from somewhere
		//that makes sense, and we can use it internally for contact-to-number resolution
		Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
		null, null, null);
		contactsList = new ArrayList<Contact>(phones.getCount());
		while (phones.moveToNext()) 
		{
			//name, number
			contactsList.add(new Contact(phones.getString(0), phones.getString(1)));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.load_contacts);

		//ContactArrayAdapter.resetCount();
		contactArrayAdapter = new ContactArrayAdapter(this, R.layout.entry_contact, contactsList);
		contactArrayAdapter.sort();

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
