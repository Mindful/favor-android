package com.favor.library;

import java.io.Serializable;

/**
 * Created by josh on 12/27/14.
 */
public class Address implements Serializable {
    private String addr;
    private long contactId;
    private long count;
    private Core.MessageType type;

    public String getAddr() {
        return addr;
    }

    public long getContactId() {
        return contactId;
    }

    public long getCount() {return count; }

    public Core.MessageType getType() {
        return type;
    }

    public Address(String addr, long contactId, Core.MessageType type) {
        this.addr = addr;
        this.contactId = contactId;
        this.type = type;
    }

    //We need this to call from the C++ layer, because using types down there'd be a mess
    public Address(String addr, long count, long contactId, int type){
        this.addr = addr;
        this.contactId = contactId;
        this.type = Core.typeFromInt(type);
        this.count = count;
    }
}
