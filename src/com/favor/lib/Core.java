package com.favor.lib;

/**
 * Created by josh on 10/29/14.
 */
public class Core {
    public native String helloWorld();
    static {
        System.loadLibrary("favor");
    }
}
