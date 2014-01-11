package com.favor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.favor.widget.Contact;

import android.annotation.SuppressLint;
import android.app.Activity;
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
	
	public static void queryTest(Contact[] contacts, String[] keys)
	{
		Contact contact = contacts[0];
		DataHandler db = DataHandler.get();
		List<textMessage> list = db.queryFromAddress(contact, keys, -1, -1);
		Debug.log("From");
		for (textMessage t : list )
		{
			Debug.log(""+t);
		}
		list = db.queryToAddress(contact, keys, -1, -1);
		Debug.log("To");
		for (textMessage t : list )
		{
			Debug.log(""+t);
		}
		list = db.queryConversation(contact, keys, -1, -1);
		Debug.log("Convo");
		for (textMessage t : list )
		{
			Debug.log(""+t);
		}
		Debug.log("MultiFrom");
		HashMap<Contact, ArrayList<textMessage>> multi = db.queryFromAddresses(contacts, keys, -1, -1);
		for (Map.Entry<Contact, ArrayList<textMessage>> entry : multi.entrySet()) {
		    Debug.log(entry.getKey().getName()+":"+entry.getValue().size());
		}
		
		for (int i = 0; i < contacts.length; i++)
		{
			list = multi.get(contacts[i]);
			for (textMessage t : list )
			{
				Debug.log(""+t);
			}
		}
	}
	
	
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
	
	public static void testData(Contact contact)
	{
		//"3607087506"
		
		//make a global hashtable, and come up with a way to programmatically produce unique
		//query type/date/name hash entries, and save values. this can fall out of scope periodically
		//but it will ensure that if someone requests the same results over and over, we don't redo
		//the math every time
		DataHandler db = DataHandler.get();
		
		long[] chars = Algorithms.charCount(contact, -1, -1);
		db.saveData(contact, DataHandler.DATA_RECEIVED_CHARS, chars[1]);
		db.saveData(contact, DataHandler.DATA_SENT_CHARS, chars[0]);
		SparseArray<dataTime> all = db.getAllData(contact);
		log("Received Chars:"+all.get(DataHandler.DATA_RECEIVED_CHARS));
		log("Sent chars:"+all.get(DataHandler.DATA_SENT_CHARS));
		db.saveData(contact, DataHandler.DATA_SENT_MMS, 250l);
		log("Test MMS:"+db.getData(contact, DataHandler.DATA_SENT_MMS));
		//Algorithms.responseTime(address, -1, -1);
	}
	
	public static void remakeDB()
	{
		DataHandler data = DataHandler.get();
		SQLiteDatabase db = data.getWritableDatabase();
		data.onUpgrade(db, 0, 1);
		data.update();
	}
	
	@SuppressLint("SdCardPath")
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
	  log("DB dump OK");
	}
	catch(Exception e)
	{
	  e.printStackTrace();
	  Toast.makeText(act, "DB dump ERROR", Toast.LENGTH_LONG).show();
	  log("DB dump ERROR");
	}
	finally
	{
	  try
	  {
	    fos.close();
	    fis.close();
	  }
	  catch(IOException ioe)
	  {log("DB dump IOException:"+ioe.getMessage());}
	}
	}
	public static void algotest () {
	
		Algorithms.responseTime(new Contact ("Rebar Niemi", "-1", new String[]{"3607081836"}),1365348980000l, 1367940980000l);
	}
}
