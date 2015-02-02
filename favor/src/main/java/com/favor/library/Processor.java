package com.favor.library;

import java.util.ArrayList;

public class Processor {

    //These look like magic numbers because they're based on array positions of functions at the C++ level; to verify
    //or change anything, the C++ JNI code must be consulted

    //These return long values
    private final static int RESPONSE_TIME_NINTIETH = 0;
    private final static int CHARCOUNT_TOTAL = 1;
    private final static int MESSAGECOUNT_TOTAL = 2;

    //These return double values
    private final static int CHARCOUNT_AVERAGE = 0;
    private final static int RESPONSE_TIME_CONVERSATIONAL = 1;

    public static double averageCharcount(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return doubleQuery(CHARCOUNT_AVERAGE, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);
    }

    public static double averageConversationalResponsetime(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return doubleQuery(RESPONSE_TIME_CONVERSATIONAL, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);

    }

    public static long responseTimeNintieth(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return longQuery(RESPONSE_TIME_NINTIETH, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);

    }

    public static long totalCharcount(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return longQuery(CHARCOUNT_TOTAL, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);

    }

    public static long totalMessagecount(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return longQuery(MESSAGECOUNT_TOTAL, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);
    }

    private static long[] idsFromContacts(ArrayList<Contact> contacts){
        long[] contactIds = new long[contacts.size()];
        for (int i = 0; i < contacts.size(); ++i){
            contactIds[i] = contacts.get(i).getId();
        }
        return contactIds;
    }

    public static long[] batchMessageCount(AccountManager account, ArrayList<Contact> contacts, long fromDate, long untilDate, boolean sent){
        long[] contactIds = idsFromContacts(contacts);
        return longMultiQuery(MESSAGECOUNT_TOTAL, account.getAccountName(), Core.intFromType(account.getType()), contactIds, fromDate, untilDate, sent);
    }

    public static long[] batchCharCount(AccountManager account, ArrayList<Contact> contacts, long fromDate, long untilDate, boolean sent){
        long[] contactIds = idsFromContacts(contacts);
        return longMultiQuery(CHARCOUNT_TOTAL, account.getAccountName(), Core.intFromType(account.getType()), contactIds, fromDate, untilDate, sent);
    }

    public static long[] batchResponseTimeNintieth(AccountManager account, ArrayList<Contact> contacts, long fromDate, long untilDate, boolean sent){
        long[] contactIds = idsFromContacts(contacts);
        return longMultiQuery(RESPONSE_TIME_NINTIETH, account.getAccountName(), Core.intFromType(account.getType()), contactIds, fromDate, untilDate, sent);
    }

    public static double[] batchAverageCharCount(AccountManager account, ArrayList<Contact> contacts, long fromDate, long untilDate, boolean sent){
        long[] contactIds = idsFromContacts(contacts);
        return doubleMultiQuery(MESSAGECOUNT_TOTAL, account.getAccountName(), Core.intFromType(account.getType()), contactIds, fromDate, untilDate, sent);
    }

    private static native long longQuery(int query, String accountName, int accountType, long contactId, long fromDate, long untilDate, boolean sent);
    private static native double doubleQuery(int query, String accountName, int accountType, long contactId, long fromDate, long untilDate, boolean sent);
    private static native long[] longMultiQuery(int query, String accountName, int accountType, long[] contactIds, long fromDate, long untilDate, boolean sent);
    private static native double[] doubleMultiQuery(int query, String accountName, int accountType, long[] contactIds, long fromDate, long untilDate, boolean sent);


}
