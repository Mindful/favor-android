package com.favor.library;

import android.util.Log;

/**
 * Created by josh on 11/6/14.
 */
public class Logger {

    static void error(String s){
        Log.e("favor", s);
    }

    static void warning(String s){
        Log.w("favor", s);
    }

    static void info(String s){
        Log.i("favor", s);
    }
}
