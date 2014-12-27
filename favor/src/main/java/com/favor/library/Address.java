package com.favor.library;

/**
 * Created by josh on 12/27/14.
 */
public class Address {
    private String addr;
    private long contactId;
    private Core.MessageType type;

    public String getAddr() {
        return addr;
    }

    public long getContactId() {
        return contactId;
    }

    public Core.MessageType getType() {
        return type;
    }

    public Address(String addr, long contactId, Core.MessageType type) {
        this.addr = addr;
        this.contactId = contactId;
        this.type = type;
    }
}
