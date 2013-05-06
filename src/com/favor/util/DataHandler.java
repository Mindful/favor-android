package com.favor.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
//import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.SparseArray;
//import android.util.Log;

class dataException extends RuntimeException {

	private static final long serialVersionUID = -2500275405542504803L;
	
    public dataException(String exc)
    {
        super(exc);
    }
    public String getMessage()
    {
        return super.getMessage();
    }
	
	
}

class textMessage {
	private long id;
	private long date;
	private int charCount;
	private String address;
	private int sms;
	private int sent;
	
	public textMessage(long id, long date, String address, int charCount, int sms, int sent)
	{
		this.id = id;
		this.date = date;
		this.address = address;
		this.charCount = charCount;
		this.sms = sms;
		this.sent = sent;
	}
	
	public textMessage(long id, long date, String address, String msg, int sms, int sent)
	{
		this.id = id;
		this.date = date;
		this.address = address;
		this.charCount = msg.length();
		this.sms = sms;
		this.sent = sent;
	}
	
	public boolean mms(){return sms==0;}
	public boolean sms(){return sms!=0;}
	public boolean received(){return sent==0;}
	public boolean sent(){return sent!=0;}
	public int charCount(){return charCount;}
	public String address(){return address;}
	public long rawDate(){return date;}
	public long id(){return id;}
	
	public String textDate()
	{
		SimpleDateFormat d = new SimpleDateFormat("MM/dd/yyy hh:mm:ss a zzz");
		d.setTimeZone(TimeZone.getDefault());
		return d.format(new Date(date));
	}
	
	
	public ContentValues content()
	{
		ContentValues ret = new ContentValues();
		ret.put(DataHandler.PRIMARY_KEY_ID, id);
		ret.put(DataHandler.KEY_DATE, date);
		ret.put(DataHandler.KEY_ADDRESS, address);
		ret.put(DataHandler.KEY_CHARCOUNT, charCount);
		ret.put(DataHandler.KEY_SMS, sms);
		return ret;
	}
	
}


public class DataHandler extends SQLiteOpenHelper{ //saves (what saves?) should be done on pauses, can't count on exits
	
	//SQLite aspects
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "messagesDatabase";
    
    
    //Messages tables
    private static final String TABLE_SENT = "sent";
    private static final String TABLE_RECEIVED = "received";
 
    //Messages table column names
    public static final String PRIMARY_KEY_ID = "_id"; //unique integer message id
    public static final String KEY_DATE = "date"; //integer date 
    public static final String KEY_ADDRESS = "address"; //address
    public static final String KEY_CHARCOUNT = "chars"; //character count
    public static final String KEY_SMS = "sms"; //1:sms, 0:mms (or other?)
    public static final String GENERATED_KEY_SENT = "sent";
    //public static final String[] KEYS = {PRIMARY_KEY_ID, KEY_DATE, KEY_ADDRESS, KEY_CHARCOUNT, KEY_SMS};
    
    //Data table
    private static final String TABLE_DATA = "data";
    
    //Data table column names
    //USE KEY_ADDRESS
    public static final String KEY_TYPE = "type";
    //USE KEY_DATE
    public static final String KEY_COUNT = "count";
    
