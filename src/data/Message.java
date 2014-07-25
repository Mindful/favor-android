package data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.database.Cursor;

public class Message {
	private long date;
	private int charCount;
	private String address;
	private int media;
	private int sent;

	private Message() {
	};

	public static Message build(Cursor c, int sent) {
		Message ret = new Message();
		int dateColumn = c.getColumnIndex(DataHandler.KEY_DATE), addressColumn = c
				.getColumnIndex(DataHandler.KEY_ADDRESS), charCountColumn = c
				.getColumnIndex(DataHandler.KEY_CHARCOUNT), mediaColumn = c
				.getColumnIndex(DataHandler.KEY_MEDIA);
		ret.sent = sent;
		if (dateColumn != -1)
			ret.date = c.getLong(dateColumn);
		else
			ret.date = -1;
		if (addressColumn != -1)
			ret.address = c.getString(addressColumn);
		else
			ret.address = null;
		if (charCountColumn != -1)
			ret.charCount = c.getInt(charCountColumn);
		else
			ret.charCount = -1;
		if (mediaColumn != -1)
			ret.media = c.getInt(mediaColumn);
		else
			ret.media = -1;
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (charCount != other.charCount)
			return false;
		if (date != other.date)
			return false;
		if (media != other.media)
			return false;
		if (sent != other.sent)
			return false;
		return true;
	}

	/**
	 * Strictly a debug method
	 */
	public String toString() {
		String log = "Address:";
		if (address == null)
			log += "<>";
		else
			log += address;
		log += " Date:";
		if (date == -1)
			log += "<>";
		else
			log += date;
		log += " Chars:";
		if (charCount == -1)
			log += "<>";
		else
			log += charCount;
		log += " Media:";
		if (media == -1)
			log += "<>";
		else
			log += media;
		log += " Sent:";
		if (sent == -1)
			log += "<>";
		else
			log += sent;
		return log;
	}

	public boolean multimedia() {
		if (media == -1)
			throw new dataException(
					"multimedia() value not known. Query must include KEY_MEDIA");
		else
			return media == 1;
	}

	public boolean received() {
		if (sent == -1)
			throw new dataException(
					"received() value not known. Query must include KEY_SENT");
		else
			return sent == 0;
	}

	public int charCount() {
		if (charCount == -1)
			throw new dataException(
					"charCount() value not known. Query must include KEY_CHARCOUNT");
		else
			return charCount;
	}

	public String address() {
		if (address == null)
			throw new dataException(
					"address() value not known. Query must include KEY_ADDRESS");
		else
			return address;
	}

	public long rawDate() {
		if (date == -1)
			throw new dataException(
					"rawDate() value not known. Query must include KEY_DATE");
		else
			return date;
	}

	public String textDate() {
		if (date == -1)
			throw new dataException(
					"textDate() value not known. Query must include KEY_DATE");
		else {
			SimpleDateFormat d = new SimpleDateFormat(
					"MM/dd/yyy hh:mm:ss a zzz");
			d.setTimeZone(TimeZone.getDefault());
			return d.format(new Date(date));
		}
	}

}


