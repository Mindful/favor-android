package com.favor.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.favor.R;

import java.util.ArrayList;

/**
 * Created by josh on 12/27/14.
 */
public class ContactDisplayAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ContactDisplay> contacts;
    private LayoutInflater inflater;

    public ArrayList<ContactDisplay> getDisplays(){
        return contacts;
    }

    public ContactDisplayAdapter(Context c, ArrayList<ContactDisplay> input){
        context = c;
        contacts = input;
        inflater = LayoutInflater.from(context);
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
        View imv;
        if (convertView == null){
            imv = inflater.inflate(R.layout.contact_item, parent, false);
            imv.setTag(R.id.picture, imv.findViewById(R.id.picture));
            imv.setTag(R.id.text, imv.findViewById(R.id.text));
            //imv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imv = convertView;
        }

        ImageView picture = (ImageView)imv.getTag(R.id.picture);
        TextView name = (TextView)imv.getTag(R.id.text);

        name.setText(contacts.get(position).getName());
        if (contacts.get(position).hasImage()) picture.setImageBitmap(contacts.get(position).getImg());
        else picture.setImageResource(R.drawable.contact_default);

        return imv;
    }

}
