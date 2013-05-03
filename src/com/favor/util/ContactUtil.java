package com.favor.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;

public class ContactUtil {
	public static final Uri MMS_SMS_CONTENT_URI = Uri
			.parse("content://mms-sms/");
	public static final Uri THREAD_ID_CONTENT_URI = Uri.withAppendedPath(
			MMS_SMS_CONTENT_URI, "threadID");
	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_SENT_CONTENT_URI = Uri
			.parse("content://sms/sent");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(
			SMS_CONTENT_URI, "inbox");

	public static ContactIdentification getPersonIdFromPhoneNumber(
			Context context, String address) {

		if (address == null) {
			return null;
		}

		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(
					Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
							Uri.encode(address)),
					new String[] { PhoneLookup._ID, PhoneLookup.DISPLAY_NAME,
							PhoneLookup.LOOKUP_KEY }, null, null, null);
		} catch (IllegalArgumentException e) {
			return null;
		} catch (Exception e) {
			return null;
		}

		if (cursor != null) {
			try {
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					String contactId = String.valueOf(cursor.getLong(0));
					String contactName = cursor.getString(1);
					String contactLookup = cursor.getString(2);

					return new ContactIdentification(contactId, contactLookup,
							contactName);
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return null;
	}

	public static long findThreadIdFromAddress(Context context, String address) {
		if (address == null)
			return 0;

		String THREAD_RECIPIENT_QUERY = "recipient";

		Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
		uriBuilder.appendQueryParameter(THREAD_RECIPIENT_QUERY, address);

		long threadId = 0;

		Cursor cursor = null;
		try {

			cursor = context.getContentResolver().query(uriBuilder.build(),
					new String[] { Contacts._ID }, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				threadId = cursor.getLong(0);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return threadId;
	}

	public static int countSentMessages(Context context, long thread) {
		final String[] projection = new String[] { "_id", "thread_id",
				"address", "date", "body" };
		String selection = "date>0 and body is not null and body != '' and thread_id = "
				+ thread;
		String[] selectionArgs = null;
		final String sortOrder = "date ASC";

		int rCount = 0;

		// Create cursor
		Cursor cursor = context.getContentResolver().query(
				SMS_SENT_CONTENT_URI, projection, selection, selectionArgs,
				sortOrder);

		String address;
		long timestamp;
		String body;

		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (count > 0) {
					while (cursor.moveToNext()) {
						address = cursor.getString(2);
						timestamp = cursor.getLong(3);
						body = cursor.getString(4);

						if (!TextUtils.isEmpty(address)
								&& !TextUtils.isEmpty(body) && timestamp > 0) {
							rCount += body.length();
						}
					}
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return rCount;
	}

	public static int countRecievedMessages(Context context, long thread) {
		final String[] projection = new String[] { "_id", "thread_id",
				"address", "date", "body" };
		String selection = "date>0 and body is not null and body != '' and thread_id = "
				+ thread;
		String[] selectionArgs = null;
		final String sortOrder = "date ASC";

		int rCount = 0;

		// Create cursor
		Cursor cursor = context.getContentResolver().query(
				SMS_INBOX_CONTENT_URI, projection, selection, selectionArgs,
				sortOrder);

		String address;
		long timestamp;
		String body;

		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (count > 0) {
					while (cursor.moveToNext()) {
						address = cursor.getString(2);
						timestamp = cursor.getLong(3);
						body = cursor.getString(4);

						if (!TextUtils.isEmpty(address)
								&& !TextUtils.isEmpty(body) && timestamp > 0) {
							rCount += body.length();
						}
					}
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return rCount;
	}

	public static class ContactIdentification {
		public String contactId = null;
		public String contactLookup = null;
		public String contactName = null;

		public ContactIdentification(String _contactId, String _contactLookup,
				String _contactName) {
			contactId = _contactId;
			contactLookup = _contactLookup;
			contactName = _contactName;
		}
	}
}
