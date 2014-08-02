package com.favor.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.util.Log;

public class Logger {
	
	//TODO: Maybe (only in debug mode?) these should actually write to a text file/favor specific long term log

	public static void error(String err)
	{
		Log.e("Favor - Error:", err);
	}
	
	public static void exception(String msg, Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		Log.e("Favor - Error:", msg+":"+e.toString());
		Log.e("Favor - Stack Trace:", sw.toString());
	}

}
