package com.favor.library;


import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.HashMap;

public class AndroidHelper {
    private static class AndroidContactData{
        long id;
        String displayName;

        private AndroidContactData(String displayName, long id) {
            this.displayName = displayName;
            this.id = id;
        }
    }


    static HashMap<Core.MessageType, HashMap<String, AndroidContactData>> contactsHash;

    //TODO: eventually this could be more generalizeable to any types of contacts we were looking for, and possibly more efficient
    public static void populateContacts() {
        contactsHash = new HashMap<Core.MessageType, HashMap<String, AndroidContactData>>();
        for (Core.MessageType type : Core.MessageType.values()){
            contactsHash.put(type, new HashMap<String, AndroidContactData>());
        }
        //Android Text type population
        Cursor contacts = Core.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}, null, null, null);
        while (contacts.moveToNext()) {
            contactsHash.get(Core.MessageType.TYPE_ANDROIDTEXT).put(Core.formatPhoneNumber(contacts.getString(0)),
                    new AndroidContactData(contacts.getString(2), contacts.getLong(1)));
        }

        //Email population
        contacts = Core.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Email.ADDRESS,
                        ContactsContract.CommonDataKinds.Email._ID,
                        ContactsContract.CommonDataKinds.Email.DISPLAY_NAME}, null, null, null);
        while (contacts.moveToNext()) {
            contactsHash.get(Core.MessageType.TYPE_ANDROIDTEXT).put(Core.formatPhoneNumber(contacts.getString(0)),
                    new AndroidContactData(contacts.getString(2), contacts.getLong(1)));
        }
    }


    public static Bitmap contactPhoto(Contact contact){
        for (Address addr : contact.getAddresses()){
            Bitmap photo = contactPhoto(addr.getAddr(), addr.getType());
            if (photo != null) return photo;
        }
        return null;
    }

    public static Bitmap contactPhoto(String address, Core.MessageType type){
        if (contactsHash.get(type).containsKey(address)){
            return seekPhoto(address, contactsHash.get(type).get(address).id);
        }
        else return null;
    }

    public static Bitmap contactPhoto(String address){
        for (HashMap<String, AndroidContactData> map : contactsHash.values()){
            if (map.containsKey(address)) return seekPhoto(address, map.get(address).id);
        }
        return null;
    }

    public static String contactName(String address){
        for (HashMap<String, AndroidContactData> map : contactsHash.values()){
            if (map.containsKey(address)) return map.get(address).displayName;
        }
        return null;
    }

    public static String contactName(String address, Core.MessageType type){
        if (contactsHash.get(type).containsKey(address)){
            return contactsHash.get(type).get(address).displayName;
        } else return null;
    }


    private static Bitmap seekPhoto(String address, long id){
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(Core.getContext().getContentResolver(), uri);
        if (input == null) return null;
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        return bitmap;
    }



}
