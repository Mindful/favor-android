package com.favor.library;

import android.accounts.Account;

/**
 * Created by josh on 10/31/14.
 */
public class AccountManager {
    private Core.MessageType type;
    private String accountName;
    private AccountManager(){}

    public Core.MessageType getType(){return type;}
    public String getAccountName(){return accountName;}

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

    public AccountManager(String name, int t){
        accountName = name;
        type = typeFromInt(t);
    }



    public native void fetch() throws FavorException; //TODO: this is also overriden for AndroidTextManagers

    public native void delete() throws FavorException; //TODO:






    public static native AccountManager create(String name, int type, String detailsJson) throws FavorException;

    public static AccountManager createAndroidTextManager(String name){
        return new AndroidTextManager(name);
    }
}
