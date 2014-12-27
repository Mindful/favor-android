package com.favor.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.favor.R;
import com.favor.library.Contact;
import com.favor.library.Logger;

import java.util.ArrayList;

/**
 * Created by josh on 12/27/14.
 */
public class ContactDisplayAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ContactDisplay> contacts;

    public ArrayList<ContactDisplay> getDisplays(){
        return contacts;
    }

    public ContactDisplayAdapter(Context c, ArrayList<ContactDisplay> input){
        context = c;
        contacts = input;
    }

    public int getCount(){
        return contacts.size();
    }

    public Object getItem(int position){
        return contacts.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ImageView imv;
        if (convertView == null){
            imv = new ImageView(context);
            imv.setLayoutParams(new GridView.LayoutParams(85, 85));
            imv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imv.setPadding(8, 8, 8, 8);
        } else {
            imv = (ImageView) convertView;
        }

        if (contacts.get(position).hasImage()) imv.setImageBitmap(contacts.get(position).getImg());
        else imv.setImageResource(R.drawable.contact_default);
        return imv;
    }

}
