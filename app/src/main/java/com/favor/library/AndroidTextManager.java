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

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;

import java.util.*;

/**
 * Created by josh on 10/31/14.
 */
public class AndroidTextManager extends AccountManager{
    public AndroidTextManager(String name){
        super(name, 1);
        messages = new ArrayList<Message>();
    }

    private static final boolean TELEPHONY_API_AVAILABLE = android.os.Build.VERSION.SDK_INT >= 19;
    private static final Uri SMS_IN = TELEPHONY_API_AVAILABLE ? Telephony.Sms.Inbox.CONTENT_URI : Uri.parse("content://sms/inbox");
    private static final Uri SMS_OUT = TELEPHONY_API_AVAILABLE ? Telephony.Sms.Sent.CONTENT_URI : Uri.parse("content://sms/sent");
    private static final Uri MMS_IN = TELEPHONY_API_AVAILABLE ? Telephony.Mms.Inbox.CONTENT_URI : Uri.parse("content://mms/inbox");
    private static final Uri MMS_OUT = TELEPHONY_API_AVAILABLE ? Telephony.Mms.Sent.CONTENT_URI : Uri.parse("content://mms/sent");
    private static final String KEY_DATE = TELEPHONY_API_AVAILABLE ? Telephony.TextBasedSmsColumns.DATE : "date"; //This is the name of the date column for texts
    private static final String KEY_ADDRESS = TELEPHONY_API_AVAILABLE ? Telephony.TextBasedSmsColumns.ADDRESS : "address";
    private static final String KEY_THREAD_ID = TELEPHONY_API_AVAILABLE ? Telephony.TextBasedSmsColumns.THREAD_ID : "thread_id";
    private static final String KEY_BODY = TELEPHONY_API_AVAILABLE ? Telephony.TextBasedSmsColumns.BODY : "body";
    private static final String KEY_ID = BaseColumns._ID;
    private static final String[] SMS_PROJECTION = { KEY_ID, KEY_DATE, KEY_ADDRESS, KEY_BODY};
    private static final String[] MMS_PROJECTION = { KEY_ID, KEY_DATE };
    private static final String[] SMS_CONTACTS_PROJECTION = {KEY_ADDRESS};
    private static final String MMS_CC = "130"; // 0x82 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_BCC = "129"; // 0x81 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_TO = "151"; // 0x97 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_FROM = "137"; // 0x89 in com.google.android.mms.pdu.PduHeaders



    private static final String LAST_FETCH = "androidtext_last_fetch";

    private static final String TRACKED_ADDRESSES = "TRACKED_ADDRESSES";

    private static final long MS_ADJUSTMENT = 1000l;


    private void countThreadIdsAndAssignAddresses(Uri uri, HashMap<String, Integer> addressCountMap,
                                                  HashMap<String, Integer> addressToThreadIdMap){
        Cursor c = Core.getContext().getContentResolver().query(uri, new String[]{KEY_THREAD_ID, KEY_ADDRESS}, null, null,
                KEY_DATE + " DESC LIMIT 500");
        int currentThreadId;
        String currentAddress;
        while (c != null && c.moveToNext()){
            currentThreadId = c.getInt(0);
            currentAddress = c.getString(1);
            Logger.error(currentAddress);
            addressCountMap.put(currentAddress,
                    addressCountMap.containsValue(currentAddress) ? addressCountMap.get(currentAddress) +1 : 1);
            addressToThreadIdMap.put(currentAddress, currentThreadId);
        }
        c.close();
    }

    private void countMmsThreadIdsAndAssignAddresses(Uri uri, HashMap<String, Integer> addressCountMap,
                                                  HashMap<String, Integer> addressToThreadIdMap){
        Cursor c = Core.getContext().getContentResolver().query(uri, new String[]{KEY_THREAD_ID, KEY_ID}, null, null,
                KEY_DATE + " DESC LIMIT 500");
        int currentThreadId;
        int mmsId;
        String currentAddress;
        while (c != null && c.moveToNext()){
            currentThreadId = c.getInt(0);
            mmsId = c.getInt(1);
            String filter = "type=" + MMS_FROM;
            Cursor innerC = Core.getContext().getContentResolver().query(
                    Uri.parse("content://mms/" + mmsId + "/addr"),
                    new String[] { "address" }, filter, null, null);
            if (!innerC.moveToFirst()){
                Logger.info("Could not get MMS address with id "+mmsId);
                continue;
            }

            currentAddress = innerC.getString(0);
            Logger.error(currentAddress);
            addressCountMap.put(currentAddress,
                    addressCountMap.containsValue(currentAddress) ? addressCountMap.get(currentAddress) +1 : 1);
            addressToThreadIdMap.put(currentAddress, currentThreadId);
        }
        c.close();
    }



