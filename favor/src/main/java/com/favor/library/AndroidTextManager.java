package com.favor.library;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by josh on 10/31/14.
 */
public class AndroidTextManager extends AccountManager{
    public AndroidTextManager(String name){
        super(name, 1);
    }

    private static Context context;

    public static void setContext(Context c){
        context = c;
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

    private ArrayList<Message> messages;

    //The old export message definition, for the record:
    //final protected void exportMessage(boolean sent, long id, long date, String address, String msg, int media)
    //c++: holdMessage(bool sent, long int id, time_t date, string address, bool media, string msg)
    private void holdMessage(boolean sent, long id, long date, String address, boolean media, String msg){
        messages.add(new Message(sent, id, date, address, media, msg));
    }


    private native void _saveMessages(int type, boolean[] sent, long[] id, long[] date, String[] address, boolean[] media, String[] msg) throws FavorException; //TODO:
    private native void _saveAddresses(int type, String[] addresses, int[] counts, String[] names);//TODO:

    private void saveMessage(){
        //Yeah, it's weird to split them up into all these different arrays, but this involves fewer JNI calls and is easier
        //to handle at the C++ layer
        boolean[] sent = new boolean[messages.size()];
        long[] id = new long[messages.size()];
        long[] date = new long[messages.size()];
        String[] address = new String[messages.size()];
        boolean[] media = new boolean[messages.size()];
        String[] msg = new String[messages.size()];
        for (int i = 0; i < messages.size(); ++i){
            sent[i] = messages.get(i).isSent();
            id[i] = messages.get(i).getId();
            date[i] = messages.get(i).getDate(); //TODO: is this how should be passed in? This might need some looking at
            address[i] = messages.get(i).getAddress();
            media[i] = messages.get(i).isMedia();
            msg[i] = messages.get(i).getMsg();
        }
        try{
            _saveMessages(type, sent, id, date, address, media, msg);
        }
        catch (FavorException e){
            //TODO: we want to know if this failed, but most of the serious error recovery should probably be at the C++
            //layer
        }

    }


    /*
    VERY BLOCKING
     */
    @Override
    public void updateAddresses(){
        try{
            HashMap<String, Integer> addressCounts = new HashMap<String, Integer>();
            HashMap<String, String> addressNames = new HashMap<String, String>();
            Cursor c = context.getContentResolver().query(SMS_IN, SMS_CONTACTS_PROJECTION, null, null, KEY_DATE + " DESC LIMIT 500");
            while (c.moveToNext()){
                addressCounts.put(c.getString(0), addressCounts.containsKey(c.getString(0)) ? addressCounts.get(c.getString(0))+1 : 1);
            }
            c.close();

            //It'd be nice if we could filter this query, but phone number formatting is dangerously inconsistent
            Cursor contacts = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null,
                    null,
                    null);
            while (contacts.moveToNext()){
                //if a number corresponds to two different addresses, we'll end up using the one we see later, but there's
                //no good way to solve that problem anyway so...
                Logger.info(contacts.getString(0)+" "+Core.formatPhoneNumber(contacts.getString(1)));
                addressNames.put(Core.formatPhoneNumber(contacts.getString(1)), contacts.getString(0));
            }
            String[] addresses = new String[addressCounts.size()];
            int[] counts = new int[addressCounts.size()];
            String[] names = new String[addressCounts.size()];

            int counter = 0;
            for (Map.Entry<String, Integer> entry : addressCounts.entrySet()){
                Logger.info("Contact: "+entry.getKey()+" Count:"+entry.getValue()+" Sugg. Name: "+addressNames.get(entry.getKey()));
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
    //TODO: needs serious testing. just copied from old favor. should probably be fine once we set up permissions and a method of keeping dates, though
    @Override
    public void updateMessages() {
        //TODO: we need to worry about which addresses we're getting information for. We don't want info for everyone anymore,
        //so like the emailManager, this needs to only pick up stuff from recognized addresses
        //We can just use the lastFetch for this because we're going on time anyway
        long lastFetchDate = 0; //TODO: get this, probably from prefs, since texts are a static issue anyway
        try {
            Cursor c = context.getContentResolver().query(SMS_IN, SMS_PROJECTION,
                    KEY_DATE + " > " + lastFetchDate, null, KEY_DATE);
            while (c.moveToNext()) {
                holdMessage(false, c.getLong(0), c.getLong(1), c.getString(2), false, c.getString(3));
            }
            c.close();

            c = context.getContentResolver().query(SMS_OUT, SMS_PROJECTION,
                    KEY_DATE + " > " + lastFetchDate, null, KEY_DATE);
            while (c.moveToNext()) {
                holdMessage(true, c.getLong(0), c.getLong(1), c.getString(2), false, c.getString(3));
            }
            c.close();

            //MMS dates are formatted retardedly, so we have to divide lastFetch accordingly
            c = context.getContentResolver().query(MMS_IN, MMS_PROJECTION,
                    KEY_DATE + " > " + lastFetchDate/1000l, null, KEY_DATE);
            while (c.moveToNext()){
                receivedMMS(c.getLong(0), c.getLong(1));
            }
            c.close();

            c = context.getContentResolver().query(MMS_OUT, MMS_PROJECTION,
                    KEY_DATE + " > " + lastFetchDate/1000l, null, KEY_DATE);
            while (c.moveToNext()){
                sentMMS(c.getLong(0), c.getLong(1));
            }
            c.close();
        } catch (Exception ex) {
            //TODO: whatever we do when the native stuff fails for the default account manager
        }
    }


    private void sentMMS(long id, long date) {
        // MMS IDs are negative to avoid overlap
        // Additionally, MMS dates must be multiplied by 1000 to work properly
        // vs SMS dates
        date = date * 1000l;
        boolean media = false;
        String type, data = "";
        Cursor c = context.getContentResolver().query(
                Uri.parse("content://mms/" + id + "/part"),
                new String[] { "_data", "text", "ct" },
                "ct<>\"application/smil\"", null, null);
        while (c.moveToNext()) {
            type = c.getString(2);
            if (type.equals("text/plain")) {
                data = c.getString(0);
                if (data == null) data = c.getString(1); // fetch from the "text" column
                else Logger.error("Unknown message data parsing sent MMS:" + data); // we have pure data
            } else media = true;
        }
        c.close();

        String filter = "(type=" + MMS_TO + " OR type=" + MMS_CC + " OR type="
                + MMS_BCC + ")";

        //The extra code here is so we can insert multiple entries for sending to multiple people
        c = context.getContentResolver().query(
                Uri.parse("content://mms/" + id + "/addr"),
                new String[] { "address" }, filter, null, null);
        if (c.getCount() > 1) {
            while (c.moveToNext()) holdMessage(true, -id, date, c.getString(0), media, data);
        } else if (c.moveToFirst()) holdMessage(true, -id, date, c.getString(0), media, data);
        c.close();
    }

    private void receivedMMS(long id, long date) {
        // MMS IDs are negative to avoid overlap
        // Additionally, MMS dates must be multiplied by 1000 to work properly
        // vs SMS dates
        date = date * 1000l;
        boolean media = false;
        String type, data = "";
        Cursor c = context.getContentResolver().query(
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
                    Logger.error("Unknown message data parsing receieved MMS:" + data); // we have
                    // pure data
                }
            } else
                media = true;
        }
        c.close();
        String filter = "type=" + MMS_FROM;
        c = context.getContentResolver().query(
                Uri.parse("content://mms/" + id + "/addr"),
                new String[] { "address" }, filter, null, null);
        c.moveToFirst();
        String address = c.getString(0);
        c.close();
        holdMessage(false, -id, date, address, media, data);
    }

}
