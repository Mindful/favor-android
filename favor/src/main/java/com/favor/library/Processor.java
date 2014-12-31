package com.favor.library;

import java.util.ArrayList;

public class Processor {
    //TODO: other batch methods

    /*
            //NintiethPercentile, totalCharcount, totalMessageCount
        LongQuery longQueries[] = {&::favor::processor::responseTimeNintiethPercentile, &::favor::processor::totalCharcount, &::favor::processor::totalMessagecount};
        //averageCharcount, averageConversationalResponsetime
        DoubleQuery doubleQueries[] = {&::favor::processor::averageCharcount, &::favor::processor::averageConversationalResponsetime};
     */

    public static double averageCharcount(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return doubleQuery(0, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);
    }

    public static double averageConversationalResponsetime(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return doubleQuery(1, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);

    }

    public static long responseTimeNintieth(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return longQuery(0, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);

    }

    public static long totalCharcount(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return longQuery(1, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);

    }

    public static long totalMessagecount(AccountManager account, Contact c, long fromDate, long untilDate, boolean sent){
        return longQuery(2, account.getAccountName(), Core.intFromType(account.getType()), c.getId(), fromDate, untilDate, sent);
    }

    public static long[] batchMessageCount(AccountManager account, ArrayList<Contact> contacts, long fromDate, long untilDate, boolean sent){
        long[] contactIds = new long[contacts.size()];
        for (int i = 0; i < contacts.size(); ++i){
            contactIds[i] = contacts.get(i).getId();
        }
        return longMultiQuery(1, account.getAccountName(), Core.intFromType(account.getType()), contactIds, fromDate, untilDate, sent);
    }

    private static native long longQuery(int query, String accountName, int accountType, long contactId, long fromDate, long untilDate, boolean sent);
    private static native double doubleQuery(int query, String accountName, int accountType, long contactId, long fromDate, long untilDate, boolean sent);
    private static native long[] longMultiQuery(int query, String accountName, int accountType, long[] contactIds, long fromDate, long untilDate, boolean sent);
    private static native double[] doubleMultiQuery(int query, String accountName, int accountType, long[] contactIds, long fromDate, long untilDate, boolean sent);



}