	private static String formatAddress(String address, boolean fetch)
	{
		if (address.contains("@")){ if (fetch) {address = "\"" + address +"\"";}}
		else address = address.replaceAll("[^0-9]", ""); //regex matches anything except digits
		return address;
	}
    
    
    //onCreate
    @Override
    public void onCreate(SQLiteDatabase db)
    {
    	db.execSQL("CREATE TABLE "+TABLE_SENT+"("+PRIMARY_KEY_ID+" INTEGER PRIMARY KEY,"+
        KEY_DATE+" INTEGER,"+KEY_ADDRESS+" TEXT,"+KEY_CHARCOUNT+" INTEGER,"+KEY_SMS+" INTEGER)");
    	db.execSQL("CREATE TABLE "+TABLE_RECEIVED+"("+PRIMARY_KEY_ID+" INTEGER PRIMARY KEY,"+
    	KEY_DATE+" INTEGER,"+KEY_ADDRESS+" TEXT,"+KEY_CHARCOUNT+" INTEGER,"+KEY_SMS+" INTEGER)");
    	db.execSQL("CREATE TABLE "+TABLE_DATA+"("+KEY_ADDRESS+" TEXT,"+KEY_TYPE+" INTEGER,"+KEY_DATE+
    	" INTEGER,"+KEY_COUNT+" INTEGER,"+"PRIMARY KEY("+KEY_ADDRESS+","+KEY_TYPE+"))");
		edit.putLong("lastFetch", 0);
		edit.apply();
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    	db.execSQL("DROP TABLE IF EXISTS "+TABLE_SENT);
    	db.execSQL("DROP TABLE IF EXISTS "+TABLE_RECEIVED);
    	db.execSQL("DROP TABLE IF EXISTS "+TABLE_DATA);
    	onCreate(db);
    }
    
    
	//Static aspects
	private static String PREFS_NAME = "dataPrefs.db";
	private static DataHandler singleton;
	/**
	 * Initializes and returns the singleton DataHandler instance. If a DataHandler is already running,
	 * a reference to that is returned.
	 * @param mainActivity The main activity, or a valid activity
	 */
	public static DataHandler initialize(Activity mainActivity)
	{
		if (singleton == null) singleton = new DataHandler(mainActivity);
		return singleton;
	}
	
	/**
	 * Returns a reference to the singleton DataHandler instance. Throws an exception if there is no
	 * initialized dataHandler instance.
	 */
	public static DataHandler get()
	{
		if (singleton == null) throw new dataException("Cannot get uninitialized DataHandler");
		return singleton;
	}
	
	private static final Uri SMS_IN = Uri.parse("content://sms/inbox");
	private static final Uri SMS_OUT = Uri.parse("content://sms/sent");
	private static final Uri MMS_IN = Uri.parse("content://mms/inbox");
	private static final Uri MMS_OUT = Uri.parse("content://mms/sent");
	private static final String[] SMS_PROJECTION={"_id", "date", "address", "body"};
	private static final String[] MMS_PROJECTION={"_id", "date"}; //I think just date and ID
	
	
	//Instance aspects
	
	private Context context;
	private long lastFetch;
	SharedPreferences prefs;
	SharedPreferences.Editor edit;
	
