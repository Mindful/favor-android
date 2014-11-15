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
