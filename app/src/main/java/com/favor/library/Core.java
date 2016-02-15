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

import android.content.Context;
import android.content.SharedPreferences;
import com.favor.app.R;


import java.util.*;

public class Core {
    public static final String PREF_NAME = "favor_prefs";

    //Private
    private static boolean initDone = false;
    private static native void init(String databaseLocation, boolean first) throws FavorException;
    private static Context context;

    private static ArrayList<AccountManager> accounts = new ArrayList<AccountManager>();

    /*
    Both the below comparators create lists sorted highest to lowest (for count and value, respectively)s
     */

    static class AddressCountComparator implements Comparator<Address> {
        @Override
        public int compare(Address lhs, Address rhs){
            if (lhs.getCount() > rhs.getCount()) return -1;
            else if (lhs.getCount() == rhs.getCount()) return 0;
            else return 1;
        }
    }

    static class ThreadIdCountEntryComparator implements Comparator<Map.Entry<Integer, Long>> {
        @Override
        public int compare(Map.Entry<Integer, Long> lhs, Map.Entry<Integer, Long> rhs) {
            if (lhs.getValue() > rhs.getValue()) return -1;
            else if (lhs.getValue() == rhs.getValue()) return 0;
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
            Logger.error("Error creating default text manager: "+e.getMessage());
            return null;
        }
    }

    //TODO: we should probably split this method up a little bit
    private static void buildDefaultPhoneContacts(AndroidTextManager account){
        /*
        Ideally we'd work off of the most common thread IDs in the latest sent 500 messages, but that would mean the
        AndroidTextManager's addressfetching code would have to start doing thread ID work. Here we'll end up
        working off of the thread IDs of the most common addresses in latest sent 500 messages.
         */
        account.updateAddresses();
        Address[] addresses = Reader.allAddresses(false);

        HashMap<String, Address> existingAddressesByName = new HashMap<>();

        //This could very well have more addresses than the selection we got from reader; it will have all of the
        //addresses connected by thread ID to any addresses in input
        HashMap<String, Integer> addressToThreadIdMap = AndroidTextManager.mapThreadIds(addresses); 

        HashMap<Integer, ArrayList<String>> threadIdToAddressesMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : addressToThreadIdMap.entrySet()){
            if (!threadIdToAddressesMap.containsKey(entry.getValue())){
                threadIdToAddressesMap.put(entry.getValue(), new ArrayList<String>());
            }
            Logger.error("Map "+entry.getValue()+" to "+entry.getKey());
            threadIdToAddressesMap.get(entry.getValue()).add(entry.getKey());
        }


        //get total counts for thread ids by adding the counts of mapped addresses
        HashMap<Integer, Long> threadIdCount = new HashMap<>();
        for (int i=0; i < addresses.length; ++i){
            existingAddressesByName.put(addresses[i].getAddr(), addresses[i]);
            int threadId = addressToThreadIdMap.get(addresses[i].getAddr());
            threadIdCount.put(threadId, (threadIdCount.containsValue(threadId) ?
                    threadIdCount.get(threadId)+addresses[i].getCount(): addresses[i].getCount()));
        }

        //take the top 10 thread IDs, make a contact for each of them using the address with the highest count,
        //then attach any other addresses mapped to that thread ID to the same contact
        ArrayList<Map.Entry<Integer, Long>> threadIds = new ArrayList<>();
        threadIds.addAll(threadIdCount.entrySet());
        Collections.sort(threadIds, new ThreadIdCountEntryComparator());
        for (int i = 0; i < threadIds.size() && i < 10; ++i){
            Logger.error("Try and get arraylist for "+threadIds.get(i));
            ArrayList<String> correspondingAddresses = threadIdToAddressesMap.get(threadIds.get(i).getKey());

            ArrayList<Address> existingCorrespondingAddresses = new ArrayList<Address>();
            ArrayList<String> newCorrespondingAddresses = new ArrayList<String>();
            for (String addrString : correspondingAddresses){
                if (existingAddressesByName.containsKey(addrString)){
                    existingCorrespondingAddresses.add(existingAddressesByName.get(addrString));
                } else {
                    newCorrespondingAddresses.add(addrString);
                }
            }
            if (existingCorrespondingAddresses.size() == 0){
                Logger.error("Could not recover an existing corresponding address for thread ID: "+i);
            } else {
                //Sort the existing corresponding addresses by count, pick the highest to use to create a contact
                //then use all the remaining corresponding addresses (new and existing) as additional addresses for
                //the contact
                long contactId = Address.NO_CONTACT_ID;
                Collections.sort(existingCorrespondingAddresses, new AddressCountComparator()); //Sorts with larger counts at lower indices
                boolean first = true;
                for(int j = 0; j < existingCorrespondingAddresses.size(); ++j){
                    Address addr = existingCorrespondingAddresses.get(j);
                    if (first){
                        first = false;
                        try {
                            contactId = contactFromAddress(existingCorrespondingAddresses.get(j),MessageType.TYPE_ANDROIDTEXT);
                            Logger.error("Contact id :"+contactId+" for contact "+addr.getAddr());
                        }
                        catch (FavorException e){
                            Logger.error("Failed to create default contact "+addr.getAddr()+ " : "+e.getMessage());
                            //TODO: give up on this contact's other addresses to; we have nothing to map it to here
                        }
                    } else {
                        if (contactId == Address.NO_CONTACT_ID){
                            //TODO: well, that's wrong
                        } else {
                            //TODO: we have to update an existing address, which we actually need a different method for
                        }
                    }
                }
                for (int j = 0; j < newCorrespondingAddresses.size(); ++j){
                    if (contactId == Address.NO_CONTACT_ID){
                        //TODO: well, that's wrong
                    } else {
                        try{
                            Worker.createAddress(newCorrespondingAddresses.get(j), MessageType.TYPE_ANDROIDTEXT, 0, contactId);
                        } catch (FavorException e){
                            Logger.error("Faled to create secondary new address "+newCorrespondingAddresses.get(j)+" : "+e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private static long contactFromAddress(Address addr, MessageType type) throws FavorException{
        String name = AndroidHelper.contactName(addr.getAddr(), MessageType.TYPE_ANDROIDTEXT);
        if (name == null) name = addr.getAddr();
        return Worker.createContact(addr.getAddr(), type, name, true);
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

    public static String stringFromType(Core.MessageType t){
        switch(t){
            case TYPE_EMAIL: return context.getString(R.string.msg_type_email_display);
            case TYPE_ANDROIDTEXT: return context.getString(R.string.msg_type_android_display);
            case TYPE_LINE: return context.getString(R.string.msg_type_line_display);
            case TYPE_SKYPE: throw new IndexOutOfBoundsException("Type 3 (Skype) not supported on Android");
            default: throw new IndexOutOfBoundsException("Attempted to convert invalid AccountManager type");
        }
    }

    public static String formatPhoneNumber(String number){
        return number.replaceAll("[^0-9]", "");
    }

    public enum MessageType {TYPE_EMAIL, TYPE_ANDROIDTEXT, TYPE_LINE, TYPE_SKYPE}

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
                accounts.add(initial);
                buildDefaultPhoneContacts(initial);
            } else {
                //TODO: this should be pulled up from saved state, we're just working with testcode right now
                accounts.add(Reader.accountManagers()[0]);
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
