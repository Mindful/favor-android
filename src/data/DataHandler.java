package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.favor.ui.GraphActivity;
import com.favor.util.Contact;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.SparseArray;
import android.widget.Toast;

import static data.DataConstants.*;

class dataException extends RuntimeException {

	private static final long serialVersionUID = -2500275405542504803L;

	public dataException(String exc) {
		super(exc);
	}

	public String getMessage() {
		return super.getMessage();
	}

}

public class DataHandler extends SQLiteOpenHelper {
	// SQLite aspects
	private static final int DATABASE_VERSION = 1;
	
	private static final String JOIN_KEY_SENT = "sent";
	
	// Data table
	private static final String TABLE_DATA = "data";

	// Data table column names
	private static final String KEY_CONTACT_ID = "contact_id"; // Contact ID,  valid *most of the time
	// USE KEY_ADDRESS
	private static final String KEY_DATA_TYPE = "type";
	// USE KEY_DATE
	private static final String KEY_COUNT = "count";

	/**
	 * Format the address into something we can use (cleans up phone numbers)
	 */

	String formatAddress(String address) {
		if (!address.contains("@")) address = address.replaceAll("[^0-9]", "");
		return address;
	}

	// onCreate
	@Override
	public void onCreate(SQLiteDatabase db) {

		// Data table
		db.execSQL("CREATE TABLE " + TABLE_DATA + "(" + KEY_CONTACT_ID
				+ " TEXT," + KEY_DATA_TYPE + " INTEGER," + KEY_DATE
				+ " INTEGER," + KEY_COUNT + " INTEGER," + "PRIMARY KEY("
				+ KEY_CONTACT_ID + "," + KEY_DATA_TYPE + "))");
		
		for (MessageManager m : managers.values()){
			m.buildTables(db);
		}
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (MessageManager m : managers.values()){
			m.dropTables(db);
		}
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
		onCreate(db);
	}

	// Static aspects
	public static String PREFS_NAME = "dataPrefs.db";
	private static DataHandler singleton;

	/**
	 * Initializes and returns the singleton DataHandler instance. If a
	 * DataHandler is already running, a reference to that is returned.
	 * 
	 * @param mainActivity
	 *            The main activity, or a valid activity
	 */
	public static DataHandler initialize(Activity mainActivity) {
		if (singleton == null)
			singleton = new DataHandler(mainActivity);
		return singleton;
	}

	/**
	 * Returns a reference to the singleton DataHandler instance. Throws an
	 * exception if there is no initialized dataHandler instance.
	 */
	public static DataHandler get() {
		if (singleton == null)
			throw new dataException("Cannot get uninitialized DataHandler");
		return singleton;
	}

	private static final String SAVED_INDEXING = "indexing";

	// Instance aspects

	private final Context context;
	private final SharedPreferences prefs;
	private final SharedPreferences.Editor edit;
	
	private final HashMap<Type, MessageManager> managers;

	private ArrayList<Contact> contactsList;


	/**
	 * Utility method that provides easy access to the global application context.
	 */
	public Context context() {
		return context;
	}
	
	/**
	 * Utility method that provides easy access to the data related preferences store.
	 */
	public SharedPreferences prefs(){
		return prefs;
	}

	/**
	 * An unmodifiable list of contacts.
	 */
	public List<Contact> contacts() {
		return Collections.unmodifiableList(contactsList); 	// Not the prettiest, but people need not to be able to change it
	}

	private DataHandler(Activity mainActivity) {
		super(mainActivity.getApplicationContext(), "messages", null, DATABASE_VERSION);
		context = mainActivity.getApplicationContext();
		prefs = mainActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		edit = prefs.edit();
		managers = new HashMap<Type, MessageManager>();
		for (Type t: Type.values()){
			managers.put(t, MessageManager.getManager(t, this));
		}
	}
	
	/**
	 * Spits out the name of a message type.
	 */

	public String messageTypeName(Type t){
		return managers.get(t).name;
	}
	/**
	 * Returns true if indexing is enabled (or defaulted to true), else returns
	 * false.
	 */

	public boolean indexingEnabled() {
		return prefs.getBoolean(SAVED_INDEXING, true);
	}

	/**
	 * Enables indexing. This will build the indexes and save indexing as
	 * enabled, and can take a significant amount of time.
	 */
	public void enableIndexing() {
		if (indexingEnabled()) return;
		SQLiteDatabase db = getReadableDatabase();
		edit.putBoolean(SAVED_INDEXING, true);
		edit.apply();
		for (MessageManager m : managers.values()){
			m.indexTables(db);
		}
		db.close();
	}

