package com.favor.library;

/**
 * Created by josh on 10/29/14.
 */
public class Reader {

    public static native AccountManager[] accountManagers();

    //TODO: if handing the contacts straight up proves slow, we can pass up large arrays of just addresses
    //and do all the actual contact building at the java layer
    public static native Contact[] contacts();

    public static native Address[] addresses();
    public static native Address[] allAddresses();

}