    private void retainThreadIdInformation(HashMap<String, Integer> addressToThreadIdMap,
                                           HashMap<Integer, Integer> threadIdToCountMap) {
        //TODO: preserve input somehow; probably android specific. we'll need to load it when we reload this class
    }

    /*
    VERY BLOCKING
     */
    @Override
    public void updateAddresses(){
        try{
            HashMap<String, Integer> addressCountMap = new HashMap<String, Integer>();
            HashMap<String, Integer> addressToThreadIdMap = new HashMap<String, Integer>();
            //TODO: we actually need to crawl received as well, for address variety reasons. We don't have to use the
            //TODO: messages we find in sent for counts, but we need to scan them anyway purely to associate thread IDs and addresses

            //TODO: if we don't count them though, gonna have to carefully rewrite some code so we use uncounted mappings (in one hashmap and not the other)
            countThreadIdsAndAssignAddresses(SMS_OUT, addressCountMap, addressToThreadIdMap);
            countMmsThreadIdsAndAssignAddresses(MMS_OUT, addressCountMap, addressToThreadIdMap);


            HashMap<String, String> addressNames = new HashMap<String, String>();
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

            String[] addresses = new String[addressCountMap.size()];
            int[] counts = new int[addressCountMap.size()];
            String[] names = new String[addressCountMap.size()];
            HashMap<Integer, Integer> threadIdToCountMap = new HashMap<Integer, Integer>();

            Logger.error("WOOOOOOO " + addressCountMap.size() + " addresses");
            int counter = 0;
            for (Map.Entry<String, Integer> entry : addressCountMap.entrySet()){
                Logger.error(entry.getKey());
                addresses[counter] = entry.getKey();
                counts[counter] = entry.getValue();
                names[counter] = addressNames.get(entry.getKey()); //This may be null when we don't know, which is intentional
                counter++;

                int threadId = addressToThreadIdMap.get(entry.getKey());
                threadIdToCountMap.put(threadId,
                        addressCountMap.containsValue(threadId) ? addressCountMap.get(threadId) +1 : 1);
            }
            retainThreadIdInformation(addressToThreadIdMap, threadIdToCountMap);
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
        Logger.info(normalSelection); //TODO: deleteme
        normalSelection = "1 = 1";
        //MMS dates are formatted retardedly, so we have to divide lastFetch accordingly. Also, we can't filter our initial search on
        //addresses, so we just have to look at every MMS and immediately give up if it's not to/from someone we want.
        String MMSSelection =  KEY_DATE + " > " + lastFetchDate/MS_ADJUSTMENT;

        Cursor c = Core.getContext().getContentResolver().query(SMS_IN, SMS_PROJECTION,
                normalSelection, null, KEY_DATE);
        while (c.moveToNext()) {
            holdMessage(false, c.getLong(0), c.getLong(1)/MS_ADJUSTMENT, c.getString(2), false, "THREAD_ID:"+c.getInt(4)+":"+c.getString(3));
        }
        c.close();

        c = Core.getContext().getContentResolver().query(SMS_OUT, SMS_PROJECTION,
                normalSelection, null, KEY_DATE);
        while (c.moveToNext()) {
            holdMessage(true, c.getLong(0), c.getLong(1)/MS_ADJUSTMENT, c.getString(2), false, "THREAD_ID:"+c.getInt(4)+":"+c.getString(3));
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
        } else if (c.moveToFirst() && addrSet.contains(c.getString(0))) {
            addrs.add(c.getString(0));
        }
        c.close();

        if (addrs.size() == 0) return; //No reason to keep parsing if there are no good addresses

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
        boolean media = false;
        String type, data = "";

        String filter = "type=" + MMS_FROM;
        Cursor c = Core.getContext().getContentResolver().query(
                Uri.parse("content://mms/" + id + "/addr"),
                new String[] { "address" }, filter, null, null);
        if (!c.moveToFirst()){
            Logger.info("Could not get MMS with id "+id);
            return;
        }
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
