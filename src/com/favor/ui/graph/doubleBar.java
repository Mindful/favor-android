package com.favor.ui.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;

import com.favor.util.Misc;

public class doubleBar extends Graph {
	
	private long[][] numbers;
	private long ceiling = 0;
	private float stepWidth = 0;
	
	static View.OnLayoutChangeListener listener = new View.OnLayoutChangeListener() {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                int oldBottom) {
            // its possible that the layout is not complete in which case
            // we will get all zero values for the positions, so ignore the event
            if (left == 0 && top == 0 && right == 0 && bottom == 0) return;
            //TODO:
           // Do what you need to do with the height/width since they are now set
        }
    };
	
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
			Log.e("Load Fail", "HTML load failure: " + e.getMessage());
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

	public void show() 
	{
		webView.clearView();
		webView.loadDataWithBaseURL("file:///android_asset/graph/", htmlWithDimensions(webView), null, "UTF-8", null);
	}

}
