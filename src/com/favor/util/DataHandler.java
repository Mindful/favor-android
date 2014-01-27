package com.favor.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.favor.ui.GraphActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.SparseArray;


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

class dataTime {
	private long count;
	private long time;
	
	public dataTime(long count, long time)
	{
		this.count = count;
		this.time = time;
	}
	
	public long count(){return count;}
	public long time(){return time;}
}
class textMessage {
	private long date;
	private int charCount;
	private String address;
	private int media;
	private int sent;
	
	private textMessage(){};
	
	public static textMessage build(Cursor c, int sent)
	{
		textMessage ret = new textMessage();
		int dateColumn = c.getColumnIndex(DataHandler.KEY_DATE), addressColumn = c.getColumnIndex(DataHandler.KEY_ADDRESS),
		charCountColumn = c.getColumnIndex(DataHandler.KEY_CHARCOUNT), mediaColumn = c.getColumnIndex(DataHandler.KEY_MEDIA);
		ret.sent = sent;
		if (dateColumn != -1) ret.date = c.getLong(dateColumn);
		else ret.date = -1;
		if (addressColumn != -1) ret.address= c.getString(addressColumn);
		else ret.address = null;
		if (charCountColumn != -1) ret.charCount = c.getInt(charCountColumn);
		else ret.charCount = -1;
		if (mediaColumn != -1) ret.media = c.getInt(mediaColumn);
		else ret.media = -1;
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		textMessage other = (textMessage) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (charCount != other.charCount)
			return false;
		if (date != other.date)
			return false;
		if (media != other.media)
			return false;
		if (sent != other.sent)
			return false;
		return true;
	}

	/**
	 * Strictly a debug method
	 */
	public String toString()
	{
		String log = "Address:";
		if (address==null) log+="<>";
		else log+=address;
		log+=" Date:";
		if (date==-1) log+="<>";
		else log+=date;
		log+=" Chars:";
		if (charCount==-1)log+="<>";
		else log+=charCount;
		log+=" Media:";
		if (media==-1)log+="<>";
		else log+=media;
		log+=" Sent:";
		if (sent==-1)log+="<>";
		else log+=sent;
		return log;
	}
	
	
	public boolean multimedia()
	{
		if (media==-1) throw new dataException ("multimedia() value not known. Query must include KEY_MEDIA");
		else return media==1;
	}
	public boolean received()
	{
		if (sent==-1) throw new dataException ("received() value not known. Query must include KEY_SENT");
		else return sent==0;
	}
	public int charCount()
	{
		if (charCount==-1) throw new dataException ("charCount() value not known. Query must include KEY_CHARCOUNT");
		else return charCount;
	}
	public String address()
	{
		if (address==null) throw new dataException ("address() value not known. Query must include KEY_ADDRESS");
		else return address;
	}
	public long rawDate()
	{
		if (date==-1) throw new dataException ("rawDate() value not known. Query must include KEY_DATE");
		else return date;
	}
	
	public String textDate()
	{
		if (date==-1) throw new dataException("textDate() value not known. Query must include KEY_DATE");
		else
		{
			SimpleDateFormat d = new SimpleDateFormat("MM/dd/yyy hh:mm:ss a zzz");
			d.setTimeZone(TimeZone.getDefault());
			return d.format(new Date(date));
		}
	}
	
}

//http://stackoverflow.com/questions/15732713/column-index-order-sqlite-creates-table
//Indicates that "order depends on projection i.e. select name, lastname from table"
public class DataHandler extends SQLiteOpenHelper{ 
	//SQLite aspects
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "messagesDatabase";
    
    
    //Messages tables
    private static final String TABLE_SENT = "sent";
    private static final String TABLE_RECEIVED = "received";
    //Messages table column names
    private static final String KEY_ID = "_id"; //unique integer message id
    private static final String KEY_CONTACT_ID = "contact_id"; //Contact ID, valid *most of the time
    public static final String KEY_DATE = "date"; //integer date 
    public static final String KEY_ADDRESS = "address"; //address
    public static final String KEY_CHARCOUNT = "chars"; //character count
    public static final String KEY_MEDIA = "media"; //1:media, 0:no media (sms or plain mms)
    private static final String GENERATED_KEY_SENT = "sent";
    public static final String[] KEYS_PUBLIC  = {KEY_DATE, KEY_ADDRESS, KEY_CHARCOUNT, KEY_MEDIA};
    
