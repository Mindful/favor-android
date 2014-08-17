package com.favor.data;

import com.almworks.sqlite4java.SQLiteException;
import com.favor.util.Logger;

import android.content.ContentValues;

import static com.favor.data.DataConstants.*;

//TODO: Does java use the "final" keyweord (before public, its seems like) to disallow overwriting functions?
//if so, a lot of these should be final

public abstract class MessageManager {
	
		protected final DataHandler dh;
		protected final String savedValues[];
		final Type type;
		final String name;
		
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
			this.savedValues = new String[] {};
		}
		
		protected MessageManager(Type type, String name, DataHandler dh, String[] savedValues){
			this.dh = dh;
			this.type = type;
			this.name = name;
			this.savedValues = savedValues;
		}
		
		final protected long getLong(String varName, long def){
			return dh.prefs().getLong(varName+name, def);
		}
		
		final protected void putLong(String varName, long l){
			dh.prefs().edit().putLong(varName+name, l).commit();
		}
		
		final protected void removeLong(String varName){
			//dh.prefs().edit().remove(varName).commit(); //This method doesn't appear to work?
			dh.prefs().edit().putLong(varName+name, 0).commit();
		}
		
		final public long getLastFetch(){
			return getLong("lastFetch", 0);
		}
		
		final public String tableName(boolean sent){
			return (sent ? TABLE_SENT : TABLE_RECEIVED)+name;
		}
		
		//Take an external database reference in the next 4 methods, both for locking safety reasons and because they're all
		//called in batches. This is minor but important.
		
		private String tableCreationStatement(boolean sent){
			if (sent){
				return "CREATE TABLE " + tableName(true) + "(" + KEY_ID + " INTEGER,"
						+ KEY_DATE + " INTEGER," + KEY_ADDRESS 
						+ " TEXT," + KEY_CHARCOUNT + " INTEGER," 
						+ KEY_MEDIA + " INTEGER, "+ sentTableEndingStatement() + ")";
			} else {
				return "CREATE TABLE " + tableName(false) + "(" + KEY_ID + " INTEGER,"
						+ KEY_DATE + " INTEGER," + KEY_ADDRESS + 
						" TEXT," + KEY_CHARCOUNT + " INTEGER,"
						+ KEY_MEDIA + " INTEGER, " + receivedTableEndingStatement() + ")";
			}
		}
		
		final void buildTables(){
			try{				
				// Sent table
				dh.exec(tableCreationStatement(true));

				// Received Table
				dh.exec(tableCreationStatement(false));

				// Tables are indexed by address and then date for query optimization
				// http://stackoverflow.com/questions/15732713/column-index-order-sqlite-creates-table
				// Indicates that "order depends on projection i.e. select name, lastname from table"
				if (dh.indexingEnabled()) {
					dh.exec("CREATE INDEX i_" + tableName(true) + " ON " + tableName(true)
							+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
					dh.exec("CREATE INDEX i_" + tableName(false) + " ON " + tableName(true)
							+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
				}
			} catch (SQLiteException e){
				Logger.exception(name + " message manager could not construct tables", e);
			}
		}
		
		final public void dropTables(){
			try {
				dh.exec("DROP TABLE IF EXISTS " + tableName(true));
				dh.exec("DROP TABLE IF EXISTS " + tableName(false));
			} catch (SQLiteException e) {
				Logger.exception(name + " message manager could not drop tables", e);
			}
			removeLong("lastFetch");
			for (String s : savedValues) removeLong(s);
		}
		
		final protected void truncateTable(boolean sent){
			try {
				dh.exec("DROP TABLE IF EXISTS "+tableName(sent));
				dh.exec(tableCreationStatement(sent));
			} catch (SQLiteException e) {
				Logger.exception(name + " message manager could not truncate tables", e);
			}
		}
		
		final public void indexTables(){
			try {
				dh.exec("CREATE INDEX i_" + tableName(true) + " ON " + tableName(true) 
						+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
				dh.exec("CREATE INDEX i_" + tableName(false) + " ON " + tableName(false)
						+ " (" + KEY_ADDRESS + "," + KEY_DATE + ")");
			} catch (SQLiteException e) {
				Logger.exception(name + " message manager could not index tables", e);
			}
		}
		
		final public void dropIndices(){
			try {
				dh.exec("DROP INDEX IF EXISTS i_" + tableName(true));
				dh.exec("DROP INDEX IF EXISTS i_" + tableName(false));
			} catch (SQLiteException e) {
				Logger.exception(name + " message manager could not drop indices", e);
			}
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
			ContentValues row = new ContentValues();
			row.put(KEY_ID, id);
			row.put(KEY_DATE, date);
			row.put(KEY_ADDRESS, formatAddress(address));
			row.put(KEY_CHARCOUNT, msg.length());
			row.put(KEY_MEDIA, media);
			String table = tableName(sent);
		}
		
		abstract String formatAddress(String address);

		
}
