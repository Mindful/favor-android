package com.favor.widget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.favor.ui.LoadFromContacts;
import com.favor.R;

@SuppressWarnings("serial")
public class ContactArrayAdapter extends ArrayAdapter<Contact> implements
		Serializable {

	private static ContactArrayAdapter singleton;

	private final Context context;
	private final int viewResourceId;
	private final ArrayList<Contact> contacts;
	private final LayoutInflater inflater;

	private int selected;

	public ContactArrayAdapter(Context context, int viewResourceId,
			ArrayList<Contact> contacts) {
		super(context, viewResourceId, contacts);
		this.context = context;
		this.viewResourceId = viewResourceId;
		this.contacts = contacts;

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		selected = 0;
	}

	public void sort() {
		Collections.sort(contacts, new Comparator<Contact>() {

			@Override
			public int compare(Contact lhs, Contact rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}

		});
	}

	public boolean isAtleastOneSelected() {
		for (Contact c : contacts)
			if (c.isSelected())
				return true;
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Contact contact = contacts.get(position);

		CheckBox checkBox;
		TextView name;
		TextView address;

		if (convertView == null) {
			convertView = inflater.inflate(viewResourceId, null);
			checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
			name = (TextView) convertView.findViewById(R.id.contact_name);
			address = (TextView) convertView.findViewById(R.id.contact_address);

			convertView.setTag(new ContactViewHolder(checkBox, name, address));

			checkBox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					Contact contact = (Contact) cb.getTag();
					contact.setSelected(cb.isChecked());

					if (cb.isChecked())
						selected++;
					else
						selected--;

					MenuItem graphItem = ((LoadFromContacts) context)
							.getGraphItem();
					graphItem.setEnabled(isAtleastOneSelected());

					String text = context.getString(R.string.graph);

					if (selected > 0)
						text += " (" + selected + ")";

					graphItem.setTitle(text);
				}
			});
		} else {
			ContactViewHolder viewHolder = (ContactViewHolder) convertView
					.getTag();
			checkBox = viewHolder.getCheckBox();
			name = viewHolder.getName();
			address = viewHolder.getAddress();
		}

		checkBox.setTag(contact);
		checkBox.setChecked(contact.isSelected());
		name.setText(contact.getName());
		address.setText(contact.getAddress());

		return convertView;
	}

	public List<Contact> getContacts() {
		return contacts;
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
