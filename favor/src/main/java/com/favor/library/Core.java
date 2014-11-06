package com.favor.library;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by josh on 10/29/14.
 */
public class Core {
    public static final String PREF_NAME = "favor_prefs";

    //Private
    private static boolean initDone = false;
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

    public static void testMethod(Activity acc){
        AccountManager[] test = Reader.accountManagers();
        Log.v("FAVOR DEBUG OUTPUT", "AccountManager count "+test.length);
        for (int i = 0; i < test.length; ++i){
            Log.v("FAVOR DEBUG OUTPUT", "Deleting "+test[i].getAccountName());
            try{
                test[i].destroy();
            } catch (FavorException e){
                Log.v("FAVOR DEBUG EXCEPTION", e.getMessage());
            }
        }
    }

    public static void initialize(Activity acc){
        if (initDone) return;
        SharedPreferences prefs = acc.getSharedPreferences(PREF_NAME, acc.MODE_PRIVATE);
        boolean first = prefs.getBoolean("first", true);
        try {
            init(acc.getFilesDir().getAbsolutePath(), first);
            if (first) buildDefaultTextManager(acc);
            prefs.edit().putBoolean("first", false).commit();
            initDone = true;
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
