package data;

import java.util.Date;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;



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
		
		public long getLastFetch(){
			return dh.prefs().getLong("lastFetch"+name, 0);
		}
		
		
		
		abstract void fetch();
		
		void exportMessage(boolean sent, long id, long date, String address, String msg, int media){
			if(db==null) throw new dataException("Cannot export messages to database without open transaction.");
			ContentValues row = new ContentValues();
			row.put(DataConstants.KEY_ID, id);
			row.put(DataConstants.KEY_DATE, date);
			row.put(DataConstants.KEY_ADDRESS, formatAddress(address, false));
			row.put(DataConstants.KEY_CHARCOUNT, msg.length());
			row.put(DataConstants.KEY_MEDIA, media);
			String table = (sent ? DataConstants.TABLE_SENT : DataConstants.TABLE_RECEIVED)+name;
			db.insert(table, null, row);
		}
		
		abstract String formatAddress(String address);

		
		
		void beginTransaction(){
			if(db!=null) throw new dataException("Transactions should not be started with a transaction open");
			else {
				db = dh.getWritableDatabase();
				success = false;
				db.beginTransaction();
			}
		}
		
		void successfulTransaction(long lastFetch){
			if(db==null) throw new dataException("Cannot mark transaction successful without open transaction.");
			db.setTransactionSuccessful();
			success = true;
			dh.prefs().edit().putLong("lastFetch"+name, lastFetch).commit();
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
