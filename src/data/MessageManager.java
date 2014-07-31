package data;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.favor.util.Misc;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import static data.DataConstants.*;

//TODO: Does java use the "final" keyweord (before public, its seems like) to disallow overwriting functions?
//if so, a lot of these should be final

public abstract class MessageManager {
	
		protected long lastFetch;
		protected final DataHandler dh;
		final int type;
		final String name;

		private boolean success = false;
		private SQLiteDatabase db;
		
		
		protected MessageManager(int type, String name){
			//Register with the datahandler, and throw an exception if something of this type is already registered?
			dh = DataHandler.get();
			lastFetch = getLastFetch();
			this.type = type;
			this.name = name;
		}
		
		final public long getLastFetch(){
			return dh.prefs().getLong("lastFetch"+name, 0);
		}
		
		
		final void buildTable(){
			db = dh.getWritableDatabase();

			//TODO: use table ending statement method
			// Sent table
			// Unique to a combo of ID and address, to allow for outgoing duplicates
			db.execSQL("CREATE TABLE " + TABLE_SENT + name + "(" + KEY_ID + " INTEGER,"
					+ KEY_DATE + " INTEGER," + KEY_ADDRESS 
					+ " TEXT," + KEY_CHARCOUNT + " INTEGER," 
					+ KEY_MEDIA + " INTEGER, "+ sentTableEndingStatement() + ")");

			// Received Table
			db.execSQL("CREATE TABLE " + TABLE_RECEIVED + name + "(" + KEY_ID + " INTEGER,"
					+ KEY_DATE + " INTEGER," + KEY_ADDRESS + 
					" TEXT," + KEY_CHARCOUNT + " INTEGER,"
					+ KEY_MEDIA + " INTEGER, " + receivedTableEndingStatement() + ")");

			// Tables are indexed by address and then date for query optimization
			// http://stackoverflow.com/questions/15732713/column-index-order-sqlite-creates-table
			// Indicates that "order depends on projection i.e. select name, lastname from table"
			if (dh.indexingEnabled()) {
				db.execSQL("CREATE INDEX i_" + TABLE_SENT + name + " ON " + TABLE_SENT
						+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
				db.execSQL("CREATE INDEX i_" + TABLE_RECEIVED + name + " ON " + TABLE_RECEIVED
						+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
			}
			db = null;
		}
		
		//private methods are auto-final, so this has to be protected
		protected String sentTableEndingStatement(){
			return "PRIMARY KEY ("+KEY_ID+")";
		}
		
		protected String receivedTableEndingStatement(){
			return "PRIMARY KEY ("+KEY_ID+")";
		}
		
		
		
		
		
		abstract void fetch();
		
		final protected void exportMessage(boolean sent, long id, long date, String address, String msg, int media){
			if(db==null) throw new dataException("Cannot export messages to database without open transaction.");
			ContentValues row = new ContentValues();
			row.put(KEY_ID, id);
			row.put(KEY_DATE, date);
			row.put(KEY_ADDRESS, formatAddress(address));
			row.put(KEY_CHARCOUNT, msg.length());
			row.put(KEY_MEDIA, media);
			String table = (sent ? TABLE_SENT : TABLE_RECEIVED)+name;
			db.insert(table, null, row);
		}
		
		abstract String formatAddress(String address);

		
		
		final protected void beginTransaction(){
			if(db!=null) throw new dataException("Transactions should not be started with a transaction open");
			else {
				db = dh.getWritableDatabase();
				success = false;
				db.beginTransaction();
			}
		}
		
		final protected void successfulTransaction(long lastFetch){
			if(db==null) throw new dataException("Cannot mark transaction successful without open transaction.");
			db.setTransactionSuccessful();
			success = true;
			dh.prefs().edit().putLong("lastFetch"+name, lastFetch).commit();
		}
		
		@SuppressLint("SimpleDateFormat")
		final protected void endTransaction(){
			if(db==null) throw new dataException("Cannot end transaction without open transaction.");
			if(!success)
				Misc.logError(name+" message manager transaction unsuccessful at"+ new SimpleDateFormat().format(new Date()));
			
			db.endTransaction();
			db.close();
			db=null;
		}
		
}
