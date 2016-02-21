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


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by josh on 12/27/14.
 */
public class Contact implements Serializable {

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

    public Contact(long id, String displayName, ArrayList<Address> addrs) {
        this.id = id;
        this.displayName = displayName;
        addresses = addrs;
    }

    //TODO: this'd be much cleaner just using flags like we do at the C++ level
    public boolean hasType (Core.MessageType type){
        for (Address address : addresses){
            if (address.getType() == type) return true;
        }
        return false;
    }

    public String getAddressListString() {
        StringBuilder str = new StringBuilder("(");
        for (int i = 0; i < addresses.size(); ++i){
            str.append(addresses.get(i));
            if (i < addresses.size() -1 ) {
                str.append(", ");
            }
        }
        str.append(")");
        return str.toString();
    }

    @Override
    public String toString() {
        return "Contact{" +
                "displayName='" + displayName + '\'' +
                ", id=" + id +
                '}';
    }
}