	/**
	 * Disables indexing. This will drop built indexes and save indexing as
	 * disabled.
	 */

	public void disableIndexing() {
		if (!indexingEnabled()) return;
		edit.putBoolean(SAVED_INDEXING, false);
		edit.apply();
		SQLiteDatabase db = getReadableDatabase();
		for (MessageManager m : managers.values()){
			m.dropIndices(db);
		}
		db.close();
	}

	/**
	 * Updates our list of contacts by querying the phone's internal contacts
	 * representation.
	 */
	public void updateContacts() {
		Cursor contacts = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID },
				null, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC");

		contactsList = new ArrayList<Contact>(contacts.getCount());
		// This will be slightly larger than it actually needs to be, but is probably better than starting with a tiny ArrayList
		String curId = null, curName = null;
		ArrayList<String> buffer = new ArrayList<String>();
		//Loop through records for every number, assigning it to the proper contact
		while (contacts.moveToNext()) {
			// If using IDs seems overkill, remember that two contacts can have the same name
			String rowId = contacts.getString(2);
			if (!rowId.equals(curId)) {
				if (curId != null) {
					contactsList.add(new Contact(curName, curId, (String[]) buffer.toArray(new String[buffer.size()])));
					buffer.clear();
				}
				curId = rowId;
				curName = contacts.getString(0);
			}
			buffer.add(formatAddress(contacts.getString(1)));
		}
		// Have to handle the last contact after the loop terminates
		if (curId != null) contactsList.add(new Contact(curName, curId, (String[]) buffer.toArray(new String[buffer.size()])));
		Collections.sort(contactsList, new Comparator<Contact>() {
			@Override
			public int compare(Contact lhs, Contact rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}

		});
		contacts.close();
	}

	/**
	 * Updates the application's internal database by pulling all new messages
	 * since the last update call. Can potentially be long-running when called
	 * for the first time, or after a long delay.
	 */
	public void update() {
		// ---------------------------------
		// This is where we clean/update other held data
		GraphActivity.clearPrevContacts();
		// ----------------------------------
		updateContacts();
		// ----------------------------------
		long count = 0;
		for (MessageManager m : managers.values()){
			count += m.fetch();
		}
		if (count>0) Toast.makeText(context, "Fetched "+count+" new messages.", Toast.LENGTH_LONG).show();
		else Toast.makeText(context, "No new messages found.", Toast.LENGTH_LONG).show();
	}

	


	// Data Section
	public static final int DATA_SENT_CHARS = 1; // total characters sent to
													// this contact (long)
	public static final int DATA_RECEIVED_CHARS = 2; // total characters
														// received from this
														// contact (long)
	public static final int DATA_SEND_TIME = 3; // average response time to this
												// contact (longdate)
	public static final int DATA_RECEIVE_TIME = 4; // average response from this
													// contact (longdate)
	public static final int DATA_SENT_MMS = 5; // mms messages sent to this
												// contact (long)
	public static final int DATA_RECEIVED_MMS = 6; // mms messages received from
													// this contact (long)
	public static final int DATA_SENT_TOTAL = 7; // total messages (sms+mms)
													// sent to this contact
													// (long)
	public static final int DATA_RECEIVED_TOTAL = 8; // total messages (sms+mms)
														// received from this
														// contact (long)

	private void validDate(int type) {
		if (type <= 0 || type >= 9)
			throw new dataException(
					"Invalid data type. Please use class constants.");
	}

	private String buildSelection(ArrayList<String> addresses, long fromDate,
			long untilDate, boolean raw) {
		StringBuilder selection = new StringBuilder();
		if (addresses == null) {
			// Nothing to do with addresses
		} else if (addresses.size() == 1) {
			if (raw)
				selection.append(" WHERE ");
			selection.append(KEY_ADDRESS).append("=").append(addresses.get(0));
		} else {
			if (raw)
				selection.append(" WHERE ");
			selection.append("(");
			for (int i = 0; i < addresses.size(); i++) {
				selection.append(KEY_ADDRESS).append("=")
						.append(addresses.get(i));
				if (i < addresses.size() - 1)
					selection.append(" OR ");
				else
					selection.append(")");
			}
		}
		if (fromDate > -1) {
			if (selection.length() > 0)
				selection.append(" AND ");
			selection.append(KEY_DATE).append(">=").append(fromDate);
		}
		if (untilDate > -1) {
			if (selection.length() > 0)
				selection.append(" AND ");
			selection.append(KEY_DATE).append("<=").append(untilDate);
		}
		return selection.toString();

	}

	/**
	 * Loads data about a user. Data types are listed as class constants in the
	 * form DATA_xxxxx. Returns a 2-long array with [data, time]
	 * If no data is found, data and count will be -1.
	 * 
	 * @param address
	 *            The address of the user you are saving data about.
	 * @param type
	 *            The data type you wish to save. Use class constants
	 *            (DATA_xxxxx)
	 * 
	 */
	public long[] getData(Contact contact, int type) {
		validDate(type);
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_DATA, new String[] { KEY_COUNT, KEY_DATE },
				KEY_CONTACT_ID + "=" + contact.id() + " AND " + KEY_DATA_TYPE
						+ "=" + type, null, null, null, null);
		if (c.getCount() == 0) return new long[]{-1, -1};
		else if (c.getCount() > 1) throw new dataException("getData producing multiple results.");
		c.close();
		return new long[]{c.getLong(0), c.getLong(1)};
	}

	/**
	 * Loads all data about a user, returning anything found in a SparseArray,
	 * with data mapped to its type. Data types are listed as class constants in
	 * the form DATA_xxxxx. Returns -1 if no data is found. Data is in the typical
	 * format of a 2-long array with [data, time].
	 * 
	 * @param address
	 *            The address of the user you are saving data about.
	 * 
	 */

	public SparseArray<long[]> getAllData(Contact contact) {
		SparseArray<long[]> ret = new SparseArray<long[]>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_DATA, new String[] { KEY_DATA_TYPE,
				KEY_COUNT, KEY_DATE }, KEY_CONTACT_ID + "=" + contact.id(),
				null, null, null, null);
		while (c.moveToNext()) {
			ret.put(c.getInt(0), new long[]{c.getLong(1), c.getLong(2)});
		}
		c.close();
		return ret;

	}

	/**
	 * Saves data of a given type about a user. Data types are listed as class
	 * constants in the form DATA_xxxx. Also, data MUST BE SAVED IMMEDIATELY
	 * after it is calculated so that the savedate is appropriate. Do not
	 * preserve results and save them later.
	 * 
	 * @param address
	 *            The address of the user you are saving data about.
	 * @param type
	 *            The data type you wish to save. Use class constants
	 *            (DATA_xxxxx)
	 * 
	 */
	public void saveData(Contact contact, int type, long data) {
		validDate(type);
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_CONTACT_ID, contact.id());
		values.put(KEY_DATA_TYPE, type);
		values.put(KEY_DATE, new Date().getTime());
		values.put(KEY_COUNT, data);
		db.insert(TABLE_DATA, null, values);
	}

	// Message Section

	private static final String SORT_DIRECTION = "DESC"; // or ASC for most
															// recent message
															// last

	private ArrayList<Message> query(Contact contact, String[] keys,
			long fromDate, long untilDate, boolean sentTable, Type type) {
		// -1 for no date, null contact for no addresses. Obviously, table is
		// mandatory.
		// Automatically sorted by date.
		if (fromDate > untilDate)
			throw new dataException("fromDate must be <= untilDate.");
		if (keys.length == 0)
			throw new dataException("must request at least one value.");
		int sent = sentTable ? 1 : 0;
		String table = managers.get(type).tableName(sentTable);

		SQLiteDatabase db = getReadableDatabase();
		ArrayList<String> addresses = null;
		if (contact != null)
			addresses = new ArrayList<String>(
					Arrays.asList(contact.addresses()));
		String selection = buildSelection(addresses, fromDate, untilDate, false);

		Cursor c = db.query(table, keys, selection, null, null, null, KEY_DATE
				+ " " + SORT_DIRECTION);
		ArrayList<Message> ret = new ArrayList<Message>(c.getCount());
		while (c.moveToNext()) {
			ret.add(Message.build(c, sent, type));
		}
		c.close();
		db.close();
		return ret;
	}

	public double average(Contact contact, String key, long fromDate,
			long untilDate, boolean sentTable, Type type) {
		// -1 for no date, null contact for no addresses. Obviously, table is
		// mandatory.
		// Automatically sorted by date.
		if (fromDate > untilDate)
			throw new dataException("fromDate must be <= untilDate.");
		String table = managers.get(type).tableName(sentTable);
		SQLiteDatabase db = getReadableDatabase();
		ArrayList<String> addresses = new ArrayList<String>(
				Arrays.asList(contact.addresses()));
		String selection = buildSelection(addresses, fromDate, untilDate, true);

		Cursor c = db.rawQuery("SELECT AVG(" + key + ") from " + table + selection, null);
		double ret = c.moveToFirst() ? c.getDouble(0) : 0.00d;
		c.close();
		return ret;
	}

	public long sum(Contact contact, String key, long fromDate, long untilDate,
			boolean sentTable, Type type) {
		// -1 for no date, null contact for no addresses. Obviously, table is
		// mandatory.
		// Automatically sorted by date.
		if (fromDate > untilDate)
			throw new dataException("fromDate must be <= untilDate.");
		String table = managers.get(type).tableName(sentTable);
		SQLiteDatabase db = getReadableDatabase();
		ArrayList<String> addresses = new ArrayList<String>(
				Arrays.asList(contact.addresses()));
		String selection = buildSelection(addresses, fromDate, untilDate, true);

		Cursor c = db.rawQuery("SELECT SUM(" + key + ") from " + table
				+ selection, null);
		long ret = c.moveToFirst() ? c.getLong(0) : 0l;
		c.close();
		return ret;
	}

	// The below method is an alternative way of running multiQueries. It
	// features more database hits and less sorting, but after some
	// basic benchmarking it proved to be the slower of the two methods. I'm
	// leaving it here in case it ever proves worth coming back
	// to; it might serve as a model for something else or it has the slim
	// potential to be more performant in cases where we fetch
	// huge numbers of text messages for only a few contacts, although I'd be
	// surprised if the difference were enough to make it worth
	// using as a default.
	/*
	 * private HashMap<Contact, ArrayList<textMessage>>
	 * multiQueryDatabase(Contact[] contacts, String[] keys, long fromDate, long
	 * untilDate, String table){ if (fromDate > untilDate) throw new
	 * dataException("fromDate must be <= untilDate."); if (contacts.length < 2)
	 * throw new
	 * dataException("multiQuery should not be used with less than 2 contacts."
	 * ); if (keys.length == 0) throw new
	 * dataException("must request at least one value.");
	 * 
	 * //Special case; this function needs to retrieve addresses regardless of
	 * whether they're //requested or not, so we add KEY_ADDRESS to keys if it's
	 * not there already boolean addressesRequested = false; for (int i = 0; i <
	 * keys.length; i++) { if (keys[i]==KEY_ADDRESS) addressesRequested = true;
	 * } if (!addressesRequested) { String [] temp = new String[keys.length+1];
	 * temp[keys.length]=KEY_ADDRESS; for (int i = 0; i < keys.length; i++) {
	 * temp[i]=keys[i]; } keys = temp; }
	 * 
	 * HashMap<Contact, ArrayList<textMessage>> ret = new HashMap<Contact,
	 * ArrayList<textMessage>>();
	 * 
	 * int sent = (table == TABLE_SENT) ? 1 : 0;
	 * 
	 * SQLiteDatabase db = getReadableDatabase();
	 * 
	 * for (int i = 0; i < contacts.length; i++) { ArrayList<textMessage>
	 * contactMessages = new ArrayList<textMessage>(); String selection =
	 * buildSelection(new
	 * ArrayList<String>(Arrays.asList(contacts[i].addresses())), fromDate,
	 * untilDate, false); Cursor c = db.query(table, keys, selection, null,
	 * null, null, KEY_ADDRESS+", "+KEY_DATE+" "+SORT_DIRECTION);
	 * 
	 * while(c.moveToNext()){ contactMessages.add(textMessage.build(c, sent)); }
	 * 
	 * ret.put(contacts[i], contactMessages); c.close(); }
	 * 
	 * 
	 * db.close(); return ret;
	 * 
	 * 
	 * 
	 * }
	 */
	private HashMap<Contact, ArrayList<Message>> multiQuery(
			Contact[] contacts, String[] keys, long fromDate, long untilDate,
			boolean sentTable, Type type) {
		if (fromDate > untilDate)
			throw new dataException("fromDate must be <= untilDate.");
		if (contacts.length < 2)
			throw new dataException(
					"multiQuery should not be used with less than 2 contacts.");
		if (keys.length == 0)
			throw new dataException("must request at least one value.");

		// Special case; this function needs to retrieve addresses regardless of
		// whether they're
		// requested or not, so we add KEY_ADDRESS to keys if it's not there
		// already
		boolean addressesRequested = false;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == KEY_ADDRESS)
				addressesRequested = true;
		}
		if (!addressesRequested) {
			String[] temp = new String[keys.length + 1];
			temp[keys.length] = KEY_ADDRESS;
			for (int i = 0; i < keys.length; i++) {
				temp[i] = keys[i];
			}
			keys = temp;
		}

		// Get all addresses, and map all of each contact's numbers to its
		// ArrayList.
		ArrayList<String> addresses = new ArrayList<String>(contacts.length); // Probably won't be big enough,
		//but closer to the desired size
		HashMap<String, ArrayList<Message>> lists = new HashMap<String, ArrayList<Message>>();
		HashMap<Contact, ArrayList<Message>> ret = new HashMap<Contact, ArrayList<Message>>();
		for (int i = 0; i < contacts.length; i++) {
			// The same ArrayList is pointed to by both hash tables, but we only
			// use the lists internally
			String[] contactAddresses = contacts[i].addresses();
			ArrayList<Message> contactMessages = new ArrayList<Message>();
			ret.put(contacts[i], contactMessages);
			for (int j = 0; j < contactAddresses.length; j++) {
				addresses.add(contactAddresses[j]);
				lists.put(contactAddresses[j], contactMessages);
			}
		}

		int sent = sentTable ? 1 : 0;
		String table = managers.get(type).tableName(sentTable);

		SQLiteDatabase db = getReadableDatabase();

		String selection = buildSelection(addresses, fromDate, untilDate, false);
		Cursor c = db.query(table, keys, selection, null, null, null,
				KEY_ADDRESS + ", " + KEY_DATE + " " + SORT_DIRECTION);

		int addressColumn = c.getColumnIndex(KEY_ADDRESS);

		while (c.moveToNext()) {
			lists.get(c.getString(addressColumn)).add(
					Message.build(c, sent, type));
		}

		c.close();
		db.close();
		return ret;
	}

	// basically, this returns the messages sent to and received by the user to
	// a given address, and puts them in a que (or deque)
	// this exists for the purpose of computing response time, which we do by
	// moving through the que, discarding any consecutively sent or received
	// messages
	// so that response time is always first to first. then we look at all the
	// response times and use statistics to discard outliers (based on standard
	// dev)
	// worth remembering that LinkedLists inheret the (slightly) MORE EFFICIENT
	// Queue functions
	/**
	 * Returns all messages to and from a specific address between the two given
	 * dates. Pass -1 to void date parameters and not use them. Returns come in
	 * the form of a sorted LinkedList with the most recent message at the head.
	 * Note this function always returns dates, regardless of whether they are
	 * requested.
	 * 
	 * @param address
	 *            Typically phone number or email
	 * @param fromDate
	 *            Minimum date
	 * @param untilDate
	 *            Maximum date
	 */
	public LinkedList<Message> queryConversation(Contact contact,
			String[] keys, long fromDate, long untilDate, Type type) {
		// same sort direction as the other two
		if (fromDate > untilDate)
			throw new dataException("fromDate must be <= untilDate.");
		if (keys.length == 0)
			throw new dataException("must request at least one value.");

		ArrayList<String> addresses = new ArrayList<String>(
				Arrays.asList(contact.addresses()));

		LinkedList<Message> res = new LinkedList<Message>();
		SQLiteDatabase db = getReadableDatabase();

		// Special case; this function needs to retrieve dates regardless of
		// whether they're
		// requested or not
		boolean addressesRequested = false;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == KEY_DATE)
				addressesRequested = true;
		}
		if (!addressesRequested) {
			String[] temp = new String[keys.length + 1];
			temp[keys.length] = KEY_DATE;
			for (int i = 0; i < keys.length; i++) {
				temp[i] = keys[i];
			}
			keys = temp;
		}
		// The generated key/column "sent" is 1 for sent, 0 for received
		String columns;
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			temp.append(keys[i] + ",");
		}
		columns = temp.deleteCharAt(temp.length() - 1).toString();
		String selection = buildSelection(addresses, fromDate, untilDate, true);
		String sql = "SELECT " + columns + ", 1 as " + DataHandler.JOIN_KEY_SENT
				+ " FROM " + TABLE_SENT + selection + " UNION " + "SELECT "
				+ columns + ", 0 as " + DataHandler.JOIN_KEY_SENT + " FROM "
				+ TABLE_RECEIVED + selection + " ORDER BY " + KEY_DATE + " "
				+ SORT_DIRECTION;

		Cursor c = db.rawQuery(sql, null);
		int sentColumn = c.getColumnIndex(DataHandler.JOIN_KEY_SENT);
		while (c.moveToNext()) res.offer(Message.build(c, c.getInt(sentColumn), type));
		c.close();
		return res;
	}

	/**
	 * Returns all received messages between the two given dates. Pass -1 to
	 * void the date parameters and not use them. Returns come in the form of a
	 * sorted ArrayList with the most recent message at index 0.
	 * 
	 * @param fromDate
	 *            Minimum date
	 * @param untilDate
	 *            Maximum date
	 */
	public ArrayList<Message> queryFromAll(String[] keys, long fromDate,
			long untilDate, Type type) {
		// Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(null, keys, fromDate, untilDate, false, type);
	}

	/**
	 * Returns all sent messages between the two given dates. Pass -1 to void
	 * the date parameters and not use them. Returns come in the form of a
	 * sorted ArrayList with the most recent message at index 0.
	 * 
	 * @param fromDate
	 *            Minimum date
	 * @param untilDate
	 *            Maximum date
	 */
	public ArrayList<Message> queryToAll(String[] keys, long fromDate,
			long untilDate, Type type) {
		// Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(null, keys, fromDate, untilDate, true, type);
	}

	/**
	 * Returns all messages received from a specific address between the two
	 * dates. Pass -1 to void the date parameters and not use them. Returns come
	 * in the form of a sorted ArrayList with the most recent message at index
	 * 0.
	 * 
	 * @param address
	 *            Typically phone number or email
	 * @param fromDate
	 *            Minimum date
	 * @param untilDate
	 *            Maximum date
	 */
	public ArrayList<Message> queryFromAddress(Contact contact,
			String[] keys, long fromDate, long untilDate, Type type) {
		// Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(contact, keys, fromDate, untilDate, false, type);
	}

	/**
	 * Returns all messages sent to a specific address between the two dates.
	 * Pass -1 to void the date parameters and not use them. Returns come in the
	 * form of a sorted ArrayList with the most recent message at index 0.
	 * 
	 * @param address
	 *            Typically phone number or email
	 * @param fromDate
	 *            Minimum date
	 * @param untilDate
	 *            Maximum date
	 */

	public ArrayList<Message> queryToAddress(Contact contact,
			String[] keys, long fromDate, long untilDate, Type type) {
		// Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return query(contact, keys, fromDate, untilDate, true, type);
	}

	/**
	 * Returns all messages received from specific addresses between the two
	 * dates. Pass -1 to void the date parameters and not use them. Returns come
	 * in the form of a HashMap of sorted ArrayLists, accessed using the
	 * relevant address as a key. Note that multiqueries will always return
	 * addresses regardless of whether or not they are requested.
	 * 
	 * @param addresses
	 *            An array of addresses
	 * @param fromDate
	 *            Minimum date
	 * @param untilDate
	 *            Maximum date
	 */
	public HashMap<Contact, ArrayList<Message>> queryFromAddresses(
			Contact[] contacts, String[] keys, long fromDate, long untilDate,
			Type type) {
		// Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return multiQuery(contacts, keys, fromDate, untilDate, false, type);
	}

	/**
	 * Returns all messages sent to specific addresses between the two dates.
	 * Pass -1 to void the date parameters and not use them. Returns come in the
	 * form of a HashMap of sorted ArrayLists, accessed using the relevant
	 * address as a key. Note that multiqueries will always return addresses
	 * regardless of whether or not they are requested.
	 * 
	 * @param addresses
	 *            An array of addresses
	 * @param fromDate
	 *            Minimum date
	 * @param untilDate
	 *            Maximum date
	 */

	public HashMap<Contact, ArrayList<Message>> queryToAddresses(
			Contact[] contacts, String[] keys, long fromDate, long untilDate,
			Type type) {
		// Pass -1 for no date limits. Do not pass 0 unless you mean 0.
		return multiQuery(contacts, keys, fromDate, untilDate, true, type);
	}

}
