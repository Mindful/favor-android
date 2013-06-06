package com.favor.ui.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;

import com.favor.util.Misc;

public class singleBar extends Graph {
	
	private long[] numbers;
	private long ceiling = 0;
	private float stepWidth = 0;
	
	private void compare(long number)
	{
		if (number>ceiling)
		{
			long trunc = (long)Math.max(1, Math.pow(10, Math.log10(number) - 1)); //truncate appropriate digits
			ceiling = (number/trunc)*trunc;
			stepWidth = ceiling/10f;
		}
	}
	public singleBar(List<String> names, long[] numbers, Context context)
	{
		super(names);
		this.numbers = numbers;
		this.htmlBase = htmlBase(context);
	}
	
	protected final String htmlBase(Context context)
	{
		String html;
		AssetManager assetManager = context.getAssets();
		try
		{
			InputStream is = assetManager.open("graph/singleBar.html");
			byte[] buffer = new byte[is.available()];
			is.read(buffer, 0, buffer.length);
			html = new String(buffer);
			is.close();
		}
		catch (IOException e) 
		{
			Misc.logError("HTML load failure: " + e.getMessage());
			return "Load Error!";
		}
		int size = names.size();
		String[] labels = new String[size];
		long[] contact = new long[size];
		for (int i = 0; i < names.size(); i++) {
			labels[i] = names.get(i);
			contact[i] = numbers[i];
			compare(contact[i]);
		}
		setReplace('t', Long.toString(ceiling));
		setReplace('p', Float.toString(stepWidth));
		setReplace('l', Misc.stringToJSON(labels));
		setReplace('c', Misc.longToJSON(contact));
		html = replace(html);

		return html;
	}

}
