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

    public static void exportDatabase(Context c){
        try{
            File currentDB = new File(c.getFilesDir().getAbsolutePath()+"/favor.db");
            File backupDB = new File("/mnt/sdcard/favor_export.db");

            if (currentDB.exists()){
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(c, backupDB.toString(), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(c, "Could not find "+currentDB, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(c, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
