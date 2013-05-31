package com.favor.ui.graph;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;

import com.favor.util.Debug;
import com.favor.widget.GraphView;

public abstract class Graph {
	
	public static enum types {singleBar, doubleBar, doughnut}
	
	protected GraphView graphView;
	//protected final String htmlBase;
	protected String htmlBase;
	protected final List<String> names;
	
	protected String htmlWithDimensions(GraphView g)
	{
		//TODO: dimensions calculations based on porportions - calculate the multiplier and then split
		//it in half, apply half to the scale and half to the pixel size
		int w = g.getWidth();
		int h = g.getHeight();
		Debug.log("W:"+w+" H:"+h);
		return htmlBase;
		//TODO: this should eventually generate wider-than-screen graphs if names.length>X
		//X should probably be a setting, actually
	}
	
	public void show() 
	{
		graphView.clearView();
		graphView.loadDataWithBaseURL("file:///android_asset/graph/", htmlWithDimensions(graphView), null, "UTF-8", null);
	}
	
	protected abstract String htmlBase(Context context);
	
	@SuppressLint("SetJavaScriptEnabled")
	public void updateView(GraphView g)
	{
		if (this.graphView == g) Debug.log("Redundant webView update.");
		this.graphView = g;
	}
	
	public static Graph newGraph(List<String> names, Object data, Context context)
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
				return (Graph)new singleBar(names, array, context);
			case doubleBar:
				return (Graph)new doubleBar(names, nestedArray, context);
			case doughnut:
				return (Graph)new doughnut(names, array, context);
			default:
				throw new RuntimeException("Unknown Type");
				
		}
				
			
	}
	
	
	public Graph(List<String> names, Context context)
	{
		//TODO: the problem is here - we're trying to build the htmlbase before numbers are set
		this.names = names;
		//this.htmlBase = htmlBase(context);
	}
	
}
