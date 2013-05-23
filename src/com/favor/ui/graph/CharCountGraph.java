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
import com.favor.util.Misc;
import com.favor.widget.Contact;

public class CharCountGraph implements Graph {

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void showBar(Context context, WebView webView, List<Contact> contacts) {
		WebSettings webSettings = webView.getSettings();
		AssetManager assetManager = context.getAssets();
		webSettings.setJavaScriptEnabled(true);

		try {
			InputStream is = assetManager.open("graph/bar.html");
			byte[] buffer = new byte[is.available()];
			is.read(buffer, 0, buffer.length);

			String html = new String(buffer);

			int size = contacts.size();
			String[] labels = new String[size];
			long[] red = new long[size];
			long[] blue = new long[size];
			for (int i = 0; i < contacts.size(); i++) {
				Contact c = contacts.get(i);
				String address = c.getAddress();
				labels[i] = contacts.get(i).getName();
				long[] results = Algorithms.charCount(address, -1, -1); //problem here
				red[i] = results[1];
				blue[i] = results[0];
			}
			html = html.replaceAll("%LABELS", Misc.stringToJSON(labels));
			html = html.replaceAll("%REDBAR", Misc.longToJSON(red));
			html = html.replaceAll("%BLUEBAR", Misc.longToJSON(blue));

			webView.loadDataWithBaseURL("file:///android_asset/graph/", html,
					null, "UTF-8", null);
			is.close();

		} catch (IOException e) {
			Log.e("Failed", "Could not load '" + e.getMessage() + "'!");
		}
	}

}
