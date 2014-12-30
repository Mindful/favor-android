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

    public static ArrayList<ContactDisplay> buildDisplays(ArrayList<Contact> input){
        ArrayList<ContactDisplay> ret = new ArrayList<ContactDisplay>(input.size());

        long[] sentCharCounts = Processor.batchMessageCount(Core.getCurrentAccount(), input, -1, -1, true);
        long[] recCharCounts = Processor.batchMessageCount(Core.getCurrentAccount(), input, -1, -1, false);

        for (int i = 0; i < input.size(); ++i){
            ContactDisplay create = new ContactDisplay(input.get(i), sentCharCounts[i], recCharCounts[i], AndroidHelper.contactPhoto(input.get(i)));
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
