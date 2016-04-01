package com.favor.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.favor.library.*;

import java.util.*;

/**
 * Created by josh on 2/20/16.
 */
public class ContactAdapter extends BaseAdapter {

    public interface OnContactSelectModeListener {
        public void setContactSelectMode(boolean mode);
        public void setAdapter(ContactAdapter adapter);
    }

    private List<ContactWrapper> contacts;
    private Context context;
    private LayoutInflater inflater;
    OnContactSelectModeListener listener;

    private final int LIST_ITEM_TYPE_COUNT = 2;
    private final int LIST_ITEM_TYPE_SELECTMODE = 1;
    private final int LIST_ITEM_TYPE_NORMAL = 0;


    public ContactAdapter(Activity activity, ArrayList<Contact> inputContacts) {

        long[] sentMessageCounts = Processor.batchMessageCount(Util.TEMP_ACCOUNT_FUNCTION(), inputContacts, -1, -1, true);
        contacts = new ArrayList<ContactWrapper>();
        for (int i = 0; i < inputContacts.size(); ++i){
            ContactWrapper contactWrapper = new ContactWrapper(inputContacts.get(i));
            contactWrapper.setSentMessages(sentMessageCounts[i]);
            contactWrapper.setPhoto(AndroidHelper.contactPhoto(inputContacts.get(i)));
            contacts.add(contactWrapper);
        }




        Collections.sort(contacts, new Comparator<ContactWrapper>(){
            @Override
            public int compare(final ContactWrapper lhs, final ContactWrapper rhs){
                return (int)(rhs.getSentMessages() - lhs.getSentMessages());
            }
        });

        int photoCounter = 0;
        for (ContactWrapper wrapper : contacts){
            if (wrapper.getPhoto() == null){
                if (photoCounter % 2 == 0){
                    wrapper.setPhoto(Util.defaultContactBlue());
                } else {
                    wrapper.setPhoto(Util.defaultContactPurple());
                }
                photoCounter += 1;
            }
        }


        context = activity;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        try {
            listener = (OnContactSelectModeListener) activity;
            listener.setAdapter(this);
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+ " must implement OnContactSelectModeListener");
        }
    }

    public boolean selectMode(){
        boolean ret = false;
        for (ContactWrapper cw : contacts){
            ret = ret || cw.getSelected();
        }
        return ret;
    }

    public ArrayList<Contact> getSelectedContacts(){
        ArrayList<Contact> ret = new ArrayList<Contact>();
        for (ContactWrapper contactWrapper : contacts){
            if (contactWrapper.getSelected()){
                ret.add(contactWrapper.getContact());
            }
        }
        return ret;
    }

    public void toggleItem(int position){
        contacts.get(position).toggleSelected();
        listener.setContactSelectMode(getSelectedContacts().size() > 1);
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

    private void associateContactWrapperToCheckbox(final ContactWrapper contact, final CheckBox checkbox){
        checkbox.setChecked(contact.getSelected());
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact.toggleSelected();
                listener.setContactSelectMode(getSelectedContacts().size() > 1);

                if (!selectMode()){
                    notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        boolean currentlySelectMode = selectMode();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contact_list_element, null);
        }

        ContactWrapper wrapper = contacts.get(position);
        TextView textView = (TextView)convertView.findViewById(R.id.contact_list_element_text);
        textView.setText(wrapper.getContact().getDisplayName()); //TODO: better contact display name
        TextView subText = (TextView)convertView.findViewById(R.id.contact_list_element_subtext);
        subText.setText("Sent "+wrapper.getSentMessages()+" messages to this contact"); //TODO: strings from resource here for localization
        ImageView imageView = (ImageView)convertView.findViewById(R.id.contact_list_element_photo);
        imageView.setImageBitmap(wrapper.getPhoto());

        CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.contact_list_checkbox);
        if (currentlySelectMode){
            associateContactWrapperToCheckbox(contacts.get(position), (CheckBox)convertView.findViewById(R.id.contact_list_checkbox));
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }

        return convertView;
    }
}