    //Data table
    private static final String TABLE_DATA = "data";
    
    //Data table column names
    //USE KEY_ADDRESS
    private static final String KEY_DATA_TYPE = "type";
    //USE KEY_DATE
    private static final String KEY_COUNT = "count";
    
	private static ContentValues row(long id, long date, String address, String msg, int media, int sent)
	{
		ContentValues ret = new ContentValues();
		ret.put(KEY_ID, id);
		ret.put(KEY_DATE, date);
		ret.put(KEY_ADDRESS, address);
		ret.put(KEY_CHARCOUNT, msg.length());
		ret.put(KEY_MEDIA, media);
		return ret;
	}
	
	/**
	 * Format the address into something we can use (largely cleans up phone numbers). Boolean "fetch"
	 * param is true if we're using this for an SQL query and we need symbol escapes.
	 */
    
	private static String formatAddress(String address, boolean fetch)
	{
		if (address.contains("@") && fetch){ address = "\"" + address +"\"";}
		else address = address.replaceAll("[^0-9]", ""); //regex matches anything except digits
		return address;
	}
    
    
    //onCreate
    @Override
    public void onCreate(SQLiteDatabase db)
    {
    	//Both tables are indexed by address and then date for query optimization
    	
    	//Sent table
    	//Unique to a combo of ID and address, to allow for outgoing duplicates
    	db.execSQL("CREATE TABLE "+TABLE_SENT+"("+KEY_ID+" INTEGER,"+
        KEY_DATE+" INTEGER,"+KEY_ADDRESS+" TEXT,"+KEY_CHARCOUNT+" INTEGER,"+KEY_MEDIA+" INTEGER,"+
        "UNIQUE ("+KEY_ID+","+KEY_ADDRESS+"))");
    	
    	//Received Table
    	db.execSQL("CREATE TABLE "+TABLE_RECEIVED+"("+KEY_ID+" INTEGER PRIMARY KEY,"+
    	KEY_DATE+" INTEGER,"+KEY_ADDRESS+" TEXT,"+KEY_CHARCOUNT+" INTEGER,"+KEY_MEDIA+" INTEGER)");
    	
    	//Indices
    	if (prefs.getBoolean(SAVED_INDEX, true))
    	{
    		db.execSQL("CREATE INDEX i_"+TABLE_SENT+" ON "+TABLE_SENT+" ("+KEY_ADDRESS+","+KEY_DATE+")");
        	db.execSQL("CREATE INDEX i_"+TABLE_RECEIVED+" ON "+TABLE_RECEIVED+" ("+KEY_ADDRESS+","+KEY_DATE+")");
    	}

    	//Data table
    	db.execSQL("CREATE TABLE "+TABLE_DATA+"("+KEY_CONTACT_ID+" TEXT,"+KEY_DATA_TYPE+" INTEGER,"+KEY_DATE+
    	" INTEGER,"+KEY_COUNT+" INTEGER,"+"PRIMARY KEY("+KEY_CONTACT_ID+","+KEY_DATA_TYPE+"))");
		edit.putLong(SAVED_FETCH, 0);
		edit.apply();
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    	db.execSQL("DROP TABLE IF EXISTS "+TABLE_SENT);
    	db.execSQL("DROP TABLE IF EXISTS "+TABLE_RECEIVED);
    	db.execSQL("DROP INDEX IF EXISTS i_"+TABLE_SENT);
    	db.execSQL("DROP INDEX IF EXISTS i_"+TABLE_RECEIVED);
    	db.execSQL("DROP TABLE IF EXISTS "+TABLE_DATA);
    	onCreate(db);
    }
    
    
	//Static aspects
	public static String PREFS_NAME = "dataPrefs.db";
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
	private static final String[] MMS_PROJECTION={"_id", "date"}; 
	
