package com.favor.library;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
    private static Context context;


    public static Context getContext(){
        return context;
    }

    public static void buildDefaultTextManager(Context c){
        TelephonyManager tm = (TelephonyManager) c.getSystemService(c.getApplicationContext().TELEPHONY_SERVICE);
        String phoneNumber = tm.getLine1Number();
        if (phoneNumber != null){
            try {
                Log.v("FAVOR DEBUG OUTPUT", "Create with phone number "+phoneNumber);
                AccountManager.create("shiggy"+phoneNumber, MessageType.TYPE_ANDROIDTEXT, "{}");
            }
            catch (FavorException e){
               // e.printStackTrace();
                Logger.info("creation error");
            }
        }
        //Cursor c = acc.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);

    }


    //Public

    public static String formatPhoneNumber(String number){
        return number.replaceAll("[^0-9]", "");
    }

    public static enum MessageType {TYPE_EMAIL, TYPE_ANDROIDTEXT, TYPE_LINE, TYPE_SKYPE}

    public static void testMethod(Activity acc){
        //Worker.exportDatabase(acc);
        try{
            AccountManager delme = AccountManager.create("DELME", MessageType.TYPE_ANDROIDTEXT, "{}");
            delme.destroy();
        } catch (FavorException e){
            e.printStackTrace();
        }

        AccountManager[] test = Reader.accountManagers();
        Log.v("FAVOR DEBUG OUTPUT", "AccountManager count "+test.length);
        for (int i = 0; i < test.length; ++i){
            test[i].updateMessages();
        }
    }

    /**
     *     Input here should be the application context so we can use it whenever we want
     */
    public static void initialize(Context c){
        if (initDone) return;
        context = c;
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, c.MODE_PRIVATE);
        boolean first = prefs.getBoolean("first", true);
        try {
            init(c.getFilesDir().getAbsolutePath(), first);
            if (first) buildDefaultTextManager(c);
            prefs.edit().putBoolean("first", false).commit(); //TODO: is this saving properly?
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
