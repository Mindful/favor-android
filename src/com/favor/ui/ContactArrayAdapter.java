package com.favor.ui;

//import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
//import com.actionbarsherlock.view.MenuInflater;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.favor.util.Contact;
import com.favor.R;

@SuppressWarnings("serial")
public class ContactArrayAdapter extends ArrayAdapter<Contact> implements
		Serializable {

	private static ContactArrayAdapter singleton;
	private final Context context;
	private final int viewResourceId;
	private final List<Contact> contacts;
	private final LayoutInflater inflater;

	private int selected;
	
	public final String buttonText;
	

	public ContactArrayAdapter(Context context, int viewResourceId,
			List<Contact> contacts) {
		super(context, viewResourceId, contacts);
		buttonText = context.getString(R.string.graph);
		this.context = context;
		this.viewResourceId = viewResourceId;
		this.contacts = contacts;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		selected = 0;
	}

	public boolean computeSelected() {
		boolean oneSelected = false;
		selected = 0;
		for (Contact c : contacts)
			if (c.isSelected())
			{
				oneSelected = true;
				selected++;
			}
		MenuItem graphItem = ((ContactsActivity) context).getGraphItem();
		graphItem.setTitle(buttonText + " (" + selected + ")");
		return oneSelected;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Contact contact = contacts.get(position);

		CheckBox checkBox;
		TextView name;
		TextView address;

		if (convertView == null) {
			convertView = inflater.inflate(viewResourceId, null);
			checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
			name = (TextView) convertView.findViewById(R.id.contact_name);
			address = (TextView) convertView.findViewById(R.id.contact_address);

			convertView.setTag(new ContactViewHolder(checkBox, name, address));

			checkBox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					Contact contact = (Contact) cb.getTag();
					contact.setSelected(cb.isChecked());

					MenuItem graphItem = ((ContactsActivity) context).getGraphItem();
					graphItem.setEnabled(computeSelected());
				}
			});
		} else {
			ContactViewHolder viewHolder = (ContactViewHolder) convertView.getTag();
			checkBox = viewHolder.getCheckBox();
			name = viewHolder.getName();
			address = viewHolder.getAddress();
		}

		checkBox.setTag(contact);
		checkBox.setChecked(contact.isSelected());
		name.setText(contact.getName());
		address.setText(contact.displayAddress());

		return convertView;
	}

	public List<Contact> getContacts() {
		return contacts;
	}
	
	public int getSelected()
	{
		return selected;
	}

	public static class ContactViewHolder {
		private final CheckBox checkBox;
		private final TextView name;
		private final TextView address;

		ContactViewHolder(CheckBox checkBox, TextView name, TextView address) {
			this.checkBox = checkBox;
			this.name = name;
			this.address = address;
		}

		public CheckBox getCheckBox() {
			return checkBox;
		}

		public TextView getName() {
			return name;
		}

		public TextView getAddress() {
			return address;
		}
		

	}

	public static void setSingleton(ContactArrayAdapter singleton) {
		ContactArrayAdapter.singleton = singleton;
	}

	public static ContactArrayAdapter getSingleton() {
		return singleton;
	}
}