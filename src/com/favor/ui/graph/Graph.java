package com.favor.ui.graph;

import java.util.List;

import android.content.Context;
import android.webkit.WebView;

import com.favor.widget.Contact;

public interface Graph {
	public void showBar(Context context, WebView webView, List<Contact> contacts);
}
