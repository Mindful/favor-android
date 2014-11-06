package com.favor.library;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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
    private static final String MMS_CC = "130"; // 0x82 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_BCC = "129"; // 0x81 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_TO = "151"; // 0x97 in com.google.android.mms.pdu.PduHeaders
    private static final String MMS_FROM = "137"; // 0x89 in com.google.android.mms.pdu.PduHeaders

    private static final String KEY_DATE = "date"; //This is the name of the date column for texts

    //The old export message definition, for the record:
    //final protected void exportMessage(boolean sent, long id, long date, String address, String msg, int media)
    //c++: holdMessage(bool sent, long int id, time_t date, string address, bool media, string msg)
    private void holdMessage(boolean sent, long id, long date, String address, boolean media, String msg){
        //Do many things
    }



    /*
    VERY BLOCKING
     */
    @Override
    public void updateContacts(){
        //TODO: if (context == null)
    }

    /*
    VERY BLOCKING
     */
    //TODO: needs serious testing. just copied from old favor. should probably be fine once we set up permissions and a method of keeping dates, though
    @Override
    public void updateMessages() {
        //TODO: if (context == null)
        long count = 0;
        //We can just use the lastFetch for this because we're going on time anyway
        long lastFetchDate = 0; //TODO:
        try {
            Cursor c = context.getContentResolver().query(SMS_IN, SMS_PROJECTION,
                    KEY_DATE + " > " + lastFetchDate, null, KEY_DATE);
            while (c.moveToNext()) {
                holdMessage(false, c.getLong(0), c.getLong(1), c.getString(2), false, c.getString(3));
                ++count;
            }
            c.close();

            c = context.getContentResolver().query(SMS_OUT, SMS_PROJECTION,
                    KEY_DATE + " > " + lastFetchDate, null, KEY_DATE);
            while (c.moveToNext()) {
                holdMessage(true, c.getLong(0), c.getLong(1), c.getString(2), false, c.getString(3));
                ++count;
            }
            c.close();

            //MMS dates are formatted retardedly, so we have to divide lastFetch accordingly
            c = context.getContentResolver().query(MMS_IN, MMS_PROJECTION,
                    KEY_DATE + " > " + lastFetchDate/1000l, null, KEY_DATE);
            while (c.moveToNext()){
                receivedMMS(c.getLong(0), c.getLong(1));
                ++count;
            }
            c.close();

            c = context.getContentResolver().query(MMS_OUT, MMS_PROJECTION,
                    KEY_DATE + " > " + lastFetchDate/1000l, null, KEY_DATE);
            while (c.moveToNext()){
                sentMMS(c.getLong(0), c.getLong(1));
                ++count;
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
