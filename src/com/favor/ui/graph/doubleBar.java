package com.favor.ui.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;


import com.favor.util.Algorithms;
import com.favor.util.Debug;
import com.favor.util.Misc;
import com.favor.widget.Contact;

public class doubleBar extends Graph {
	
	private long[][] numbers;
	public doubleBar(List<String> names, long[][] numbers)
	{
		super(names);
		this.numbers = numbers;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void show(Context context, WebView webView) {
		WebSettings webSettings = webView.getSettings();
		AssetManager assetManager = context.getAssets();
		webSettings.setJavaScriptEnabled(true);

		try {
			InputStream is = assetManager.open("graph/bar.html");
			byte[] buffer = new byte[is.available()];
			is.read(buffer, 0, buffer.length);

			String html = new String(buffer);

			int size = names.size();
			String[] labels = new String[size];
			long[] red = new long[size];
			long[] blue = new long[size];
			for (int i = 0; i < names.size(); i++) 
			{
				labels[i] = names.get(i);
				red[i] = numbers[i][1]; //them
				blue[i] = numbers[i][0]; //you
			}
			html = html.replaceAll("%LABELS", Misc.stringToJSON(labels));
			html = html.replaceAll("%REDBAR", Misc.longToJSON(red));
			html = html.replaceAll("%BLUEBAR", Misc.longToJSON(blue));
			webView.clearView();
			webView.loadDataWithBaseURL("file:///android_asset/graph/", html,
					null, "UTF-8", null);
			is.close();

		} catch (IOException e) {
			Log.e("Failed", "Could not load '" + e.getMessage() + "'!");
		}
	}

}
