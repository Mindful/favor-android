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

import java.util.ArrayList;

/**
 * Created by josh on 10/31/14.
 */
public class AccountManager {
    protected int type;
    protected String accountName;
    protected ArrayList<Message> messages; //Unused in most cases, but should still live here
    private AccountManager(){}

    public Core.MessageType getType(){return Core.typeFromInt(type);}
    public String getAccountName(){return accountName;}

    public AccountManager(String name, int t){
        accountName = name;
        type = t;
    }

    protected native String[] contactAddresses(int type) throws FavorException;
    protected native void _saveMessages(int type, String name, boolean[] sent, long[] id, long[] date, String[] address, boolean[] media, String[] msg) throws FavorException;
    protected native void _saveAddresses(int type, String[] addresses, int[] counts, String[] names) throws FavorException;

    protected void saveMessages(){
        //Yeah, it's weird to split them up into all these different arrays, but this involves fewer JNI calls and is easier
        //to handle at the C++ layer
        if (messages.size() == 0 ) return;
        boolean[] sent = new boolean[messages.size()];
        long[] id = new long[messages.size()];
        long[] date = new long[messages.size()];
        String[] address = new String[messages.size()];
        boolean[] media = new boolean[messages.size()];
        String[] msg = new String[messages.size()];
        for (int i = 0; i < messages.size(); ++i){
            sent[i] = messages.get(i).isSent();
            id[i] = messages.get(i).getId();
            date[i] = messages.get(i).getDate();
            address[i] = messages.get(i).getAddress();
            media[i] = messages.get(i).isMedia();
            msg[i] = messages.get(i).getMsg();
        }
        messages.clear();
        try{
            _saveMessages(type, accountName, sent, id, date, address, media, msg);
        }
        catch (FavorException e){
            e.printStackTrace();
            //TODO: we want to know if this failed, but most of the serious error recovery should probably be at the C++
            //layer
        }

    }

    //The old export message definition, for the record:
    //final protected void exportMessage(boolean sent, long id, long date, String address, String msg, int media)
    //c++: holdMessage(bool sent, long int id, time_t date, string address, bool media, string msg)
    protected void holdMessage(boolean sent, long id, long date, String address, boolean media, String msg){
        messages.add(new Message(sent, id, date, address, media, msg));
        Logger.info(new Message(sent, id, date, address, media, msg).toString());//TODO:TESTCODE
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
        _create(name, Core.intFromType(type), detailsJson);
        if (type==Core.MessageType.TYPE_ANDROIDTEXT) return new AndroidTextManager(name);
        else return new AccountManager(name, Core.intFromType(type));
    }

    private native static void _create(String name, int type, String detailsJson) throws FavorException;
}
