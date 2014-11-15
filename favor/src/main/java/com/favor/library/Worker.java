package com.favor.library;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by josh on 10/29/14.
 */
public class Worker {
    private static native void _createContact(String address, int type, String displayName, boolean addressExists);
    public static void createContact(String address, Core.MessageType type, String displayName){
        _createContact(address, AccountManager.intFromType(type), displayName, false);
    }
    public static void createContact(String address, Core.MessageType type, String displayName, boolean addressExists){
        _createContact(address, AccountManager.intFromType(type), displayName, addressExists);
    }
}
