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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidHelper {
    private static class AndroidContactData{
        long id;
        String displayName;
        String contactAddress;

        private AndroidContactData(String displayName, long id, String contactAddress) {
            this.displayName = displayName;
            this.id = id;
            this.contactAddress = contactAddress;
        }
    }


    static Map<Core.MessageType, List<AndroidContactData>> contactsHash;

    //TODO: eventually this could be more generalizeable to any types of contacts we were looking for, and possibly more efficient or even just less wonky
    public static void populateContacts() {
        contactsHash = new HashMap<Core.MessageType, List<AndroidContactData>>();
        for (Core.MessageType type : Core.MessageType.values()){
            contactsHash.put(type, new ArrayList<AndroidContactData>());
        }
        //Android Text type population, as well as contacts by ID
        Cursor contacts = Core.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}, null, null, null);
        while (contacts.moveToNext()) {
            List<AndroidContactData> typeContactListData = contactsHash.get(Core.MessageType.TYPE_ANDROIDTEXT);
            AndroidContactData data = new AndroidContactData(contacts.getString(2), contacts.getLong(1),
                    Core.formatPhoneNumber(contacts.getString(0)));
            typeContactListData.add(data);
        }
        contacts.close();

        //Email population
        contacts = Core.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Email.ADDRESS,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Email.DISPLAY_NAME}, null, null, null);
        while (contacts.moveToNext()) {
            List<AndroidContactData> typeContactListData = contactsHash.get(Core.MessageType.TYPE_EMAIL);
            AndroidContactData data = new AndroidContactData(contacts.getString(2), contacts.getLong(1), contacts.getString(0));
            String suggestedName = contactName(data.contactAddress, Core.MessageType.TYPE_EMAIL);
            if (data.displayName == null && suggestedName != null) data.displayName = suggestedName;
            typeContactListData.add(data);
        }
        contacts.close();
    }


    public static Bitmap contactPhoto(Contact contact){
        for (Address addr : contact.getAddresses()){
            Bitmap photo = contactPhoto(addr.getAddr(), addr.getType());
            if (photo != null){
                return photo;
            }
        }
        Logger.info("Photo for "+contact.getDisplayName()+" is null");
        return null;
    }

    public static Bitmap contactPhoto(String address, Core.MessageType type){
        AndroidContactData contactData = findContactData(address, type);
        if (contactData != null){
            return seekPhoto(address, contactData.id);
        }
        else return null;
    }

    public static Bitmap contactPhoto(String address){
        for (Core.MessageType type : contactsHash.keySet()){
            AndroidContactData contactData = findContactData(address, type);
            if (contactData != null) return seekPhoto(address, contactData.id);
        }
        return null;
    }

    public static String contactName(String address){
        for (Core.MessageType type : contactsHash.keySet()){
            AndroidContactData contactData = findContactData(address, type);
            if (contactData != null) return contactData.displayName;
        }
        return null;
    }

    public static String contactName(String address, Core.MessageType type){
        AndroidContactData contactData = findContactData(address, type);
        if (contactData != null){
            return contactData.displayName;
        } else return null;
    }

    public static AndroidContactData findContactData(String address, Core.MessageType type){
        if (type == Core.MessageType.TYPE_ANDROIDTEXT){
            for (AndroidContactData data : contactsHash.get(type)){
                if (phoneNumberEquality(address, data.contactAddress)) return data;
            }
        } else {
            for (AndroidContactData data : contactsHash.get(type)){
                if (address.equals(data.contactAddress)) return data;
            }
        }
        return null;
    }

    public static boolean phoneNumberEquality(String lhs, String rhs) {
        //Both of these numbers have to have been formatted for this to work properly
        if (lhs.matches(Core.PHONE_NUMBER_ILLEGAL_CHARS)){
            Logger.error("Illegally formatted phone number used in comparison: "+lhs);
            throw new FavorRuntimeException("Illegally formatted phone number used in comparison: "+lhs); //TODO: probably shouldn't have these exceptions
        } else if (rhs.matches(Core.formatPhoneNumber(Core.PHONE_NUMBER_ILLEGAL_CHARS))){
            Logger.error("Illegally formatted phone number used in comparison: "+rhs);
            throw new FavorRuntimeException("Illegally formatted phone number used in comparison: "+rhs); //TODO: probably shouldn't have these exceptions
        } else {
            boolean match = false;
            if (endSubstringMatch(lhs, rhs)) match = true;
            if (endSubstringMatch(rhs, lhs)) match = true;
            return match;
        }
    }

    private static boolean endSubstringMatch(String parentString, String childString){
        if (parentString.length() < childString.length()){
            return false;
        } else {
            return childString.equals(parentString.substring(parentString.length() - childString.length(), parentString.length()));
        }
    }


    private static Bitmap seekPhoto(String address, long id){
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(Core.getContext().getContentResolver(), uri);
        if (input == null) return null;
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        return bitmap;
    }



}