	private static final String SAVED_FETCH = "lastFetch";
	private static final String SAVED_INDEX = "index";
	
	
	
	//Instance aspects
	
	private final Context context;
	private final SharedPreferences prefs;
	private final SharedPreferences.Editor edit;
	
	private long lastFetch;
	private ArrayList<Contact> contactsList;
	
	
	/**
	 * Utility method that provides easy access to the global application context.
	 */
	public Context context(){return context;}
	
	/**
	 * An unmodifiable list of contacts.
	 */
	public List<Contact> contacts(){return Collections.unmodifiableList(contactsList);}
	//Not the prettiest, but people need not to be able to change it
	
	private DataHandler(Activity mainActivity)
	{
		super(mainActivity.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
		context = mainActivity.getApplicationContext();
		prefs = mainActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		edit = prefs.edit();
		lastFetch = prefs.getLong(SAVED_FETCH, 0);
	}
	
	/**
	 * Returns true if indexing is enabled (or defaulted to true), else returns false.
	 */
	
	public boolean indexingEnabled()
	{
		return prefs.getBoolean(SAVED_INDEX, true);
	}
	
	/**
	 * Enables indexing. This will build the indexes and save indexing as enabled, and can take a
	 * significant amount of time.
	 */
	public void enableIndexing()
	{
		if (indexingEnabled()) return;
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("CREATE INDEX i_"+TABLE_SENT+" ON "+TABLE_SENT+" ("+KEY_ADDRESS+","+KEY_DATE+")");
    	db.execSQL("CREATE INDEX i_"+TABLE_RECEIVED+" ON "+TABLE_RECEIVED+" ("+KEY_ADDRESS+","+KEY_DATE+")");
	}
	
	/**
	 * Disables indexing. This will drop built indexes and save indexing as disabled.
	 */
	
	public void disableIndexing()
	{
		if (!indexingEnabled()) return;
		SQLiteDatabase db = getWritableDatabase();
    	db.execSQL("DROP INDEX IF EXISTS i_"+TABLE_SENT);
    	db.execSQL("DROP INDEX IF EXISTS i_"+TABLE_RECEIVED);
	}
	
	/**
	 * Updates our list of contacts by querying the phone's internal contacts representation.
	 */
	public void updateContacts()
	{
		Cursor contacts = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, 
		ContactsContract.CommonDataKinds.Phone.NUMBER,
		ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
		null, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" ASC");
		
		contactsList = new ArrayList<Contact>(contacts.getCount());
		//This will be slightly larger than it actually needs to be, but is almost certainly better than
		//starting with a tiny ArrayList
		String curId = null, curName = null;
		ArrayList<String> buffer = new ArrayList<String>();
		while (contacts.moveToNext()) 
		{
			//If using IDs seems overkill, remember that two contacts can have the same name
			String rowId = contacts.getString(2);
			if(!rowId.equals(curId))
			{
				if (curId!=null)
				{
					contactsList.add(new Contact(curName, curId, (String[])buffer.toArray(new String[buffer.size()])));
					buffer.clear();
				}
				curId = rowId;
				curName = contacts.getString(0);
			}
			buffer.add(formatAddress(contacts.getString(1), false));
		}
		//Have to handle the last contact after the loop terminates
		if (curId!=null) contactsList.add(new Contact(curName, curId, (String[])buffer.toArray(new String[buffer.size()])));
		Collections.sort(contactsList, new Comparator<Contact>() {

			@Override
			public int compare(Contact lhs, Contact rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}

		});
	}
	
	
	/**
	 * Updates the application's internal database by pulling all new messages since the last update
	 * call. Can potentially be long-running when called for the first time, or after a long delay.
	 */
	public void update()
	{
		//---------------------------------
		//This is where we clean/update other held data
		GraphActivity.clearPrevContacts();
		//----------------------------------
		updateContacts();
		//----------------------------------
		//Do the actual heavy lifting for texts and such below
		lastFetch = prefs.getLong(SAVED_FETCH, 0);
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try{
		Cursor c = context.getContentResolver().query(SMS_IN, SMS_PROJECTION, KEY_DATE+" > "+lastFetch, null, KEY_DATE);
		while (c.moveToNext())
		{
			db.insert(TABLE_RECEIVED, null, 
			row(c.getLong(0), c.getLong(1), formatAddress(c.getString(2), false), c.getString(3), 0, 0));
		}
		c.close();
		c = context.getContentResolver().query(SMS_OUT, SMS_PROJECTION, KEY_DATE+" > "+lastFetch, null, KEY_DATE);
		while (c.moveToNext())
		{
			db.insert(TABLE_SENT, null, 
			row(c.getLong(0), c.getLong(1),  formatAddress(c.getString(2), false), c.getString(3), 0, 1));
		}
		c.close();
		c = context.getContentResolver().query(MMS_IN, MMS_PROJECTION, KEY_DATE+" > "+lastFetch, null, KEY_DATE);
		while (c.moveToNext())
		{
			receivedMMS(c.getLong(0), c.getLong(1), db);
		}
		c.close();
		c = context.getContentResolver().query(MMS_OUT, MMS_PROJECTION, KEY_DATE+" > "+lastFetch, null, KEY_DATE);
		while (c.moveToNext())
		{
			sentMMS(c.getLong(0), c.getLong(1), db);
		}
		c.close();
		} 
		catch (Exception ex){throw new dataException(ex.toString());}
		finally {db.setTransactionSuccessful();}
		db.endTransaction();
		
		db.close();
		Date d = new Date();
		edit.putLong(SAVED_FETCH, d.getTime());
		edit.apply();
	}
	
