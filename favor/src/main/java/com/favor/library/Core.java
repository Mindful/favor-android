package com.favor.library;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Core {
    public static final String PREF_NAME = "favor_prefs";

    //Private
    private static boolean initDone = false;
    private static native void init(String databaseLocation, boolean first) throws FavorException;
    private static Context context;


    public static AccountManager getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(AccountManager currentAccount) {
        currentAccount = currentAccount;
        //TODO: this may mean we need to do lots of other things, depending on how/where it's called
    }

    private static AccountManager currentAccount;




    public static Context getContext(){
        return context;
    }

    private static AndroidTextManager buildDefaultTextManager(Context c){
        try {
            return (AndroidTextManager) AccountManager.create("Phone", MessageType.TYPE_ANDROIDTEXT, "{}");
        }
        catch (FavorException e){
            Logger.error("Error creating default text manager");
            return null;
        }
    }

    private static void buildDefaultPhoneContacts(AndroidTextManager account){
            account.updateAddresses();
            //TODO: get addresses
    }


    //Public

    public static Core.MessageType typeFromInt(int i){
        switch(i) {
            case 0: return Core.MessageType.TYPE_EMAIL;
            case 1: return Core.MessageType.TYPE_ANDROIDTEXT;
            case 2: return Core.MessageType.TYPE_LINE;
            case 3: throw new IndexOutOfBoundsException("Type 3 (Skype) not supported on Android");
            default: throw new IndexOutOfBoundsException("Invalid AccountManager type");
        }
    }
    public static int intFromType(Core.MessageType t){
        switch(t){
            case TYPE_EMAIL: return 0;
            case TYPE_ANDROIDTEXT: return 1;
            case TYPE_LINE: return 2;
            case TYPE_SKYPE: return 3;
            default: throw new IndexOutOfBoundsException("Invalid AccountManager type");
        }
    }

    public static String formatPhoneNumber(String number){
        return number.replaceAll("[^0-9]", "");
    }

    public static enum MessageType {TYPE_EMAIL, TYPE_ANDROIDTEXT, TYPE_LINE, TYPE_SKYPE}

    /**
     *     Input here should be the application context so we can use it whenever we want
     */
    public static void initialize(Context c){
        if (initDone) return;
        context = c;
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, c.MODE_PRIVATE);
        boolean first = prefs.getBoolean("first", true);
        Logger.info("First: "+first); //TODO: testcode, just make sure it's saving properly
        try {
            init(c.getFilesDir().getAbsolutePath(), first);
            if (first){
                AndroidTextManager initial = buildDefaultTextManager(c);
                buildDefaultPhoneContacts(initial);
            }
            AndroidHelper.populateContacts();
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
