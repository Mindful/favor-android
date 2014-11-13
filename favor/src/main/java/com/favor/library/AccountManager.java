package com.favor.library;

import android.accounts.Account;

/**
 * Created by josh on 10/31/14.
 */
public class AccountManager {
    protected int type;
    protected String accountName;
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

    protected native String[] contactAddresses(int type) throws FavorException;
    protected native void _saveMessages(int type, boolean[] sent, long[] id, long[] date, String[] address, boolean[] media, String[] msg) throws FavorException;
    protected native void _saveAddresses(int type, String[] addresses, int[] counts, String[] names);

    public void TESTMETHOD(){
        try {
            String[] t = contactAddresses(type);
            Logger.info("Test method contact count:"+t.length);
            for( String s: t){
                Logger.info("Contact: "+t);
            }
        } catch (FavorException e) {
            e.printStackTrace();
        }
    }


    //Blocks
    public void updateAddresses(){
        try{
            _update(accountName, type, true);
        } catch (FavorException e){
            e.printStackTrace(); //TODO: something else
        }
    }


    //Blocks
    public void updateMessages(){
        try{
            _update(accountName, type, false);
        } catch (FavorException e){
            e.printStackTrace(); //TODO: something else
        }
    }


    //true if we want to updateAddresses(), false otherwise. Two methods would've just been extra code
    private native void _update(String name, int typ, boolean addresses) throws FavorException;

    public void destroy() throws FavorException {
        //Would be a substantial pain to translate the enumeration at the C++ layer, so we do it here
        _destroy(accountName, type);
    }

    private native void _destroy(String name, int typ) throws FavorException;

    public static AccountManager create(String name, Core.MessageType type, String detailsJson) throws FavorException {
        _create(name, intFromType(type), detailsJson);
        if (type==Core.MessageType.TYPE_ANDROIDTEXT) return new AndroidTextManager(name);
        else return new AccountManager(name, intFromType(type));
    }

    private native static void _create(String name, int type, String detailsJson) throws FavorException;
}
