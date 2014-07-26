package data;

import java.util.Date;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import static data.DataHandler.KEY_ADDRESS;
import static data.DataHandler.KEY_ID;
import static data.DataHandler.KEY_DATE;
import static data.DataHandler.KEY_ADDRESS;
import static data.DataHandler.KEY_CHARCOUNT;
import static data.DataHandler.KEY_MEDIA;

public abstract class MessageManager {
	
		protected long lastFetch;
		protected long lastID;
		
		protected final int type;
		protected final String name;
		protected final DataHandler dh;

		private boolean success = false;
		private SQLiteDatabase db;
		
		
		protected MessageManager(int type, String name){
			//Register with the datahandler, and throw an exception if something of this type is already registered?
			dh = DataHandler.get();
			lastFetch = getLastFetch();
			lastID = getLastID();
			this.type = type;
			this.name = name;
		}
		
		public long getLastFetch(){
			return dh.prefs().getLong("lastFetch"+name, 0);
		}
		
		protected long getLastID(){
			return dh.prefs().getLong("lastID"+name, 0);
		}
		
		
		abstract void fetch();
		
		void exportMessage(boolean sent, long id, long date, String address, String msg, int media){
			if(db==null) throw new dataException("Cannot export messages to database without open transaction.");
			ContentValues row = new ContentValues();
			row.put(KEY_ID, id);
			row.put(KEY_DATE, date);
			row.put(KEY_ADDRESS, address);
			row.put(KEY_CHARCOUNT, msg.length());
			row.put(KEY_MEDIA, media);
			//actual export code
		}

		
		
		void beginTransaction(){
			if(db!=null) throw new dataException("Transactions should not be started with a transaction open");
			else {
				db = dh.getWritableDatabase();
				success = false;
				db.beginTransaction();
			}
		}
		
		void successfulTransaction(long lastID){
			if(db==null) throw new dataException("Cannot mark transaction successful without open transaction.");
			db.setTransactionSuccessful();
			success = true;
			dh.prefs().edit().putLong("lastFetch"+name, new Date().getTime()).commit();
			dh.prefs().edit().putLong("lastID"+name, new Date().getTime()).commit();
		}
		
		void endTransaction(){
			if(db==null) throw new dataException("Cannot end transaction without open transaction.");
			//if(!success) throw new dataException("Transaction unsuccessful");
			//TODO: Warning here? What? Log something?
			
			db.endTransaction();
			db.close();
			db=null;
		}
		
}
