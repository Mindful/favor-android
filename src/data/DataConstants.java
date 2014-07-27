package data;

public class DataConstants {

	static final String KEY_ID = "_id"; // unique integer message id
	static final String KEY_DATE = "date"; // integer date
	static final String KEY_ADDRESS = "address"; // address
	static final String KEY_CHARCOUNT = "chars"; // character count
	static final String KEY_MEDIA = "media"; // 1:media, 0:no media (sms or plain mms)
	static final String[] KEYS_PUBLIC = { KEY_DATE, KEY_ADDRESS, KEY_CHARCOUNT, KEY_MEDIA };
	// Messages tables
	static final String TABLE_SENT = "sent";
	static final String TABLE_RECEIVED = "received";

}
