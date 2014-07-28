package data;

import java.util.Date;

import android.database.Cursor;
import android.net.Uri;

import com.favor.util.Misc;





public class TextManager extends MessageManager {
	
	private final Uri SMS_IN = Uri.parse("content://sms/inbox");
	private final Uri SMS_OUT = Uri.parse("content://sms/sent");
	private final Uri MMS_IN = Uri.parse("content://mms/inbox");
	private final Uri MMS_OUT = Uri.parse("content://mms/sent");
	private final String[] SMS_PROJECTION = { "_id", "date", "address","body" };
	private final String[] MMS_PROJECTION = { "_id", "date" };
	private final String MMS_CC = "130"; // 0x82 in com.google.android.mms.pdu.PduHeaders
	private final String MMS_BCC = "129"; // 0x81 in com.google.android.mms.pdu.PduHeaders
	private final String MMS_TO = "151"; // 0x97 in com.google.android.mms.pdu.PduHeaders
	private final String MMS_FROM = "137"; // 0x89 in com.google.android.mms.pdu.PduHeaders


	protected TextManager(int type, String name) {
		super(type, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	void fetch() {
		lastFetch = getLastFetch();
		beginTransaction();
		try {
			Cursor c = dh.context().getContentResolver().query(SMS_IN, SMS_PROJECTION, 
					DataConstants.KEY_DATE + " > " + lastFetch, null, DataConstants.KEY_DATE);
			while (c.moveToNext()) exportMessage(false, c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), 0);
			c.close();
			
			c = dh.context().getContentResolver().query(SMS_OUT, SMS_PROJECTION,
					DataConstants.KEY_DATE + " > " + lastFetch, null, DataConstants.KEY_DATE);
			while (c.moveToNext()) while (c.moveToNext()) exportMessage(true, c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), 0);
			c.close();
			
			c = dh.context().getContentResolver().query(MMS_IN, MMS_PROJECTION,
					DataConstants.KEY_DATE + " > " + lastFetch, null, DataConstants.KEY_DATE);
			while (c.moveToNext()) receivedMMS(c.getLong(0), c.getLong(1));
			c.close();
			
			c = dh.context().getContentResolver().query(MMS_OUT, MMS_PROJECTION,
					DataConstants.KEY_DATE + " > " + lastFetch, null, DataConstants.KEY_DATE);
			while (c.moveToNext()) sentMMS(c.getLong(0), c.getLong(1));
			c.close();
			successfulTransaction(new Date().getTime());
		} catch (Exception ex) {
			throw new dataException(ex.toString());
		} finally {
			endTransaction();
		}
	}
	
	
	@Override
	String formatAddress(String address) {
		// TODO implement any address formattng necessary for the TextManager
		return null;
	}

	
	private String formatAddress(String address, boolean fetch) {
		if (address.contains("@") && fetch) {
			address = "\"" + address + "\"";
		} else
			address = address.replaceAll("[^0-9]", ""); // regex matches
														// anything except
														// digits
		return address;
	}

	private void sentMMS(long id, long date) {
		// MMS IDs are negative to avoid overlap
		// Additionally, MMS dates must be multiplied by 1000 to work properly
		// vs SMS dates
		date = date * 1000l;
		int media = 0;
		String type, data = "";
		Cursor c = dh.context().getContentResolver().query(
				Uri.parse("content://mms/" + id + "/part"),
				new String[] { "_data", "text", "ct" },
				"ct<>\"application/smil\"", null, null);
		while (c.moveToNext()) {
			type = c.getString(2);
			if (type.equals("text/plain")) {
				data = c.getString(0);
				if (data == null) data = c.getString(1); // fetch from the "text" column
			    else Misc.logError("Unknown message data:" + data); // we have pure data
			} else media = 1;
		}
		c.close();

		String filter = "(type=" + MMS_TO + " OR type=" + MMS_CC + " OR type="
				+ MMS_BCC + ")";
		
		//The extra code here is so we can insert multiple entries for sending to multiple people
		c = dh.context().getContentResolver().query(
				Uri.parse("content://mms/" + id + "/addr"),
				new String[] { "address" }, filter, null, null);
		if (c.getCount() > 1) {
			while (c.moveToNext()) exportMessage(true, -id, date, c.getString(0), data, media);
		} else if (c.moveToFirst()) exportMessage(true, -id, date, c.getString(0), data, media);
		c.close();
	}

	private void receivedMMS(long id, long date) {
		// MMS IDs are negative to avoid overlap
		// Additionally, MMS dates must be multiplied by 1000 to work properly
		// vs SMS dates
		date = date * 1000l;
		int media = 0;
		String type, data = "";
		Cursor c = dh.context().getContentResolver().query(
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
		c = dh.context().getContentResolver().query(
				Uri.parse("content://mms/" + id + "/addr"),
				new String[] { "address" }, filter, null, null);
		c.moveToFirst();
		String address = c.getString(0);
		c.close();
		exportMessage(true, -id, date, address, data, media);
	}


}
