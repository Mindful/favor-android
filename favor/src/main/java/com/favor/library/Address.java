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
