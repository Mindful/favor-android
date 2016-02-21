package com.favor.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import com.favor.library.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 2/20/16.
 */
public class ContactAdapter extends BaseAdapter {



    private ArrayList<Contact> contacts = new ArrayList<>();
    private Context context;

    public ContactAdapter(Context inputContext, ArrayList<Contact> inputContacts) {
        contacts = inputContacts;
        context = inputContext;
        //mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final Contact item) {
        contacts.add(item);
        notifyDataSetChanged();
    }

//    @Override
//    public int getItemViewType(int position) {
//        if(position < LIST_ITEM_TYPE_1_COUNT)
//            return LIST_ITEM_TYPE_1;
//        else
//            return LIST_ITEM_TYPE_2;
//    }

//    @Override
//    public int getViewTypeCount() {
//        return LIST_ITEM_TYPE_COUNT;
//    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Contact getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder = null;
//        int type = getItemViewType(position);
//        if (convertView == null) {
//            holder = new ViewHolder();
//            switch(type) {
//                case LIST_ITEM_TYPE_1:
//                    convertView = mInflater.inflate(R.layout.list_item_type1, null);
//                    holder.textView = (TextView)convertView.findViewById(R.id.list_item_type1_text_view);
//                    break;
//                case LIST_ITEM_TYPE_2:
//                    convertView = mInflater.inflate(R.layout.list_item_type2, null);
//                    holder.textView = (TextView)convertView.findViewById(R.id.list_item_type2_button);
//                    break;
//            }
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder)convertView.getTag();
//        }
//        holder.textView.setText(mData.get(position));
//        return convertView;
//    }
}
