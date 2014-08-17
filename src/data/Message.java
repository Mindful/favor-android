package data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import data.DataConstants.Type;

import static data.DataConstants.*;

public class Message {
	private long date;
	private int charCount;
	private String address;
	private int media;
	private int sent;
	private Type type;
	
	//In a perfect world this would actually live inside SQLite statements, but extending the library
	//is substantially more difficult than putting it here.
	static HashMap<String, Integer> buildColumnMap(SQLiteStatement s) throws SQLiteException{
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (int i = 0; i < s.columnCount(); i++) ret.put(s.getColumnName(i), i);
		for (int i = 0; i < KEYS_PUBLIC.length; i++){
			if(!ret.containsKey(KEYS_PUBLIC[i])) ret.put(KEYS_PUBLIC[i], -1);
		}
		return ret;
	}

	private Message() {
	};

	public static Message build(SQLiteStatement s, HashMap<String, Integer> columnMap, int sent, Type type) throws SQLiteException {
		Message ret = new Message();
		int dateColumn = columnMap.get(KEY_DATE), 
			addressColumn = columnMap.get(KEY_ADDRESS), 
			charCountColumn = columnMap.get(KEY_CHARCOUNT), 
			mediaColumn = columnMap.get(KEY_MEDIA);
		
		ret.sent = sent;
		ret.type = type;
		
		if (dateColumn != -1) ret.date = s.columnLong(dateColumn);
		else ret.date = -1;
		
		if (addressColumn != -1) ret.address = s.columnString(addressColumn);
		else ret.address = null;
		
		if (charCountColumn != -1) ret.charCount = s.columnInt(charCountColumn);
		else ret.charCount = -1;
		
		if (mediaColumn != -1) ret.media = s.columnInt(mediaColumn);
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
		//A message should always, always know the below two things about itself
		log += " Sent:" + sent;
		log += " Type:" +  DataHandler.get().messageTypeName(type);
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
	
	public Type type(){
		if (type == null) throw new dataException("type() value not known. This should never happen.");
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


