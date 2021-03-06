/*
 * Copyright (C) 2015  Joshua Tanner (mindful.jt@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        return doubleMultiQuery(CHARCOUNT_AVERAGE, account.getAccountName(), Core.intFromType(account.getType()), contactIds, fromDate, untilDate, sent);
    }

    public static long[] dailyMessagesLastTwoWeeks(AccountManager account, Contact contact, boolean sent){
        return messageCountLastTwoWeeks(account.getAccountName(), Core.intFromType(account.getType()), contact.getId(), sent);
    }

    public static native void clearCache();

    private static native long longQuery(int query, String accountName, int accountType, long contactId, long fromDate, long untilDate, boolean sent);
    private static native double doubleQuery(int query, String accountName, int accountType, long contactId, long fromDate, long untilDate, boolean sent);
    private static native long[] longMultiQuery(int query, String accountName, int accountType, long[] contactIds, long fromDate, long untilDate, boolean sent);
    private static native double[] doubleMultiQuery(int query, String accountName, int accountType, long[] contactIds, long fromDate, long untilDate, boolean sent);
    private static native long[] messageCountLastTwoWeeks(String accountName, int accountType, long contactId, boolean sent);


}
