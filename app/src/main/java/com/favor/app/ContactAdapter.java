package com.favor.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.favor.library.Contact;
import com.favor.library.Logger;

import java.util.ArrayList;

/**
 * Created by josh on 2/20/16.
 */
public class ContactAdapter extends BaseAdapter {



    private ArrayList<ContactWrapper> contacts;
    private Context context;
    private LayoutInflater inflater;

    private final int LIST_ITEM_TYPE_COUNT = 2;
    private final int LIST_ITEM_TYPE_SELECTMODE = 1;
    private final int LIST_ITEM_TYPE_NORMAL = 0;


    public ContactAdapter(Context inputContext, ArrayList<Contact> inputContacts) {
        contacts = new ArrayList<ContactWrapper>();
        for (Contact contact : inputContacts){
            contacts.add(new ContactWrapper(contact));
        }
        context = inputContext;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public boolean selectMode(){
        boolean ret = false;
        for (ContactWrapper cw : contacts){
            ret = ret || cw.getSelected();
        }
        return ret;
    }

    public void toggleItem(int position){
        contacts.get(position).toggleSelected();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (!contacts.get(position).getSelected()){
            return LIST_ITEM_TYPE_NORMAL;
        } else {
            return LIST_ITEM_TYPE_SELECTMODE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return LIST_ITEM_TYPE_COUNT;
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Contact getItem(int position) {
        return contacts.get(position).getContact();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void associateContactWrapper(final ContactWrapper contact, final CheckBox checkbox){
        checkbox.setChecked(contact.getSelected());
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact.toggleSelected();

                if (!selectMode()){
                    notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        boolean currentSelectMode = selectMode();
        int type = currentSelectMode ? LIST_ITEM_TYPE_SELECTMODE : LIST_ITEM_TYPE_NORMAL;
        TextView textView;
        if (convertView == null || (Boolean)(convertView.getTag(R.id.contact_tag_id)) != currentSelectMode) {
            switch(type) {
                case LIST_ITEM_TYPE_NORMAL:
                    convertView = inflater.inflate(R.layout.contact_list_element, null);
                    break;
                case LIST_ITEM_TYPE_SELECTMODE:
                    convertView = inflater.inflate(R.layout.contact_list_element_selectmode, null);
                    associateContactWrapper(contacts.get(position), (CheckBox)convertView.findViewById(R.id.contact_list_element_text));
                    break;
            }
            textView = (TextView)convertView.findViewById(R.id.contact_list_element_text);
            convertView.setTag(R.id.contact_tag_id, currentSelectMode);
        } else {
            textView = (TextView)convertView.findViewById(R.id.contact_list_element_text);
            if (currentSelectMode){
                associateContactWrapper(contacts.get(position), (CheckBox)convertView.findViewById(R.id.contact_list_element_text));
            }
        }
        textView.setText(contacts.get(position).getContact().getDisplayName()); //TODO: do this better
        return convertView;
    }
}