	private static final String MMS_CC = "130"; //0x82 in com.google.android.mms.pdu.PduHeaders
	private static final String MMS_BCC = "129"; //0x81 in com.google.android.mms.pdu.PduHeaders
	private static final String MMS_TO = "151"; //0x97 in com.google.android.mms.pdu.PduHeaders
	private static final String MMS_FROM = "137"; //0x89 in com.google.android.mms.pdu.PduHeaders
	
	private void sentMMS(long id, long date, SQLiteDatabase db)
	{
		//MMS IDs are negative to avoid overlap 
		//Additionally, MMS dates must be multiplied by 1000 to work properly vs SMS dates
		date = date *1000l;
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
					   Misc.logError("Unknown message data:"+data); //we have pure data
				   }
			   }
			   else media = 1;
		}
		c.close();
		
		String filter = "(type="+MMS_TO+" OR type="+MMS_CC+" OR type="+MMS_BCC+")";
		
		c = context.getContentResolver().query(Uri.parse("content://mms/"+id+"/addr"), new String[] {"address"}, filter, null, null);
		if (c.getCount() > 1)
		{
			while (c.moveToNext())
			{
				db.insert(TABLE_SENT, null, 
				row(-id, date, formatAddress(c.getString(0), false), data, media, 0));
			}
		}
		else
		{
			if (c.moveToFirst())
				{
					db.insert(TABLE_SENT, null, 
					row(-id, date, formatAddress(c.getString(0), false), data, media, 0)); 
				}
		}
		c.close();
	}
	
	private void receivedMMS(long id, long date, SQLiteDatabase db)
	{
		//MMS IDs are negative to avoid overlap 
		//Additionally, MMS dates must be multiplied by 1000 to work properly vs SMS dates
		date = date *1000l;
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
					   Misc.logError("Unknown message data:"+data); //we have pure data
				   }
			   }
			   else media = 1;
		}
		c.close();
		String filter = "type="+MMS_FROM;
		c = context.getContentResolver().query(Uri.parse("content://mms/"+id+"/addr"), new String[] {"address"}, filter, null, null);
		c.moveToFirst();
		String address = c.getString(0);
		c.close();
		db.insert(TABLE_RECEIVED, null, 
		row(-id, date,  formatAddress(address, false), data, media, 0));
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
	
	private String buildSelection(ArrayList<String> addresses, long fromDate, long untilDate, boolean raw){
		StringBuilder selection = new StringBuilder();
		if (addresses==null){
			//Nothing to do with addresses
		} else if (addresses.size()==1){
			if(raw) selection.append(" WHERE ");
			selection.append(KEY_ADDRESS).append("=").append(addresses.get(0));
		}
		else{
			if(raw) selection.append(" WHERE ");
			selection.append("(");
			for (int i = 0; i < addresses.size(); i++)
			{
				selection.append(KEY_ADDRESS).append("=").append(addresses.get(i));
				if (i < addresses.size()-1) selection.append(" OR ");
				else selection.append(")");
			}
		}
		if (fromDate > -1){
			if (selection.length() > 0) selection.append(" AND ");
			selection.append(KEY_DATE).append(">=").append(fromDate);
		}
		if (untilDate > -1){
			if (selection.length() > 0) selection.append(" AND ");
			selection.append(KEY_DATE).append("<=").append(untilDate);
		}
		return selection.toString();
		
	}
	/**
	 * Loads data about a user. Data types are listed
	 * as class constants in the form DATA_xxxxx. 
	 * Returns a dataTime object with fields "count" and "time", both longs.
	 * If no data is found, fields "count" and "time" will be -1.
	 * @param address The address of the user you are saving data about.
	 * @param type The data type you wish to save. Use class constants (DATA_xxxxx)
	 * 
	 */
	public dataTime getData(Contact contact, int type)
	{
		validDate(type);
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_DATA, new String[] {KEY_COUNT, KEY_DATE}, KEY_CONTACT_ID+"="+contact.id()+" AND "+KEY_DATA_TYPE+"="+type, null, null, null, null);
		if (c.getCount()==0) return new dataTime(-1, -1);
		else if (c.getCount()>1) throw new dataException("getData producing multiple results.");
		c.moveToNext();
		return new dataTime(c.getLong(0), c.getLong(1));
	}
	
	/**
	 * Loads all data about a user, returning anything found in a SparseArray, with data mapped
	 * to its type. Data types are listed as class constants in the form DATA_xxxxx. 
	 * Returns -1 if no data is found.
	 * @param address The address of the user you are saving data about.
	 * 
	 */
	
	public SparseArray<dataTime> getAllData(Contact contact)
	{
		SparseArray<dataTime> ret = new SparseArray<dataTime>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_DATA, new String[] {KEY_DATA_TYPE, KEY_COUNT, KEY_DATE}, KEY_CONTACT_ID+"="+contact.id(), null, null, null, null);
		while (c.moveToNext())
		{
			ret.put(c.getInt(0), new dataTime(c.getLong(1), c.getLong(2)));
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
	public void saveData(Contact contact, int type, long data)
	{
		validDate(type);
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_CONTACT_ID, contact.id());
		values.put(KEY_DATA_TYPE, type);
		values.put(KEY_DATE, new Date().getTime());
		values.put(KEY_COUNT, data);
		db.insert(TABLE_DATA, null, values);
	}
	
	
	
	
	
	//Message Section
	
	private static final String SORT_DIRECTION = "DESC"; //or ASC for most recent message last
	
	
	private ArrayList<textMessage> query(Contact contact, String[] keys, long fromDate, long untilDate, String table)
	{
		//-1 for no date, null contact for no addresses. Obviously, table is mandatory.
		//Automatically sorted by date.
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		if (keys.length == 0) throw new dataException("must request at least one value.");
		int sent = (table == TABLE_SENT) ? 1 : 0;
		
		SQLiteDatabase db = getReadableDatabase();
		ArrayList<String> addresses = null;
		if(contact!=null) addresses = new ArrayList<String>(Arrays.asList(contact.addresses()));
		String selection = buildSelection(addresses, fromDate, untilDate, false);
		
		Cursor c = db.query(table, keys, selection, null, null, null, KEY_DATE+" "+SORT_DIRECTION);
		ArrayList<textMessage> ret = new ArrayList<textMessage>(c.getCount());
		while (c.moveToNext())
		{
			ret.add(textMessage.build(c, sent));
		}
		c.close();
		db.close();
		return ret;
	}
	
	public double average(Contact contact, String key, long fromDate, long untilDate, String table){
		//-1 for no date, null contact for no addresses. Obviously, table is mandatory.
		//Automatically sorted by date.
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		SQLiteDatabase db = getReadableDatabase();
		ArrayList<String> addresses = new ArrayList<String>(Arrays.asList(contact.addresses()));
		String selection = buildSelection(addresses, fromDate, untilDate, true);
		
		Cursor c = db.rawQuery("SELECT AVG("+key+") from "+table+selection, null);
		if(c.moveToFirst()){
			return c.getDouble(0);
		}
		else return 0.00d;
	}
	
	public long sum(Contact contact, String key, long fromDate, long untilDate, String table){
		//-1 for no date, null contact for no addresses. Obviously, table is mandatory.
		//Automatically sorted by date.
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		SQLiteDatabase db = getReadableDatabase();
		ArrayList<String> addresses = new ArrayList<String>(Arrays.asList(contact.addresses()));
		String selection = buildSelection(addresses, fromDate, untilDate, true);
		
		Cursor c = db.rawQuery("SELECT SUM("+key+") from "+table+selection, null);
		if(c.moveToFirst()){
			return c.getLong(0);
		}
		else return 0;
	}

	
	
	//The below method is an alternative way of running multiQueries. It features more database hits and less sorting, but after some
	//basic benchmarking it proved to be the slower of the two methods. I'm leaving it here in case it ever proves worth coming back
	//to; it might serve as a model for something else or it has the slim potential to be more performant in cases where we fetch
	//huge numbers of text messages for only a few contacts, although I'd be surprised if the difference were enough to make it worth
	//using as a default.
	/*
	private HashMap<Contact, ArrayList<textMessage>> multiQueryDatabase(Contact[] contacts, String[] keys, long fromDate, long untilDate, String table){
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		if (contacts.length < 2) throw new dataException("multiQuery should not be used with less than 2 contacts.");
		if (keys.length == 0) throw new dataException("must request at least one value.");
		
		//Special case; this function needs to retrieve addresses regardless of whether they're
		//requested or not, so we add KEY_ADDRESS to keys if it's not there already
		boolean addressesRequested = false;
		for (int i = 0; i < keys.length; i++)
		{
			if (keys[i]==KEY_ADDRESS) addressesRequested = true;
		}
		if (!addressesRequested)
		{
			String [] temp = new String[keys.length+1];
			temp[keys.length]=KEY_ADDRESS;
			for (int i = 0; i < keys.length; i++)
			{
				temp[i]=keys[i];
			}
			keys = temp;
		}
		
		HashMap<Contact, ArrayList<textMessage>> ret = new HashMap<Contact, ArrayList<textMessage>>();
		
		int sent = (table == TABLE_SENT) ? 1 : 0;
		
		SQLiteDatabase db = getReadableDatabase();
		
		for (int i = 0; i < contacts.length; i++)
		{
			ArrayList<textMessage> contactMessages = new ArrayList<textMessage>();			
			String selection = buildSelection(new ArrayList<String>(Arrays.asList(contacts[i].addresses())), fromDate, untilDate, false);
			Cursor c = db.query(table, keys, selection, null, null, null, KEY_ADDRESS+", "+KEY_DATE+" "+SORT_DIRECTION);
			
			while(c.moveToNext()){
				contactMessages.add(textMessage.build(c, sent));	
			}
			
			ret.put(contacts[i], contactMessages);
			c.close();
		}
		

		db.close();
		return ret;
		
		
		
	}
	*/
	private HashMap<Contact, ArrayList<textMessage>> multiQuery(Contact[] contacts, String[] keys, long fromDate, long untilDate, String table)
	{		
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		if (contacts.length < 2) throw new dataException("multiQuery should not be used with less than 2 contacts.");
		if (keys.length == 0) throw new dataException("must request at least one value.");
		
		//Special case; this function needs to retrieve addresses regardless of whether they're
		//requested or not, so we add KEY_ADDRESS to keys if it's not there already
		boolean addressesRequested = false;
		for (int i = 0; i < keys.length; i++)
		{
			if (keys[i]==KEY_ADDRESS) addressesRequested = true;
		}
		if (!addressesRequested)
		{
			String [] temp = new String[keys.length+1];
			temp[keys.length]=KEY_ADDRESS;
			for (int i = 0; i < keys.length; i++)
			{
				temp[i]=keys[i];
			}
			keys = temp;
		}
		
		
		//Get all addresses, and map all of each contact's numbers to its ArrayList.
		ArrayList<String> addresses = new ArrayList<String>(contacts.length); //Probably won't be big enough, but closer to desired size
		HashMap<String, ArrayList<textMessage>> lists = new HashMap<String, ArrayList<textMessage>>();
		HashMap<Contact, ArrayList<textMessage>> ret = new HashMap<Contact, ArrayList<textMessage>>();
		for (int i = 0; i < contacts.length; i++)
		{
			//The same ArrayList is pointed to by both hash tables, but we only use the lists internally
			String[] contactAddresses = contacts[i].addresses();
			ArrayList<textMessage> contactMessages = new ArrayList<textMessage>();
			ret.put(contacts[i], contactMessages);
			for (int j = 0; j < contactAddresses.length; j++) {
				addresses.add(contactAddresses[j]);
				lists.put(contactAddresses[j], contactMessages);
			}
		}
		
		int sent = (table == TABLE_SENT) ? 1 : 0;
		
		SQLiteDatabase db = getReadableDatabase();
		
		String selection = buildSelection(addresses, fromDate, untilDate, false);
		Cursor c = db.query(table, keys, selection, null, null, null, KEY_ADDRESS+", "+KEY_DATE+" "+SORT_DIRECTION);

		int addressColumn = c.getColumnIndex(KEY_ADDRESS);

		while (c.moveToNext())
		{
			lists.get(c.getString(addressColumn)).add(textMessage.build(c, sent));
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
	 * Note this function always returns dates, regardless of whether they are requested.
	 * @param address Typically phone number or email
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public LinkedList<textMessage> queryConversation(Contact contact, String[] keys, long fromDate, long untilDate)
	{
		//same sort direction as the other two
		if (fromDate > untilDate) throw new dataException("fromDate must be <= untilDate.");
		if (keys.length == 0) throw new dataException("must request at least one value.");
		
		ArrayList<String> addresses = new ArrayList<String>(Arrays.asList(contact.addresses()));
		
		LinkedList<textMessage> res = new LinkedList<textMessage>();
		SQLiteDatabase db = getReadableDatabase();
		
		//Special case; this function needs to retrieve dates regardless of whether they're
		//requested or not
		boolean addressesRequested = false;
		for (int i = 0; i < keys.length; i++)
		{
			if (keys[i]==KEY_DATE) addressesRequested = true;
		}
		if (!addressesRequested)
		{
			String [] temp = new String[keys.length+1];
			temp[keys.length]=KEY_DATE;
			for (int i = 0; i < keys.length; i++)
			{
				temp[i]=keys[i];
			}
			keys = temp;
		}
		//The generated key/column "sent" is 1 for sent, 0 for received
		String columns;
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < keys.length; i++)
		{
			temp.append(keys[i]+",");
		}
		columns = temp.deleteCharAt(temp.length()-1).toString();
		String selection = buildSelection(addresses, fromDate, untilDate, true);
		String sql = 
				"SELECT "+columns+", 1 as "+GENERATED_KEY_SENT+" FROM "+TABLE_SENT+selection+
				" UNION "+
				"SELECT "+columns+", 0 as "+GENERATED_KEY_SENT+" FROM "+TABLE_RECEIVED+selection+
				" ORDER BY "+KEY_DATE+" "+SORT_DIRECTION;
		
		Cursor c = db.rawQuery(sql, null);
		int sentColumn = c.getColumnIndex(GENERATED_KEY_SENT);
		while (c.moveToNext())
		{
			res.offer(textMessage.build(c, c.getInt(sentColumn)));
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
	public ArrayList<textMessage> queryFromAll(String[] keys, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(null, keys, fromDate, untilDate, TABLE_RECEIVED);
	}
	
	/**
	 * Returns all sent messages between the two given dates. Pass -1 to void the date
	 * parameters and not use them. Returns come in the form of a sorted ArrayList with
	 * the most recent message at index 0.
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public ArrayList<textMessage> queryToAll(String[] keys, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(null, keys, fromDate, untilDate, TABLE_SENT);
	}
	
	/**
	 * Returns all messages received from a specific address between the two dates. Pass
	 * -1 to void the date parameters and not use them. Returns come in the form of a sorted
	 * ArrayList with the most recent message at index 0.
	 * @param address Typically phone number or email
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public ArrayList<textMessage> queryFromAddress(Contact contact, String[] keys, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(contact, keys, fromDate, untilDate, TABLE_RECEIVED);
	}
	
	/**
	 * Returns all messages sent to a specific address between the two dates. Pass
	 * -1 to void the date parameters and not use them. Returns come in the form of a sorted
	 * ArrayList with the most recent message at index 0.
	 * @param address Typically phone number or email
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	
	public ArrayList<textMessage> queryToAddress(Contact contact, String[] keys, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(contact, keys, fromDate, untilDate, TABLE_SENT);
	}
	
	/**
	 * Returns all messages received from specific addresses between the two dates. Pass
	 * -1 to void the date parameters and not use them. Returns come in the form of a HashMap
	 * of sorted ArrayLists, accessed using the relevant address as a key.
	 * Note that multiqueries will always return addresses regardless of whether or not they are requested.
	 * @param addresses An array of addresses
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	public HashMap<Contact, ArrayList<textMessage>> queryFromAddresses(Contact[] contacts, String[] keys, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return multiQuery(contacts, keys, fromDate, untilDate, TABLE_RECEIVED);
	}
	
	/**
	 * Returns all messages sent to specific addresses between the two dates. Pass
	 * -1 to void the date parameters and not use them. Returns come in the form of a HashMap
	 * of sorted ArrayLists, accessed using the relevant address as a key.
	 * Note that multiqueries will always return addresses regardless of whether or not they are requested.
	 * @param addresses An array of addresses
	 * @param fromDate Minimum date
	 * @param untilDate Maximum date
	 */
	
	public HashMap<Contact, ArrayList<textMessage>> queryToAddresses(Contact[] contacts, String[] keys, long fromDate, long untilDate)
	{
		//Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return multiQuery(contacts, keys, fromDate, untilDate, TABLE_SENT);
	}
	

}
