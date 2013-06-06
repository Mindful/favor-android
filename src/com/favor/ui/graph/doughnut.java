package com.favor.ui.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.favor.util.Misc;

import android.content.Context;
import android.content.res.AssetManager;


public class doughnut extends Graph {
	
	private final long[] numbers;
	public doughnut(List<String> names, long[] numbers, Context context)
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
			InputStream is = assetManager.open("graph/doughnut.html");
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
		setReplace('c', Long.toString(numbers[1]));
		setReplace('s', Long.toString(numbers[0]));
		setReplace('n', names.get(0));
		html = replace(html);

		return html;
			
	}

}
