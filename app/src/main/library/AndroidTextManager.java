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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.*;

/**
 * Created by josh on 10/31/14.
 */
public class AndroidTextManager extends AccountManager{
    public AndroidTextManager(String name){
        super(name, 1);
        messages = new ArrayList<Message>();
    }

    private static final Uri SMS_IN = Uri.parse("content://sms/inbox");
    private static final Uri SMS_OUT = Uri.parse("content://sms/sent");
    private static final Uri MMS_IN = Uri.parse("content://mms/inbox");
    private static final Uri MMS_OUT = Uri.parse("content://mms/sent");
    private static final String[] SMS_PROJECTION = { "_id", "date", "address","body" };
    private static final String[] MMS_PROJECTION = { "_id", "date" };
    private static final String[] SMS_CONTACTS_PROJECTION = {"address"};
    private static final String MMS_CC = "130"; // 0x82 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_BCC = "129"; // 0x81 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_TO = "151"; // 0x97 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_FROM = "137"; // 0x89 in com.google.android.mms.pdu.PduHeaders

    private static final String KEY_DATE = "date"; //This is the name of the date column for texts
    private static final String KEY_ADDRESS = "address";

    private static final String LAST_FETCH = "androidtext_last_fetch";

    private static final String TRACKED_ADDRESSES = "TRACKED_ADDRESSES";


