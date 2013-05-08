package com.favor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

//Important!! This is the only file that uses write to external storage permission. When we delete this 
//file, we will no longer need that permission.

@SuppressLint("NewApi") //suppressing error on L46
public class Debug {
	
	/*
	 * 	private ContentValues mmsContent(long id, long date, long m_id, boolean sent)
	{		
		int media = 0;
		String type, data = "";
		Cursor c = context.getContentResolver().query(Uri.parse("content://mms/"+id+"/part"), new String[] {"_data", "text", "ct"}, "ct<>\"application/smil\"", null, null);
		while (c.moveToNext())
		{
			   type = c.getString(2);
			   if (type.equals("text/plain"))
			   {
				   data = c.getString(0);
				   if (data==null)
				   {
					   data = c.getString(1); //fetch from the "text" column
				   }
				   else 
				   {
					   Debug.log(data); //we have pure data
				   }
			   }
			   else media = 1;
		}
		c.close();
		
		String filter;
		if (sent) filter = "(type="+MMS_TO+" OR type="+MMS_CC+" OR type="+MMS_BCC+")";
		else filter = "type="+MMS_FROM;
		
		c = context.getContentResolver().query(Uri.parse("content://mms/"+id+"/addr"), new String[] {"address", "type"}, filter, null, null);
		String[] addresses = new String[c.getCount()];
		while (c.moveToNext())
		{
			addresses[c.getPosition()] = c.getString(1)+":"+c.getString(0);
		}
		String lo = c.getCount()+" entries sent:"+sent+":";
		for (int i = 0; i < addresses.length; i++)
		{
			lo+=addresses[i]+",";
		}
		Debug.log(lo);
		c.close();
		//actually need to generate one text per address;
		return new textMessage(id, date,addresses[0],data.length(), media, 0).content();
	}
	 */
	
	public static void uriProperties(String uri, Context con)
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
			Cursor c = con.getContentResolver().query(u, null, null, null, null);
			
	    	while(c.moveToNext())
	    	{
	            for (int i = 0; i < c.getColumnCount(); i++)
	            {
	            	String colName = c.getColumnName(i).toString();
	            	String colType = e.get(c.getType(i)); //this line requires a higher api level than we're
	            	//using, but it's a debug online line, so no worries, I think
	            	
	            	if (cols.get(colName)==null){cols.put(colName, colType);}
	            	else if (cols.get(colName)==nullType && colType != nullType){cols.put(colName, colType);}
	            }
	    	}
	    	int i = 0;
	    	for (Map.Entry<String, String> entry : cols.entrySet()) 
	    	{
	    	    String key = entry.getKey();
	    	    Object value = entry.getValue();
	    	    Log.v(uri+" Column-Type "+i,key+" - "+value);
	    	    i++;
	    	}  	
	    	c.close();
			
	}

	public static void log(String message)
	{
		Log.v("Debug Log", message);
	}
	
	public static void testData(String address)
	{
		//"3607087506"
		
		//make a global hashtable, and come up with a way to programmatically produce unique
		//query type/date/name hash entries, and save values. this can fall out of scope periodically
		//but it will ensure that if someone requests the same results over and over, we don't redo
		//the math every time
		DataHandler db = DataHandler.get();
		
		long[] chars = Algorithms.charCount(address, -1, -1);
		db.saveData(address, DataHandler.DATA_RECEIVED_CHARS, chars[1]);
		db.saveData(address, DataHandler.DATA_SENT_CHARS, chars[0]);
		SparseArray<Long> all = db.getAllData(address);
		log("Received Chars:"+all.get(DataHandler.DATA_RECEIVED_CHARS));
		log("Sent chars:"+all.get(DataHandler.DATA_SENT_CHARS));
		db.saveData(address, DataHandler.DATA_SENT_MMS, 250l);
		log("Test MMS:"+db.getData(address, DataHandler.DATA_SENT_MMS));
		//Algorithms.responseTime(address, -1, -1);
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
	}
	public static void algotest () {
		Algorithms.responseTime("3607081836",1365348980000l, 1367940980000l);
	}
}
