package com.favor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

//Important!! This is the only file that uses write to external storage permission. When we delete this 
//file, we will no longer need that permission.

public class Debug {

	public static void log(String message)
	{
		Log.v("Debug Log", message);
	}
	
	public static void remakeDB()
	{
		DataHandler data = DataHandler.get();
		SQLiteDatabase db = data.getWritableDatabase();
		data.onUpgrade(db, 0, 1);
		data.update();
	}
	
	public static void writeDatabase(Activity act)
	{
	String path = DataHandler.get().getReadableDatabase().getPath();
	File f=new File(path);
	FileInputStream fis=null;
	FileOutputStream fos=null;

	try
	{
	  fis=new FileInputStream(f);
	  fos=new FileOutputStream("/mnt/sdcard/db_dump.db");
	  while(true)
	  {
	    int i=fis.read();
	    if(i!=-1)
	    {fos.write(i);}
	    else
	    {break;}
	  }
	  fos.flush();
	  Toast.makeText(act, "DB dump OK", Toast.LENGTH_LONG).show();
	}
	catch(Exception e)
	{
	  e.printStackTrace();
	  Toast.makeText(act, "DB dump ERROR", Toast.LENGTH_LONG).show();
	}
	finally
	{
	  try
	  {
	    fos.close();
	    fis.close();
	  }
	  catch(IOException ioe)
	  {}
	}
	}}
