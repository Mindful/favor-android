package com.favor.library;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by josh on 10/29/14.
 */
public class Reader {

    public static native AccountManager[] accountManagers();

    //TODO: if handing the contacts straight up proves slow, we can pass up large arrays of just addresses
    //and do all the actual contact building at the java layer
    public static ArrayList<Contact> contacts(){
        //Get addresses, bind them appropriately to each contact
        Contact[] emptyContacts = _contacts();
        ArrayList<Contact> filledContacts = new ArrayList<Contact>();
        HashMap<Long, ArrayList<Address>> contactMap = new HashMap<Long, ArrayList<Address>>();
        Address[] addrs = allAddresses(true);

        for (int i = 0; i < emptyContacts.length; ++i){
            contactMap.put(emptyContacts[i].getId(), new ArrayList<Address>());
        }

        for (int i = 0; i < addrs.length; ++i){
            contactMap.get(addrs[i].getContactId()).add(addrs[i]);
        }

        for (int i = 0; i < emptyContacts.length; ++i){
            filledContacts.add(new Contact(emptyContacts[i].getId(), emptyContacts[i].getDisplayName(), contactMap.get(emptyContacts[i].getId())));
        }

        return filledContacts;
    }

    private static native Contact[] _contacts();

    //public static native Address[] addresses();
    public static native Address[] allAddresses(boolean contactRelevantOnly);

}