    /*
    VERY BLOCKING
     */
    @Override
    public void updateAddresses(){
        try{
            HashMap<String, Integer> addressCounts = new HashMap<String, Integer>();
            HashMap<String, String> addressNames = new HashMap<String, String>();
            Cursor c = Core.getContext().getContentResolver().query(SMS_IN, SMS_CONTACTS_PROJECTION, null, null, KEY_DATE + " DESC LIMIT 500");
            while (c.moveToNext()){
                addressCounts.put(c.getString(0), addressCounts.containsKey(c.getString(0)) ? addressCounts.get(c.getString(0))+1 : 1);
            }
            c.close();

            //It'd be nice if we could filter this query, but phone number formatting is dangerously inconsistent
            Cursor contacts = Core.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null,
                    null,
                    null);
            while (contacts.moveToNext()){
                //if a number corresponds to two different addresses, we'll end up using the one we see later, but there's
                //no good way to solve that problem anyway so...
                addressNames.put(Core.formatPhoneNumber(contacts.getString(1)), contacts.getString(0));
            }
            contacts.close();
            String[] addresses = new String[addressCounts.size()];
            int[] counts = new int[addressCounts.size()];
            String[] names = new String[addressCounts.size()];

            int counter = 0;
            for (Map.Entry<String, Integer> entry : addressCounts.entrySet()){
                addresses[counter] = entry.getKey();
                counts[counter] = entry.getValue();
                names[counter] = addressNames.get(entry.getKey()); //This may be null when we don't know, which is intentional
                counter++;
            }
            _saveAddresses(type, addresses, counts, names);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    VERY BLOCKING
     */
    private void scanPhoneMessages(ArrayList<String> addrs, boolean catchup){
        if (addrs.size() == 0 ) return; //Nothing to do if no addresses to read
        HashSet<String> addrSet = new HashSet<String>();

        String addressSelection = "(";
        for (int i = 0; i < addrs.size(); ++i){
            addrSet.add(addrs.get(i));

            addressSelection += KEY_ADDRESS +"=\""+addrs.get(i)+"\"";
            if (i == addrs.size() -1) addressSelection += ")";
            else addressSelection += " OR ";
        }


        long lastFetchDate = Core.getContext().getSharedPreferences(Core.PREF_NAME, Context.MODE_PRIVATE).getLong(LAST_FETCH, 0);

        String normalSelection = addressSelection + " AND ";
        if (catchup) normalSelection += KEY_DATE + " <= " + lastFetchDate;
        else normalSelection += KEY_DATE + " > " + lastFetchDate;
        //MMS dates are formatted retardedly, so we have to divide lastFetch accordingly. Also, we can't filter our initial search on
        //addresses, so we just have to look at every MMS and immediately give up if it's not to/from someone we want.
        String MMSSelection =  KEY_DATE + " > " + lastFetchDate/1000l;

        Cursor c = Core.getContext().getContentResolver().query(SMS_IN, SMS_PROJECTION,
                normalSelection, null, KEY_DATE);
        while (c.moveToNext()) {
            holdMessage(false, c.getLong(0), c.getLong(1), c.getString(2), false, c.getString(3));
        }
        c.close();

        c = Core.getContext().getContentResolver().query(SMS_OUT, SMS_PROJECTION,
                normalSelection, null, KEY_DATE);
        while (c.moveToNext()) {
            holdMessage(true, c.getLong(0), c.getLong(1), c.getString(2), false, c.getString(3));
        }
        c.close();

        //TODO: Hopefully people don't have thousands upon thousands of MMS messages, because all we can do right now
        //is crawl them and check every one for the right address. Eventually, this might be worth rewriting

        //MMS dates are formatted retardedly, so we have to divide lastFetch accordingly
        c = Core.getContext().getContentResolver().query(MMS_IN, MMS_PROJECTION,
                MMSSelection, null, KEY_DATE);
        while (c.moveToNext()){
            receivedMMS(c.getLong(0), c.getLong(1), addrSet);
        }
        c.close();

        c = Core.getContext().getContentResolver().query(MMS_OUT, MMS_PROJECTION,
                MMSSelection, null, KEY_DATE);
        while (c.moveToNext()){
            sentMMS(c.getLong(0), c.getLong(1), addrSet);
        }
        c.close();
        saveMessages();
    }


    @Override
    public void updateMessages() {
        Logger.info("updateMessages");
        try {
            ArrayList<String> addrs = new ArrayList<String>(Arrays.asList(contactAddresses(type)));
            ArrayList<String> newAddrs = new ArrayList<String>();
            HashSet<String> trackedAddresses = new HashSet<String>(Core.getContext().getSharedPreferences(Core.PREF_NAME, Context.MODE_PRIVATE).getStringSet(TRACKED_ADDRESSES, new HashSet<String>()));
            for (int i = 0; i < addrs.size(); ++i){
                if (!trackedAddresses.contains(addrs.get(i))){
                    newAddrs.add(addrs.get(i));
                }
            }
            long lastFetchDate = Core.getContext().getSharedPreferences(Core.PREF_NAME, Context.MODE_PRIVATE).getLong(LAST_FETCH, 0);
            if (newAddrs.size() > 0 && lastFetchDate != 0){
                scanPhoneMessages(newAddrs, true);
                Logger.info("backfetch for "+newAddrs.size()+"new addresses");
            }
            scanPhoneMessages(addrs, false);
            for (int i = 0; i < addrs.size(); ++i){
                trackedAddresses.add(addrs.get(i));
            }
            Core.getContext().getSharedPreferences(Core.PREF_NAME, Context.MODE_PRIVATE).edit().
                    putLong(LAST_FETCH, new Date().getTime()).putStringSet(TRACKED_ADDRESSES, trackedAddresses).commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            //TODO: whatever we do when the native stuff fails for the default account manager
        }
    }


    //TODO: the new sent and received MMS methods have had basically no testing, and really need some more thorough
    //testing but... I got a new phone right before switching countries. Alas, I am MMS-less


    private void sentMMS(long id, long date, Set<String> addrSet) {
        // MMS IDs are negative to avoid overlap
        // Additionally, MMS dates must be multiplied by 1000 to work properly
        // vs SMS dates


        ArrayList<String> addrs = new ArrayList<String>();
        String filter = "(type=" + MMS_TO + " OR type=" + MMS_CC + " OR type="
                + MMS_BCC + ")";

        //The extra code here is so we can insert multiple entries for sending to multiple people
        Cursor c = Core.getContext().getContentResolver().query(
                Uri.parse("content://mms/" + id + "/addr"),
                new String[] { "address" }, filter, null, null);
        if (c.getCount() > 1) {
            while (c.moveToNext()){
                if (addrSet.contains(c.getString(0))) addrs.add(c.getString(0));
            }
        } else if (c.moveToFirst() && addrSet.contains(c.getString(0))) addrs.add(c.getString(0));
        c.close();

        if (addrs.size() == 0) return; //No reason to keep parsing if there are no good addresses

        date = date * 1000l;
        boolean media = false;
        String type, data = "";
        c = Core.getContext().getContentResolver().query(
                Uri.parse("content://mms/" + id + "/part"),
                new String[] { "_data", "text", "ct" },
                "ct<>\"application/smil\"", null, null);
        while (c.moveToNext()) {
            type = c.getString(2);
            if (type.equals("text/plain")) {
                data = c.getString(0);
                if (data == null) data = c.getString(1); // fetch from the "text" column
                else Logger.error("Unknown message data parsing sent MMS:" + data); //TODO: we have pure data
            } else media = true;
        }
        c.close();

        for (String addr : addrs) holdMessage(true, -id, date, addr, media, data);
    }

    private void receivedMMS(long id, long date, Set<String> addrSet) {
        // MMS IDs are negative to avoid overlap
        // Additionally, MMS dates must be multiplied by 1000 to work properly
        // vs SMS dates
        date = date * 1000l;
        boolean media = false;
        String type, data = "";

        String filter = "type=" + MMS_FROM;
        Cursor c = Core.getContext().getContentResolver().query(
                Uri.parse("content://mms/" + id + "/addr"),
                new String[] { "address" }, filter, null, null);
        c.moveToFirst();
        String address = c.getString(0);
        if (!addrSet.contains(address)) return; //No reason to keep parsing if this isn't a relevant address
        c.close();


        c = Core.getContext().getContentResolver().query(
                Uri.parse("content://mms/" + id + "/part"),
                new String[] { "_data", "text", "ct" },
                "ct<>\"application/smil\"", null, null);
        while (c.moveToNext()) {
            type = c.getString(2);
            if (type.equals("text/plain")) {
                data = c.getString(0);
                if (data == null) {
                    data = c.getString(1); // fetch from the "text" column
                } else {
                    Logger.error("Unknown message data parsing receieved MMS:" + data); //TODO: we have pure data
                }
            } else
                media = true;
        }
        c.close();
        holdMessage(false, -id, date, address, media, data);
    }

}
