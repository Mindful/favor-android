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

public class doughnut extends Graph {
	
	private long[] numbers;
	public doughnut(List<String> names, long[] numbers)
	{
		super(names);
		this.numbers = numbers;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void show(Context context, WebView webView) {
		if (this.numbers.length != 2) throw new RuntimeException("Doughnut graphs must be 2 numbers only");
		WebSettings webSettings = webView.getSettings();
		AssetManager assetManager = context.getAssets();
		webSettings.setJavaScriptEnabled(true);

		try {
			InputStream is = assetManager.open("graph/doughnut.html");
			byte[] buffer = new byte[is.available()];
			is.read(buffer, 0, buffer.length);

			String html = new String(buffer);
			html = html.replaceAll("%CONTACT", Long.toString(numbers[0]));
			html = html.replaceAll("%SELF", Long.toString(numbers[1]));
			Debug.log(html);
			webView.clearView();
			webView.loadDataWithBaseURL("file:///android_asset/graph/", html,
					null, "UTF-8", null);
			is.close();

		} catch (IOException e) {
			Log.e("Failed", "Could not load '" + e.getMessage() + "'!");
		}
	}

}