	private DataHandler(Activity mainActivity)
	{
		super(mainActivity.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
		context = mainActivity.getApplicationContext();
		prefs = mainActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		//prefs = PreferenceManager.getDefaultSharedPreferences(context);
		edit = prefs.edit();
		lastFetch = prefs.getLong("lastFetch", 0);
		//fetch();
	}
	
	/**
	 * Updates the application's internal database by pulling all new messages since the last update
	 * call. Can potentially be long-running when called for the first time, or after a long delay.
	 */
	public void update()
	{
		lastFetch = prefs.getLong("lastFetch", 0); //mandatory. not sure how this wasn't here before
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = context.getContentResolver().query(SMS_IN, SMS_PROJECTION, KEY_DATE+" > "+lastFetch, null, KEY_DATE);
		int[] cols = new int[SMS_PROJECTION.length];
		for (int i = 0; i < SMS_PROJECTION.length; i++)
		{
			cols[i] = c.getColumnIndex(SMS_PROJECTION[i]);
		}
		db.beginTransaction();
		try{
		while (c.moveToNext())
		{
			db.insert(TABLE_RECEIVED, null, 
			new textMessage(c.getLong(cols[0]), c.getLong(cols[1]), formatAddress(c.getString(cols[2]), false), c.getString(cols[3]), 1, 0).content());
		}
		c.close();
		c = context.getContentResolver().query(SMS_OUT, SMS_PROJECTION, KEY_DATE+" > "+lastFetch, null, KEY_DATE);
		while (c.moveToNext())
		{
			db.insert(TABLE_SENT, null, 
			new textMessage(c.getLong(cols[0]), c.getLong(cols[1]),  formatAddress(c.getString(cols[2]), false), c.getString(cols[3]), 1, 1).content());
		}
		c.close();
		//TODO: WE NEED TO HANDLE MMS FETCHING AS WELL. IMPORTANTTTTT!
		} 
		catch (Exception ex){throw new dataException(ex.toString());}
		finally {db.setTransactionSuccessful();}
		db.endTransaction();
		
		db.close();
		Date d = new Date();
		edit.putLong("lastFetch", d.getTime());
		edit.apply();
		//edit.apply(); //not commit, because we're the only ones setting this value
	}
	
	//Data Section
	public static final int DATA_SENT_CHARS = 1; //total characters sent to this contact (long)
	public static final int DATA_RECEIVED_CHARS = 2; //total characters received from this contact (long)
	public static final int DATA_SEND_TIME = 3; //average response time to this contact (longdate)
	public static final int DATA_RECEIVE_TIME = 4; //average response from this contact (longdate)
	public static final int DATA_SENT_MMS = 5; //mms messages sent to this contact (long)
	public static final int DATA_RECEIVED_MMS = 6; //mms messages received from this contact (long)
	public static final int DATA_SENT_TOTAL = 7; //total messages (sms+mms) sent to this contact (long)
	public static final int DATA_RECEIVED_TOTAL = 8; //total messages (sms+mms) received from this contact (long)
	
	private void validDate(int type)
	{
		if (type <= 0 || type >= 9) throw new dataException("Invalid data type. Please use class constants.");
	}
	/**
	 * Loads data about a user. Data types are listed
	 * as class constants in the form DATA_xxxxx. Returns -1 if no data is found.
	 * @param address The address of the user you are saving data about.
	 * @param type The data type you wish to save. Use class constants (DATA_xxxxx)
	 * 
	 */
	public long getData(String address, int type)
	{
		validDate(type);
		address = formatAddress(address, true);
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_DATA, new String[] {KEY_COUNT}, KEY_ADDRESS+"="+address+" AND "+KEY_TYPE+"="+type, null, null, null, null);
		if (c.getCount()==0) return -1;
		else if (c.getCount()>1) throw new dataException("getData producing multiple results.");
		c.moveToNext();
		return c.getLong(0);
	}
	/**
	 * Loads all data about a user, returning anything found in a SparseArray, with data mapped
	 * to its type. Data types are listed as class constants in the form DATA_xxxxx. 
	 * Returns -1 if no data is found.
	 * @param address The address of the user you are saving data about.
	 * 
	 */
	
