package data;

//This would ideally be in the favor.develop package but then we couldn't get at some of the internal datahandler stuff...

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.favor.util.Contact;





import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

//Important!! This is the only file that uses write to external storage permission. When we delete this 
//file, we will no longer need that permission.

@SuppressLint("NewApi") //suppressing error on L46
public class Debug {
	
	public static void strictMode(){
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
		//TODO: change this after the email testing is done
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().penaltyLog().build());
		//StrictMode.enableDefaults();
	}
	
	public static void queryTest(Contact[] contacts, String[] keys)
	{
		Contact contact = contacts[0];
		DataHandler db = DataHandler.get();
		List<Message> list = db.queryFromAddress(contact, keys, -1, -1, DataConstants.Type.TYPE_TEXT);
		Debug.log("From");
		for (Message t : list )
		{
			Debug.log(""+t);
		}
		list = db.queryToAddress(contact, keys, -1, -1, DataConstants.Type.TYPE_TEXT);
		Debug.log("To");
		for (Message t : list )
		{
			Debug.log(""+t);
		}
		list = db.queryConversation(contact, keys, -1, -1, DataConstants.Type.TYPE_TEXT);
		Debug.log("Convo");
		for (Message t : list )
		{
			Debug.log(""+t);
		}
		Debug.log("MultiFrom");
		HashMap<Contact, ArrayList<Message>> multi = db.queryFromAddresses(contacts, keys, -1, -1, DataConstants.Type.TYPE_TEXT);
		for (Map.Entry<Contact, ArrayList<Message>> entry : multi.entrySet()) {
		    Debug.log(entry.getKey().getName()+":"+entry.getValue().size());
		}
		
		for (int i = 0; i < contacts.length; i++)
		{
			list = multi.get(contacts[i]);
			for (Message t : list )
			{
				Debug.log(""+t);
			}
		}
	}
	
	
	@SuppressLint("UseSparseArrays")
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

	/*
	public static void averageTest(){
		DataHandler db = DataHandler.get();
		Contact contact = null;
		for (Contact cc : db.contacts()){
			if(cc.addresses()[0].equals("3607428147")){
				contact=cc; //Robert
				Debug.log("Found Robert");
			}
		}
		double sqlAverage = db.average(contact, DataHandler.KEY_CHARCOUNT, -1, -1, "sent");
		double algoAverage = Algorithms.charCount(contact, -1, -1)[0];
		Debug.log("SQL Average:"+sqlAverage+" Algorithmic Average:"+algoAverage);
		long sum = db.sum(contact, DataHandler.KEY_CHARCOUNT, -1, -1, "sent");
		Debug.log("Sum: "+sum);
		for (Contact cc : db.contacts()){
			if(cc.addresses()[0].equals("2023650265")){
				contact=cc; //Rob Davis
				Debug.log("Found Rob Davis");
			}
		}
		sum = db.sum(contact, DataHandler.KEY_CHARCOUNT, -1, -1, "sent");
		Debug.log("SQL Average:"+db.average(contact, DataHandler.KEY_CHARCOUNT, -1, -1, "sent"));
		Debug.log("Sum: "+sum);
		
	}
	*/
	
	/*
	public static void queryEquality(){
		DataHandler db = DataHandler.get();
		Contact[] c = new Contact[2];
		List<Contact> contacts = db.contacts();
		for (Contact cc : contacts){
			//Debug.log(cc.toString());
			if(cc.addresses()[0].equals("3607428147")){
				c[0]=cc; //Robert
				Debug.log("Found Robert");
			}else if(cc.addresses()[0].equals("3607087506")){
				c[1]=cc; //Rebar
				Debug.log("Found Rebar");
			}
		}
		HashMap<Contact, ArrayList<textMessage>> dbResults = db.multiQueryDatabase(c, DataHandler.KEYS_PUBLIC, -1, -1, "sent");
		HashMap<Contact, ArrayList<textMessage>> javaResults = db.multiQuery(c, DataHandler.KEYS_PUBLIC, -1, -1, "sent");
		int totalOne=dbResults.get(c[0]).size(), totalTwo=javaResults.get(c[0]).size();
		Debug.log("Robert msg count -- total one:"+totalOne+" total two:"+totalTwo);
		if(totalOne!=totalTwo){
			Debug.log("Robert text quantity inequal; something broken.");
		}
		totalOne=dbResults.get(c[1]).size(); totalTwo=javaResults.get(c[1]).size();
		Debug.log("Rebar msg count -- total one:"+totalOne+" total two:"+totalTwo);
		if(totalOne!=totalTwo){
			Debug.log("Rebar text quantity inequal; something broken.");
		}
		totalOne = totalTwo=0;
		for (int i = 0; i < dbResults.get(c[0]).size(); ++i){
			totalOne+=dbResults.get(c[0]).get(i).charCount();
			totalTwo+=javaResults.get(c[0]).get(i).charCount();
		}
		if (totalOne!=totalTwo){
			Debug.log("Robert text char count total inequal; something broken.");
		}
		Debug.log("Robert char count -- total one:"+totalOne+" total two:"+totalTwo);
		totalOne = totalTwo = 0;
		for (int i = 0; i < dbResults.get(c[1]).size(); ++i){
			totalOne+=dbResults.get(c[1]).get(i).charCount();
			totalTwo+=javaResults.get(c[1]).get(i).charCount();
		}
		Debug.log("Rebar char count -- total one:"+totalOne+" total two:"+totalTwo);
		if (totalOne!=totalTwo){
			Debug.log("Rebar text char count total inequal; something broken.");
			
		}
		Debug.log("Finished");
		
	}
	*/
	
	public static void testData(Contact contact)
	{
		//"3607087506"
		
		//make a global hashtable, and come up with a way to programmatically produce unique
		//query type/date/name hash entries, and save values. this can fall out of scope periodically
		//but it will ensure that if someone requests the same results over and over, we don't redo
		//the math every time
		DataHandler db = DataHandler.get();
		
		long[] chars = DataProcessor.charCount(contact, -1, -1);
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
	
		DataProcessor.responseTime(new Contact ("Rebar Niemi", "-1", new String[]{"3607081836"}),1365348980000l, 1367940980000l);
	}
}
