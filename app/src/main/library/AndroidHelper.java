/*
 * Copyright (C) 2015  Joshua Tanner (mindful.jt@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.favor.library;


import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
    static HashMap<Long, AndroidContactData> contactsByID;

    //TODO: eventually this could be more generalizeable to any types of contacts we were looking for, and possibly more efficient or even just less wonky
    public static void populateContacts() {
        contactsHash = new HashMap<Core.MessageType, HashMap<String, AndroidContactData>>();
        contactsByID = new HashMap<Long, AndroidContactData>();
        for (Core.MessageType type : Core.MessageType.values()){
            contactsHash.put(type, new HashMap<String, AndroidContactData>());
        }
        //Android Text type population, as well as contacts by ID
        Cursor contacts = Core.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}, null, null, null);
        while (contacts.moveToNext()) {
            HashMap<String, AndroidContactData> targetHash = contactsHash.get(Core.MessageType.TYPE_ANDROIDTEXT);
            AndroidContactData data = new AndroidContactData(contacts.getString(2), contacts.getLong(1));
            targetHash.put(Core.formatPhoneNumber(contacts.getString(0)), data);
            contactsByID.put(data.id, data);
        }
        contacts.close();

        //Email population
        contacts = Core.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Email.ADDRESS,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Email.DISPLAY_NAME}, null, null, null);
        while (contacts.moveToNext()) {
            HashMap<String, AndroidContactData> targetHash = contactsHash.get(Core.MessageType.TYPE_EMAIL);
            AndroidContactData data = new AndroidContactData(contacts.getString(2), contacts.getLong(1));
            if (data.displayName == null && contactsByID.containsKey(data.id)) data.displayName = contactsByID.get(data.id).displayName;
            targetHash.put(contacts.getString(0), data);
        }
        contacts.close();
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
