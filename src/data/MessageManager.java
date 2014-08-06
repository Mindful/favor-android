package data;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.favor.util.Logger;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import static data.DataConstants.*;

//TODO: Does java use the "final" keyweord (before public, its seems like) to disallow overwriting functions?
//if so, a lot of these should be final

public abstract class MessageManager {
	
		protected final DataHandler dh;
		final Type type;
		final String name;

		private boolean success = false;
		private SQLiteDatabase db = null;
		
		final static MessageManager getManager(Type type, DataHandler dh){
			switch(type){
			case TYPE_TEXT:
				return new TextManager(dh);
			case TYPE_EMAIL:
				return new EmailManager(dh);
			default:
				throw new dataException("This should never happen.");
			}
		}
		
		protected MessageManager(Type type, String name, DataHandler dh){
			this.dh = dh;
			this.type = type;
			this.name = name;
		}
		
		final protected long getLong(String varName, long def){
			return dh.prefs().getLong(varName+name, def);
		}
		
		final protected void setLong(String varName, long l){
			dh.prefs().edit().putLong(varName+name, l).commit();
		}
		
		final public long getLastFetch(){
			return dh.prefs().getLong("lastFetch"+name, 0);
		}
		
		final public String tableName(boolean sent){
			return (sent ? TABLE_SENT : TABLE_RECEIVED)+name;
		}
		
		//Take an external database reference in the next 4 methods, both for locking safety reasons and becuase they're all
		//called in batches. This is minor but important.
		
		final void buildTables(SQLiteDatabase external){

			// Sent table
			external.execSQL("CREATE TABLE " + tableName(true) + "(" + KEY_ID + " INTEGER,"
					+ KEY_DATE + " INTEGER," + KEY_ADDRESS 
					+ " TEXT," + KEY_CHARCOUNT + " INTEGER," 
					+ KEY_MEDIA + " INTEGER, "+ sentTableEndingStatement() + ")");

			// Received Table
			external.execSQL("CREATE TABLE " + tableName(false) + "(" + KEY_ID + " INTEGER,"
					+ KEY_DATE + " INTEGER," + KEY_ADDRESS + 
					" TEXT," + KEY_CHARCOUNT + " INTEGER,"
					+ KEY_MEDIA + " INTEGER, " + receivedTableEndingStatement() + ")");

			// Tables are indexed by address and then date for query optimization
			// http://stackoverflow.com/questions/15732713/column-index-order-sqlite-creates-table
			// Indicates that "order depends on projection i.e. select name, lastname from table"
			if (dh.indexingEnabled()) {
				external.execSQL("CREATE INDEX i_" + tableName(true) + " ON " + tableName(true)
						+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
				external.execSQL("CREATE INDEX i_" + tableName(false) + " ON " + tableName(true)
						+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
			}
		}
		
		final public void dropTables(SQLiteDatabase external){
			external.execSQL("DROP TABLE IF EXISTS " + tableName(true));
			external.execSQL("DROP TABLE IF EXISTS " + tableName(false));
			dh.prefs().edit().putLong("lastFetch"+name, 0).commit();
		}
		
		final public void indexTables(SQLiteDatabase external){
			external.execSQL("CREATE INDEX i_" + tableName(true) + " ON " + tableName(true) 
					+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
			external.execSQL("CREATE INDEX i_" + tableName(false) + " ON " + tableName(false)
					+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
		}
		
		final public void dropIndices(SQLiteDatabase external){
			external.execSQL("DROP INDEX IF EXISTS i_" + tableName(true));
			external.execSQL("DROP INDEX IF EXISTS i_" + tableName(false));
		}
		
		
		//private methods are auto-final, so this has to be protected
		protected String sentTableEndingStatement(){
			return "PRIMARY KEY ("+KEY_ID+")";
		}
		
		protected String receivedTableEndingStatement(){
			return "PRIMARY KEY ("+KEY_ID+")";
		}
		
		
		
		
		
		abstract long fetch();
		
		final protected void exportMessage(boolean sent, long id, long date, String address, String msg, int media){
			if(db==null) throw new dataException("Cannot export messages to database without open transaction.");
			ContentValues row = new ContentValues();
			row.put(KEY_ID, id);
			row.put(KEY_DATE, date);
			row.put(KEY_ADDRESS, formatAddress(address));
			row.put(KEY_CHARCOUNT, msg.length());
			row.put(KEY_MEDIA, media);
			String table = tableName(sent);
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
		
		final protected void successfulTransaction(){
			if(db==null) throw new dataException("Cannot mark transaction successful without open transaction.");
			db.setTransactionSuccessful();
			success = true;
			dh.prefs().edit().putLong("lastFetch"+name, new Date().getTime()).commit();
		}
		
		@SuppressLint("SimpleDateFormat")
		final protected void endTransaction(){
			if(db==null) throw new dataException("Cannot end transaction without open transaction.");
			if(!success) Logger.error(name+" message manager transaction unsuccessful at "+ new SimpleDateFormat().format(new Date()));
			
			db.endTransaction();
			db.close();
			db=null;
		}
		
}
