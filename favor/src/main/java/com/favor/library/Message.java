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

/**
 * Created by josh on 11/7/14.
 */
public class Message {
    private boolean sent;
    private long id;
    private long date;
    private String address;
    private boolean media;
    private String msg;

    public Message(boolean sent, long id, long date, String address, boolean media, String msg){
        this.sent = sent;
        this.id = id;
        this.date = date;
        this.address = address;
        this.media = media;
        this.msg = msg;
    }

    public boolean isSent() {
        return sent;
    }

    public long getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public String getAddress() {
        return address;
    }

    public boolean isMedia() {
        return media;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString(){
        String result = "[Message ID: "+id+" | Sent? " + sent+" | Date: "+date+" | Address: "+address;
        result += " | Media? "+media+ " | Body Length: "+msg.length()+ "| Body: <<"+ msg+">>]";
        return result;
    }



}