	public SparseArray<Long> getAllData(String address)
	{
		address = formatAddress(address, true);
		SparseArray<Long> ret = new SparseArray<Long>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_DATA, new String[] {KEY_TYPE, KEY_COUNT}, KEY_ADDRESS+"="+address, null, null, null, null);
		while (c.moveToNext())
		{
			ret.put(c.getInt(0), c.getLong(1));
		}
		return ret;
		
	}
	/**
	 * Saves data of a given type about a user. Data types are listed
	 * as class constants in the form DATA_xxxx.
	 * Also, data MUST BE SAVED IMMEDIATELY after it is calculated
	 * so that the savedate is appropriate. Do not preserve results and save them later.
	 * @param address The address of the user you are saving data about.
	 * @param type The data type you wish to save. Use class constants (DATA_xxxxx)
	 * 
	 */
	public void saveData(String address, int type, long data)
	{
		validDate(type);
		address = formatAddress(address, false);
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ADDRESS, formatAddress(address, false));
		values.put(KEY_TYPE, type);
		values.put(KEY_DATE, new Date().getTime());
		values.put(DataHandler.KEY_COUNT, data);
		db.insert(TABLE_DATA, null, values);
	}
	
	
	
	
	
	//Message Section
	
	private static final String SORT_DIRECTION = "DESC"; //or ASC for most recent message last
	
	private ArrayList<textMessage> query(long fromDate, long untilDate, String address, String table)
	{
		//-1 for no date, empty string for no address. Obviously, table is mandatory.
		//Automatically sorted by date.
		address = formatAddress(address, true);
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		
		int sent;
		if (table == TABLE_SENT) sent = 1;
		else if (table == TABLE_RECEIVED) sent = 0;
		else throw new dataException("Invalid table name. Serious internal error.");
		
		SQLiteDatabase db = getReadableDatabase();
		String selection = "";
		if (!address.equals("")) selection = KEY_ADDRESS+"="+address;
		
		if (fromDate > -1)
		{
			if (selection.length() > 0) selection += " AND ";
			selection += KEY_DATE + ">=" + fromDate;
		}
		if (untilDate > -1)
		{
			if (selection.length() > 0) selection += " AND ";
			selection += KEY_DATE + "<=" +untilDate;
		}
		
		Cursor c = db.query(table, null, selection, null, null, null, KEY_DATE+" "+SORT_DIRECTION);
		ArrayList<textMessage> ret = new ArrayList<textMessage>(c.getCount());
		while (c.moveToNext())
		{
			ret.add(new textMessage(c.getLong(0), c.getLong(1), c.getString(2), c.getInt(3), c.getInt(4), sent));
		}
		c.close();
		db.close();
		return ret;
	}

	private HashMap<String, ArrayList<textMessage>> multiQuery(long fromDate, long untilDate, String[] addresses, String table)
	{
		//use group by to ensure addresses are together, then interate keeping a coutner and arraylist per address
		//when we hit the end of an address, we will cursor move back to the beginning of the address and drop all of the address
		//items into the arraylist, then hash the arraylist and move on to the next address. this makes us only need one hashtable, and only one 
		//loop, and no hashtable gets. still 2n though
		
		//-1 for no date, empty string for no address. Obviously, table is mandatory.
		//Automatically sorted by date.
		for (int i = 0; i < addresses.length; i++)
		{
			addresses[i] = formatAddress(addresses[i], true);
		}
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		if (addresses.length < 2) throw new dataException("multiQuery should not be used with less than 2 addresses.");
		
		int sent;
		if (table == TABLE_SENT) sent = 1;
		else if (table == TABLE_RECEIVED) sent = 0;
		else throw new dataException("Invalid table name. Serious internal error.");
		
		SQLiteDatabase db = getReadableDatabase();
		HashMap<String, ArrayList<textMessage>> ret = new HashMap<String, ArrayList<textMessage>>();
		
		String selection = "(";
		for (int i = 0; i < addresses.length; i++)
		{
			selection += KEY_ADDRESS+"="+addresses[i];
			if (i < addresses.length-1) selection += " OR ";
			else selection += ")";
		}
		if (fromDate > -1) selection += " AND " + KEY_DATE + ">=" + fromDate;
		if (untilDate > -1) selection += " AND " + KEY_DATE + "<=" +untilDate;
		
		//we don't want group by. we want multiple order by
		
		Cursor c = db.query(table, null, selection, null, null, null, KEY_ADDRESS+", "+KEY_DATE+" "+SORT_DIRECTION);
		
		
		//Set up the loop
		int hi, lo;
		if (!c.moveToNext()) return ret; //empty hashmap. nothing to return. this also moves us, though
		
		String prevAddr = c.getString(2);
		hi = lo = 0;
		ArrayList<textMessage> list;
		while (true)
		{
			if (c.moveToNext() && prevAddr.equals(c.getString(2)))//Short circuiting saves us possible exceptions here
			{
				hi++; //hi will max out at the last element of the same address
			}
			else
			{
				c.moveToPosition(lo);
				list = new ArrayList<textMessage>((hi-lo)+1); //Number of terms we'll have to deal with
				for(; lo<=hi ;lo++)
				{
					list.add(new textMessage(c.getLong(0), c.getLong(1), c.getString(2), c.getInt(3), c.getInt(4), sent));
					c.moveToNext(); //This loop structure leaves the cursor at (hi+1)
				}
				ret.put(prevAddr, list);
				hi = lo = hi+1;
				if (c.isAfterLast()) break; //end of the loop
				else prevAddr = c.getString(2);
			}
		}
		

		c.close();
		db.close();
		return ret;
	}
	
	//basically, this returns the messages sent to and received by the user to a given address, and puts them in a que (or deque)
	//this exists for the purpose of computing response time, which we do by moving through the que, discarding any consecutively sent or received messages
	//so that response time is always first to first. then we look at all the response times and use statistics to discard outliers (based on standard dev)
	//worth remembering that LinkedLists inheret the (slightly) MORE EFFICIENT Queue functions
	/**
	 * Returns all messages to and from a specific address between the two given dates. Pass -1 to 
	 * void date parameters and not use them. Returns come in the form of a sorted LinkedList with
	 * the most recent message at the head.
	 * @param address Typically phone number or email
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public LinkedList<textMessage> queryConversation(String address, long fromDate, long untilDate)
	{
		//same sort direction as the other two
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		
		address = formatAddress(address, true);
		
		LinkedList<textMessage> res = new LinkedList<textMessage>();
		SQLiteDatabase db = getReadableDatabase();
		
		//holy grail of SQL statement construction right here; this works like a charm. the generated key/column "sent" is 1 for sent, 0 for received
		String selection = " WHERE "+KEY_ADDRESS+"="+address;
		if (fromDate > -1) selection += " AND " + KEY_DATE + ">=" + fromDate;
		if (untilDate > -1) selection += " AND " + KEY_DATE + "<=" +untilDate;
		String sql = 
				"SELECT *, 1 as "+GENERATED_KEY_SENT+" FROM "+TABLE_SENT+selection+
				" UNION "+
				"SELECT *, 0 as "+GENERATED_KEY_SENT+" FROM "+TABLE_RECEIVED+selection+
				" ORDER BY "+KEY_DATE+" "+SORT_DIRECTION;
		
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext())
		{
			res.offer(new textMessage(c.getLong(0), c.getLong(1), c.getString(2), c.getInt(3), c.getInt(4), c.getInt(5)));
		}
		
		return res;
	}
	
	/**
	 * Returns all received messages between the two given dates. Pass -1 to void the date
	 * parameters and not use them. Returns come in the form of a sorted ArrayList with
	 * the most recent message at index 0.
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public ArrayList<textMessage> queryFromAll(long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(fromDate, untilDate, "", TABLE_RECEIVED);
	}
	
	/**
	 * Returns all sent messages between the two given dates. Pass -1 to void the date
	 * parameters and not use them. Returns come in the form of a sorted ArrayList with
	 * the most recent message at index 0.
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public ArrayList<textMessage> queryToAll(long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(fromDate, untilDate, "", TABLE_SENT);
	}
	
	/**
	 * Returns all messages received from a specific address between the two dates. Pass
	 * -1 to void the date parameters and not use them. Returns come in the form of a sorted
	 * ArrayList with the most recent message at index 0.
	 * @param address Typically phone number or email
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public ArrayList<textMessage> queryFromAddress(String address, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(fromDate, untilDate, address, TABLE_RECEIVED);
	}
	
	/**
	 * Returns all messages sent to a specific address between the two dates. Pass
	 * -1 to void the date parameters and not use them. Returns come in the form of a sorted
	 * ArrayList with the most recent message at index 0.
	 * @param address Typically phone number or email
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	
	public ArrayList<textMessage> queryToAddress(String address, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(fromDate, untilDate, address, TABLE_SENT);
	}
	
	/**
	 * Returns all messages received from specific addresses between the two dates. Pass
	 * -1 to void the date parameters and not use them. Returns come in the form of a HashMap
	 * of sorted ArrayLists, accessed using the relevant address as a key.
	 * @param addresses An array of addresses
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public HashMap<String, ArrayList<textMessage>> queryFromAddresses(String[] addresses, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return multiQuery(fromDate, untilDate, addresses, TABLE_RECEIVED);
	}
	
	/**
	 * Returns all messages sent to specific addresses between the two dates. Pass
	 * -1 to void the date parameters and not use them. Returns come in the form of a HashMap
	 * of sorted ArrayLists, accessed using the relevant address as a key.
	 * @param addresses An array of addresses
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	
	public HashMap<String, ArrayList<textMessage>> queryToAddresses(String[] addresses, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return multiQuery(fromDate, untilDate, addresses, TABLE_SENT);
	}
	

}
