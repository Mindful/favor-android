package data;

import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.favor.util.Misc;





public class TextManager extends MessageManager {
	
	private static final Uri SMS_IN = Uri.parse("content://sms/inbox");
	private static final Uri SMS_OUT = Uri.parse("content://sms/sent");
	private static final Uri MMS_IN = Uri.parse("content://mms/inbox");
	private static final Uri MMS_OUT = Uri.parse("content://mms/sent");
	private static final String[] SMS_PROJECTION = { "_id", "date", "address","body" };
	private static final String[] MMS_PROJECTION = { "_id", "date" };

	protected TextManager(int type, String name) {
		super(type, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	void fetch() {
		// TODO Auto-generated method stub

	}
	
	private void updateSMS() {
		lastFetch = getLastFetch();
		beginTransaction();
		try {
			Cursor c = context.getContentResolver().query(SMS_IN,
					SMS_PROJECTION, KEY_DATE + " > " + lastFetch, null,
					KEY_DATE);
			while (c.moveToNext()) {
				db.insert(
						TABLE_RECEIVED,
						null,
						row(c.getLong(0), c.getLong(1),
								formatAddress(c.getString(2), false),
								c.getString(3), 0, 0));
			}
			c.close();
			c = context.getContentResolver().query(SMS_OUT, SMS_PROJECTION,
					KEY_DATE + " > " + lastFetch, null, KEY_DATE);
			while (c.moveToNext()) {
				db.insert(
						TABLE_SENT,
						null,
						row(c.getLong(0), c.getLong(1),
								formatAddress(c.getString(2), false),
								c.getString(3), 0, 1));
			}
			c.close();
			c = context.getContentResolver().query(MMS_IN, MMS_PROJECTION,
					KEY_DATE + " > " + lastFetch, null, KEY_DATE);
			while (c.moveToNext()) {
				receivedMMS(c.getLong(0), c.getLong(1), db);
			}
			c.close();
			c = context.getContentResolver().query(MMS_OUT, MMS_PROJECTION,
					KEY_DATE + " > " + lastFetch, null, KEY_DATE);
			while (c.moveToNext()) {
				sentMMS(c.getLong(0), c.getLong(1), db);
			}
			c.close();
			successfulTransaction();
		} catch (Exception ex) {
			throw new dataException(ex.toString());
		} finally {
			endTransaction();
		}

	}

	private static final String MMS_CC = "130"; // 0x82 in
												// com.google.android.mms.pdu.PduHeaders
	private static final String MMS_BCC = "129"; // 0x81 in
													// com.google.android.mms.pdu.PduHeaders
	private static final String MMS_TO = "151"; // 0x97 in
												// com.google.android.mms.pdu.PduHeaders
	private static final String MMS_FROM = "137"; // 0x89 in
													// com.google.android.mms.pdu.PduHeaders

	private void sentMMS(long id, long date, SQLiteDatabase db) {
		// MMS IDs are negative to avoid overlap
		// Additionally, MMS dates must be multiplied by 1000 to work properly
		// vs SMS dates
		date = date * 1000l;
		int media = 0;
		String type, data = "";
		Cursor c = context.getContentResolver().query(
				Uri.parse("content://mms/" + id + "/part"),
				new String[] { "_data", "text", "ct" },
				"ct<>\"application/smil\"", null, null);
		while (c.moveToNext()) {
			type = c.getString(2);
			if (type.equals("text/plain")) {
				data = c.getString(0);
				if (data == null) {
					data = c.getString(1); // fetch from the "text" column
				} else {
					Misc.logError("Unknown message data:" + data); // we have
																	// pure data
				}
			} else
				media = 1;
		}
		c.close();

		String filter = "(type=" + MMS_TO + " OR type=" + MMS_CC + " OR type="
				+ MMS_BCC + ")";

		c = context.getContentResolver().query(
				Uri.parse("content://mms/" + id + "/addr"),
				new String[] { "address" }, filter, null, null);
		if (c.getCount() > 1) {
			while (c.moveToNext()) {
				db.insert(
						TABLE_SENT,
						null,
						row(-id, date, formatAddress(c.getString(0), false),
								data, media, 0));
			}
		} else {
			if (c.moveToFirst()) {
				db.insert(
						TABLE_SENT,
						null,
						row(-id, date, formatAddress(c.getString(0), false),
								data, media, 0));
			}
		}
		c.close();
	}

	private void receivedMMS(long id, long date, SQLiteDatabase db) {
		// MMS IDs are negative to avoid overlap
		// Additionally, MMS dates must be multiplied by 1000 to work properly
		// vs SMS dates
		date = date * 1000l;
		int media = 0;
		String type, data = "";
		Cursor c = context.getContentResolver().query(
				Uri.parse("content://mms/" + id + "/part"),
				new String[] { "_data", "text", "ct" },
				"ct<>\"application/smil\"", null, null);
		while (c.moveToNext()) {
			type = c.getString(2);
			if (type.equals("text/plain")) {
				data = c.getString(0);
				if (data == null) {
					data = c.getString(1); // fetch from the "text" column
				} else {
					Misc.logError("Unknown message data:" + data); // we have
																	// pure data
				}
			} else
				media = 1;
		}
		c.close();
		String filter = "type=" + MMS_FROM;
		c = context.getContentResolver().query(
				Uri.parse("content://mms/" + id + "/addr"),
				new String[] { "address" }, filter, null, null);
		c.moveToFirst();
		String address = c.getString(0);
		c.close();
		db.insert(TABLE_RECEIVED, null,
				row(-id, date, formatAddress(address, false), data, media, 0));
	}


}
