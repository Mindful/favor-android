package com.favor.library;

import com.favor.app.R;

import java.util.List;

/**
 * Created by josh on 2/27/16.
 */
public class Util {
    public static int sentColor(){
        return Core.getContext().getResources().getColor(R.color.sent);
    }

    public static int receivedColor(){
        return Core.getContext().getResources().getColor(R.color.rec);
    }

    public static String[] contactListToNames(List<Contact> contacts){
        String[] ret = new String[contacts.size()];
        for (int i = 0; i < contacts.size(); ++i){
            ret[i] = contacts.get(i).getDisplayName();
        }
        return ret;
    }
}
