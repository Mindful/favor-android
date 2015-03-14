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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.favor.RefreshResponder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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
        Core.currentAccount = currentAccount;
        //TODO: this may mean we need to do lots of other things, depending on how/where it's called
    }

    private static AccountManager currentAccount;

    static class AddressComparator implements Comparator<Address> {
        @Override
        public int compare(Address lhs, Address rhs){
            if (lhs.getCount() > rhs.getCount()) return -1;
            else if (lhs.getCount() == rhs.getCount()) return 0;
            else return 1;
        }
    }


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
        ArrayList<Address> addrs = new ArrayList<Address>(Arrays.asList(Reader.allAddresses(false)));
        Collections.sort(addrs, new AddressComparator());

        try{
            for (int i = 0; i < addrs.size() && i < 15; ++i){
                String name = AndroidHelper.contactName(addrs.get(i).getAddr(), MessageType.TYPE_ANDROIDTEXT);
                if (name == null) name = addrs.get(i).getAddr();
                Worker.createContact(addrs.get(i).getAddr(), MessageType.TYPE_ANDROIDTEXT, name, true);
            }
        }
        catch (FavorException e){
            Logger.error("Error creating default contacts");
        }
    }


    //Public

    public static Core.MessageType typeFromInt(int i){
        switch(i) {
            case 0: return Core.MessageType.TYPE_EMAIL;
            case 1: return Core.MessageType.TYPE_ANDROIDTEXT;
            case 2: return Core.MessageType.TYPE_LINE;
            case 3: throw new IndexOutOfBoundsException("Type 3 (Skype) not supported on Android");
            default: throw new IndexOutOfBoundsException("Attempted to convert invalid AccountManager type");
        }
    }
    public static int intFromType(Core.MessageType t){
        switch(t){
            case TYPE_EMAIL: return 0;
            case TYPE_ANDROIDTEXT: return 1;
            case TYPE_LINE: return 2;
            case TYPE_SKYPE: return 3;
            default: throw new IndexOutOfBoundsException("Attempted to convert invalid AccountManager type");
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
        try {
            init(c.getFilesDir().getAbsolutePath(), first);
            AndroidHelper.populateContacts();
            if (first){
                AndroidTextManager initial = buildDefaultTextManager(c);
                setCurrentAccount(initial);
                buildDefaultPhoneContacts(initial);
            } else {
                //TODO: this should be pulled up from saved state, we're just working with testcode right now
                setCurrentAccount(Reader.accountManagers()[0]);
            }
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
