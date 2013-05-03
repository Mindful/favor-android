package com.favor.util;

import java.util.LinkedList;

import org.json.simple.JSONValue;

public class Misc {
	public static String stringToJSON(String[] array) {
		LinkedList<String> list = new LinkedList<String>();

		for (int i = 0; i < array.length; i++)
			list.add(array[i]);

		return JSONValue.toJSONString(list);
	}

	public static String longToJSON(long[] array) {
		LinkedList<Long> list = new LinkedList<Long>();

		for (int i = 0; i < array.length; i++)
			list.add(Long.valueOf(array[i]));

		return JSONValue.toJSONString(list);
	}
}
