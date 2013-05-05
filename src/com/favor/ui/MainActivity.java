package com.favor.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewAnimator;

import com.favor.util.DataHandler;
import com.favor.util.Misc;
import com.favor.widget.Contact;
import com.favor.widget.ContactArrayAdapter;
import com.favor.widget.ContactArrayAdapter.ContactViewHolder;
import com.favor.R;

public class MainActivity extends Activity {

	private ContactArrayAdapter contactArrayAdapter;
	private MenuItem graphItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		contactArrayAdapter = new ContactArrayAdapter(this,
				R.layout.contact_list, new ArrayList<Contact>());

		ViewAnimator va = (ViewAnimator) findViewById(R.id.viewAnimator1);
		va.setInAnimation(inFromLeftAnimation());
		va.setOutAnimation(outToRightAnimation());

		DataHandler db = DataHandler.initialize(this);
		db.update();
		Log.v("test", Misc.formatAddress("(619) 908-2292"));
		ListView view = (ListView) findViewById(R.id.contactList);
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

		new PopulateContactListTask().execute();
		ContactArrayAdapter.setSingleton(contactArrayAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		graphItem = menu.findItem(R.id.graph);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.graph:
			Intent intent = new Intent(this, GraphBarActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		return true;
	}

	@SuppressWarnings("unused")
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

	private Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(500);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}

	private Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(500);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}

	final class PopulateContactListTask extends AsyncTask<Void, Contact, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
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
				publishProgress(new Contact(name, number));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ViewAnimator va = (ViewAnimator) MainActivity.this
					.findViewById(R.id.viewAnimator1);

			contactArrayAdapter.sort();
			va.setDisplayedChild(1);
		}

		@Override
		protected void onProgressUpdate(Contact... values) {
			contactArrayAdapter.add(values[0]);
		}

	}
}
