package data;

import android.database.sqlite.SQLiteDatabase;

public abstract class MessageManager {
		protected long lastFetch;
		protected final int messageType;
		protected final String messageName;
		protected DataHandler dh;
		private boolean transacting = false;
		
		private SQLiteDatabase db;
		private static MessageManager singleton;
		
		protected MessageManager(int mt, String mn){
			dh = DataHandler.get();
			messageType = mt;
			messageName = mn;
		}
		
		abstract void fetch();
		
		
		
		void beginTransaction(){
			if(transacting) throw new dataException("Transactions should not be started with a transaction open");
			else {
				transacting = true;
				db = dh.getWritableDatabase();
				db.beginTransaction();
			}
		}

}
