package com.favor.ui.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;

import com.favor.util.Misc;

public class doubleBar extends Graph {
	
	private final long[][] numbers;
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
	public doubleBar(List<String> names, long[][] numbers, Context context)
	{
		super(names, context);
		this.numbers = numbers;
		this.htmlBase = htmlBase(context); //TODO: figure the ordering out here, this is just a workaround
	}
	
	protected final String htmlBase(Context context)
	{
		String html;
		AssetManager assetManager = context.getAssets();
		try
		{
			InputStream is = assetManager.open("graph/doubleBar.html");
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
		long[] self = new long[size];
		for (int i = 0; i < names.size(); i++) 
		{
			labels[i] = names.get(i);
			contact[i] = numbers[i][1]; //them
			compare(contact[i]);
			self[i] = numbers[i][0]; //you
			compare(self[i]);
		}
		html = html.replaceAll("%CEILING", Long.toString(ceiling));
		html = html.replaceAll("%STEPWIDTH", Float.toString(stepWidth));
		html = html.replaceAll("%LABELS", Misc.stringToJSON(labels));
		html = html.replaceAll("%CONTACT", Misc.longToJSON(contact));
		html = html.replaceAll("%SELF", Misc.longToJSON(self));
		return html;
	}


}
