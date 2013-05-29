package com.favor.ui;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewAnimator;

import com.favor.widget.Contact;
import com.favor.widget.ContactArrayAdapter;
import com.favor.widget.OptionsMenu;
import com.favor.widget.ContactArrayAdapter.ContactViewHolder;
import com.favor.R;

public class LoadFromContacts extends ListActivity {

	private ContactArrayAdapter contactArrayAdapter;
	private MenuItem graphItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.load_contacts);

		contactArrayAdapter = new ContactArrayAdapter(this,
				R.layout.contact, new ArrayList<Contact>());

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
		//optimize me
		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				null, null, null);

		while (phones.moveToNext()) {
			String name = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String number = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			contactArrayAdapter.add(new Contact(name, number));
		}
		ContactArrayAdapter.setSingleton(contactArrayAdapter);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return OptionsMenu.onCreateOptionsMenu(this, menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		return OptionsMenu.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSearchRequested() {
		/**
		 * TODO finish search
		 */
		if (true)
			return false;

		ViewAnimator va = (ViewAnimator) findViewById(R.id.viewAnimator1);
		if (va.getDisplayedChild() != 1)
			return false;

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.search_dialog);
		dialog.setTitle("Search Contact");

		dialog.show();

		return true;
	}

	public MenuItem getGraphItem() {
		return graphItem;
	}

}
