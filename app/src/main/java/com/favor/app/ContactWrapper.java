package com.favor.app;

import com.favor.library.Address;
import com.favor.library.Contact;

import java.util.ArrayList;

/**
 * Created by josh on 2/21/16.
 */
public class ContactWrapper {
    private Contact contact;
    private boolean selected;

    public ContactWrapper(Contact inputContact){
        contact = inputContact;
        selected = false;
    }

    public Contact getContact(){
        return contact;
    }


    public String getAddressListString() {
        StringBuilder str = new StringBuilder("(");
        ArrayList<Address> addresses = contact.getAddresses();
        for (int i = 0; i < addresses.size(); ++i){
            str.append(addresses.get(i));
            if (i < addresses.size() -1 ) {
                str.append(", ");
            }
        }
        str.append(")");
        return str.toString();
    }

    public boolean getSelected(){
        return selected;
    }

    public void toggleSelected(){
        selected = !selected;
    }

    @Override
    public String toString() {
        return "ContactWrapper{" +
                "contact=" + contact +
                ", selected=" + selected +
                '}';
    }
}
