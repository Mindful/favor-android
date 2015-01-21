package com.favor.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.favor.R;
import com.favor.library.Contact;
import com.favor.library.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by josh on 12/27/14.
 */
public class ContactDisplayAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ContactDisplay> contacts;
    private HashMap<Long, Boolean> selected;
    private LayoutInflater inflater;

    public ArrayList<ContactDisplay> getDisplays(){
        return contacts;
    }

    public ContactDisplayAdapter(Context c, ArrayList<ContactDisplay> input, HashMap<Long, Boolean> selected){
        context = c;
        contacts = input;
        inflater = LayoutInflater.from(context);
        this.selected = selected;
    }

    public HashMap<Long, Boolean> getSelected(){
        return selected;
    }

    @Override
    public int getCount(){
        return contacts.size();
    }

    @Override
    public Object getItem(int position){
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position){
        return contacts.get(position).getId();
    }

    @Override
    public boolean hasStableIds(){
        return true;
    }

    public void toggleItem(int position){
        Logger.info("Toggle item "+position);
        selected.put(contacts.get(position).getId(), !selected.get(contacts.get(position).getId()));
        notifyDataSetChanged();
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
        if (selected.get(contacts.get(position).getId())) {
            picture.setColorFilter(Color.argb(200, 255, 50, 255));
        } else {
            picture.clearColorFilter();
        }
        TextView name = (TextView)imv.getTag(R.id.text);

        name.setText(contacts.get(position).getName());
        if (contacts.get(position).hasImage()) picture.setImageBitmap(contacts.get(position).getImg());
        else picture.setImageResource(R.drawable.contact_default);

        return imv;
    }

}
