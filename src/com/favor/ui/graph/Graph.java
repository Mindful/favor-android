package com.favor.ui.graph;

import java.util.List;

import android.content.Context;
import android.webkit.WebView;

import com.favor.util.Debug;
import com.favor.widget.Contact;

public abstract class Graph {
	
	public static enum types {singleBar, doubleBar, doughnut}
	public abstract void show(Context context, WebView webView);
	
	public static Graph newGraph(List<String> names, Object data)
	{
		long[] array = null;
		long[][] nestedArray = null;
		types type = null;
		if (data instanceof long[])
		{
			array = (long[])data;
			if(array.length == 2) {type = types.doughnut; Debug.log("doughnut");}
			else {type = types.singleBar; Debug.log("singleBar");}
		}
		else if (data instanceof long[][])
		{
			nestedArray = (long[][])data;
			if(nestedArray[0].length == 2) {type = types.doubleBar; Debug.log("doubleBar");}
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
	
	
	public Graph(List<String> names)
	{
		this.names = names;
	}
	
	protected List<String> names;
}
