package com.favor.library;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by josh on 11/15/14.
 */
public class Debug {
    public static void uriProperties(String uri, Context act)
    {
        HashMap<Integer, String> e = new HashMap<Integer, String>();
        String nullType = "Null type";
        e.put(0, nullType);
        e.put(1, "Integer type");
        e.put(2, "Float type");
        e.put(3, "String type");
        e.put(4, "Blob type");

        HashMap<String, String> cols = new HashMap<String, String>();

        Uri u = Uri.parse(uri);
        Cursor c = act.getContentResolver().query(u, null, null, null, null);

        while(c.moveToNext())
        {
            for (int i = 0; i < c.getColumnCount(); i++)
            {
                String colName = c.getColumnName(i).toString();
                String colType = e.get(c.getType(i));

                if (cols.get(colName)==null){cols.put(colName, colType);}
                else if (cols.get(colName)==nullType && colType != nullType){cols.put(colName, colType);}
            }
        }
        int i = 0;
        for (Map.Entry<String, String> entry : cols.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            Logger.info(uri + " Column-Type " + i + " -:- "+ key + " - " + value);
            i++;
        }
        c.close();

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
