package com.favor.library;

import com.favor.ui.ContactDisplay;

import java.util.ArrayList;

/**
 * Created by josh on 12/27/14.
 */
public class Contact {

    private long id;
    private String displayName;
    ArrayList<Address> addresses;

    public String getDisplayName() {
        return displayName;
    }

    public long getId() {
        return id;
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }

    public Contact(long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        addresses = new ArrayList<Address>();
    }

    public Contact(long id, String displayName, String[] addrs, long[] addrIds, int[] addrTypes) {
        this.id = id;
        this.displayName = displayName;
        addresses = new ArrayList<Address>();
        for (int i = 0; i < addrs.length; ++i){
            addresses.add(new Address(addrs[i], addrIds[i], Core.typeFromInt(addrTypes[i])));
        }
    }

    //TODO: this'd be much cleaner just using flags like we do at the C++ level
    public boolean hasType (Core.MessageType type){
        for (Address address : addresses){
            if (address.getType() == type) return true;
        }
        return false;
    }


}
