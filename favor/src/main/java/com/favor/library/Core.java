package com.favor.library;

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by josh on 10/29/14.
 */
public class Core {
    //Private
    private static native void init(String databaseLocation, boolean first) throws FavorException;

    private static void buildDefaultTextManager(Activity acc){
        TelephonyManager tm = (TelephonyManager) acc.getSystemService(acc.getApplicationContext().TELEPHONY_SERVICE);
        String phoneNumber = tm.getLine1Number();
        if (phoneNumber != null){
            try {
                Log.v("FAVOR DEBUG OUTPUT", "Create with phone number "+phoneNumber);
                AccountManager.create(phoneNumber, 1, "{}");
            }
            catch (FavorException e){
             //Couldn't build default account
            }
        }
        //Cursor c = acc.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);

    }


    //Public

    public static enum MessageType {TYPE_EMAIL, TYPE_ANDROIDTEXT, TYPE_LINE, TYPE_SKYPE}
    public static native String helloWorld(); //This is just here for testing


    public static void initialize(Activity acc){
        //TODO: check and see if this is the first time we're runnin the application
        boolean first = true;
        try {
            init(acc.getFilesDir().getAbsolutePath(), first);
            if (first) buildDefaultTextManager(acc);
            //TODO: below this is test code
            AccountManager[] test = Reader.accountManagers();
            Log.v("FAVOR DEBUG OUTPUT", "AccountManager count "+test.length);
            for (int i = 0; i < test.length; ++i){
                Log.v("FAVOR DEBUG OUTPUT", test[i].getAccountName());
            }
        } catch (FavorException e) {
            e.printStackTrace();
            //TODO: log something or display something to the user
        }
    }

    public static native void cleanup();


    static {
        System.loadLibrary("favor");
    }
}
