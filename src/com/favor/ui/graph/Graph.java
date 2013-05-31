package com.favor.ui.graph;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.favor.util.Debug;

public abstract class Graph {
	
	public static enum types {singleBar, doubleBar, doughnut}
	public abstract void show();
	
	protected WebView webView;
	protected final String htmlBase;
	
	protected String htmlWithDimensions(WebView webView)
	{
		//TODO: dimensions calculations based on porportions - calculate the multiplier and then split
		//it in half, apply half to the scale and half to the pixel size
		int w = webView.getWidth();
		int h = webView.getHeight();
		Debug.log("W:"+w+" H:"+h);
		return htmlBase;
	}
	
	protected abstract String htmlBase(Context context);
	
	@SuppressLint("SetJavaScriptEnabled")
	public void updateView(WebView webView)
	{
		this.webView = webView;
		WebSettings webSettings = this.webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
	}
	
	public static Graph newGraph(List<String> names, Object data)
	{
		long[] array = null;
		long[][] nestedArray = null;
		types type = null;
		if (data instanceof long[])
		{
			array = (long[])data;
			type = types.singleBar; 
			Debug.log("singleBar");
		}
		else if (data instanceof long[][])
		{
			nestedArray = (long[][])data;
			if(nestedArray[0].length != 2) throw new RuntimeException("Nested graph arrays must be[x][2]");
			if (nestedArray.length == 1) 
			{
				array = nestedArray[0]; 
				type = types.doughnut; 
				Debug.log("doughnut");
			}
			else {type = types.doubleBar; Debug.log("doubleBar");}
		}
		if (type == null) throw new RuntimeException("Unknown Type");
		//different constuctors take different things; pass depending on what data is
		switch (type)
		{
			case singleBar:
				return (Graph)new singleBar(names, array);
			case doubleBar:
				return (Graph)new doubleBar(names, nestedArray);
			case doughnut:
				return (Graph)new doughnut(names, array);
			default:
				throw new RuntimeException("Unknown Type");
				
		}
				
			
	}
	
	
	public Graph(List<String> names, Context context)
	{
		this.names = names;
		this.htmlBase = htmlBase(context);
	}
	
	protected List<String> names;
}
