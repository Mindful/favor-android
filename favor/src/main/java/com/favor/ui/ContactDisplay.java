package com.favor.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.widget.ImageView;
import com.favor.library.*;

import java.util.ArrayList;

/**
 * Created by josh on 12/27/14.
 */
public class ContactDisplay {
    public String getName() {
        return contact.getDisplayName();
    }

    @Override
    public String toString() {
        return contact.getDisplayName();
    }

    public long getCharCountSent() {
        return charCountSent;
    }

    public long getCharCountReceived() {
        return charCountReceived;
    }

    private Contact contact;
    private long charCountSent;
    private long charCountReceived;

    private Bitmap img = null;

    public boolean hasImage(){
        return img != null;
    }

    public Bitmap getImg(){
        return img;
    }

    public static ArrayList<ContactDisplay> buildDisplays(Contact[] input){
        ArrayList<ContactDisplay> ret = new ArrayList<ContactDisplay>(input.length);

        //TODO: Eventually this will need to look for associations with any other address types we support (aside from just email and phone #s)
        Cursor contacts = Core.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Email.ADDRESS},
                null,
                null,
                null);
        while (contacts.moveToNext()){
            Logger.info(contacts.getString(0)+"-"+contacts.getString(1)+"-"+contacts.getString(2));
        }

        contacts = Core.getContext().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, new String[] {
                ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
                ContactsContract.Profile.LOOKUP_KEY,
                ContactsContract.Profile.PHOTO_THUMBNAIL_URI},
                null,
                null,
                null);

        for (int i = 0; i < input.length; ++i){
            long sent = Processor.totalCharcount(Core.getCurrentAccount(), input[i], -1, -1, true);
            long rec =  Processor.totalCharcount(Core.getCurrentAccount(), input[i], -1, -1, false);
            ContactDisplay create = new ContactDisplay(input[i], sent, rec, null);
            ret.add(create);
        }

        return ret;
    }

    private ContactDisplay(Contact contact, long charCountSent, long charCountReceived, Bitmap img) {
        this.contact = contact;
        this.charCountSent = charCountSent;
        this.charCountReceived = charCountReceived;
        this.img = img;
    }
}
