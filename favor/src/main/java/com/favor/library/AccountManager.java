package com.favor.library;

import android.accounts.Account;

/**
 * Created by josh on 10/31/14.
 */
public class AccountManager {
    private int type;
    private String accountName;
    private AccountManager(){}

    public Core.MessageType getType(){return typeFromInt(type);}
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
        type = t;
    }

    private native String[] contactAddresses() throws FavorException;

    //true if we want to updateContacts(), false otherwise. Two methods would've just been extra code
    private native void _update(boolean contacts) throws FavorException;

    private native void _destroy() throws FavorException;

    public void destroy() throws FavorException {
        //Would be a substantial pain to translate the enumeration at the C++ layer, so we do it here
        _destroy();
    }

    /*
    VERY BLOCKING
     */
    public void updateContacts(){
        try{
            _update(true);
        } catch (FavorException e){

        }
    }

    /*
    VERY BLOCKING
     */
    public void updateMessages(){
        try{
            _update(false);
        } catch (FavorException e){

        }
    }






    public static native AccountManager create(String name, int type, String detailsJson) throws FavorException;

    public static AccountManager createAndroidTextManager(String name){
        return new AndroidTextManager(name);
    }
}
