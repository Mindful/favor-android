package com.favor.library;

import android.app.Activity;

/**
 * Created by josh on 10/29/14.
 */
public class Core {
    //Private
    private static native void init(String databaseLocation, boolean first) throws FavorException;


    //Public

    public static native String helloWorld(); //This is just here for testing


    public static void initialize(Activity acc){
        //TODO: check and see if this is the first time we're runnin the application
        boolean first = true;
        try {
            init(acc.getFilesDir().getAbsolutePath(), first);
        } catch (FavorException e) {
            e.printStackTrace();
            //TODO: log something or display something to the user
        }
    }

    public static native void cleanup();


    static {
        System.loadLibrary("favor");
    }
}
