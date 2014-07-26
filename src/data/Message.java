package data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.database.Cursor;

public class Message {
	private long date;
	private int charCount;
	private String address;
	private int media;
	private int sent;
	private int type;
	//TODO: update all of this to deal with message type

	private Message() {
	};

	public static Message build(Cursor c, int sent, int type) {
		Message ret = new Message();
		int dateColumn = c.getColumnIndex(DataConstants.KEY_DATE), 
			addressColumn = c.getColumnIndex(DataConstants.KEY_ADDRESS), 
			charCountColumn = c.getColumnIndex(DataConstants.KEY_CHARCOUNT), 
			mediaColumn = c.getColumnIndex(DataConstants.KEY_MEDIA);
		
		ret.sent = sent;
		ret.type = type;
		
		if (dateColumn != -1) ret.date = c.getLong(dateColumn);
		else ret.date = -1;
		
		if (addressColumn != -1) ret.address = c.getString(addressColumn);
		else ret.address = null;
		
		if (charCountColumn != -1) ret.charCount = c.getInt(charCountColumn);
		else ret.charCount = -1;
		
		if (mediaColumn != -1) ret.media = c.getInt(mediaColumn);
		else ret.media = -1;
		
		return ret;
	}

	/**
	 * Strictly a debug method
	 */
	public String toString() {
		String log = "Address:";
		if (address == null) log += "<>";
		else log += address;
		log += " Date:";
		if (date == -1) log += "<>";
		else log += date;
		log += " Chars:";
		if (charCount == -1) log += "<>";
		else log += charCount;
		log += " Media:";
		if (media == -1) log += "<>";
		else log += media;
		log += " Sent:";
		if (sent == -1) log += "<>";
		else log += sent;
		log+= " Type:";
		if (type == -1) log += "<>"; //TODO: This should eventually check with DataHandler and spit out the type name if known
		else log+= type;
		return log;
	}

	public boolean multimedia() {
		if (media == -1) throw new dataException("multimedia() value not known. Query must include KEY_MEDIA");
		else return media == 1;
	}

	public boolean received() {
		if (sent == -1) throw new dataException("received() value not known. This should never happen.");
		else return sent == 0;
	}
	
	public int type(){
		if (type == -1) throw new dataException("type() value not known. This should never happen.");
		else return type;
	}
		

	public int charCount() {
		if (charCount == -1) throw new dataException("charCount() value not known. Query must include KEY_CHARCOUNT");
		else return charCount;
	}

	public String address() {
		if (address == null) throw new dataException("address() value not known. Query must include KEY_ADDRESS");
		else return address;
	}

	public long rawDate() {
		if (date == -1) throw new dataException("rawDate() value not known. Query must include KEY_DATE");
		else return date;
	}

	public String textDate() {
		if (date == -1) throw new dataException("textDate() value not known. Query must include KEY_DATE");
		else {
			SimpleDateFormat d = new SimpleDateFormat("MM/dd/yyy hh:mm:ss a zzz", Locale.getDefault());
			d.setTimeZone(TimeZone.getDefault());
			return d.format(new Date(date));
		}
	}

}


