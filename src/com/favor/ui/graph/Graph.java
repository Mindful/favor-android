package com.favor.ui.graph;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;

import com.favor.widget.GraphView;

public abstract class Graph {
	
	public static enum types {singleBar, doubleBar, doughnut}
	
	protected GraphView graphView;
	//protected final String htmlBase;
	protected String htmlBase;
	protected final List<String> names;
	
	//old height was 500
	//old width was 340
	private final float baseScale = 2.0f;
	
	protected String htmlWithDimensions(GraphView g)
	{
		//TODO: if names > saved maximum, multiply width * 1+((names.length-max)/max)
		//this should eventually generate wider-than-screen graphs if names.length>X
		//X should probably be a setting, actually
		//TODO: 99% sure the problem is arising from 0-values
		if (g.getWidth()==0 && g.getHeight()==0)
		{
			return "<!doctype html><html><body bgcolor=\"#808080\"><head>"+
			"<title>Bar Chart</title><script type=\"text/javascript\" src=\"file:///android_asset/graph/charts.js\"></script>"+
			"<meta name = \"viewport\" content = \"initial-scale = %SCALE, target-densitydpi=device-dpi\"></head>"+
			"<body style=\"margin:0px; padding:0px;\"><canvas id=\"canvas\" height=\"%HEIGHT\" width=\"%WIDTH\" style=\"padding-top:%PADDING\"></canvas>"+
			"</body></html>";

		}
		float aspectRatio = (float)g.getWidth()/(float)g.getHeight();
		final int baseHeight = (int) (g.getHeight()/(baseScale*1.05));
		int startWidth = (int)(aspectRatio*baseHeight);
		int startHeight = baseHeight;
		
		//Debug.log("view w:"+g.getWidth()+" view h:"+g.getHeight());
		//Debug.log("Start w:"+startWidth+" Start h:"+startHeight);
		
		float widthRatio = (g.getWidth()/baseScale)/(float)startWidth;
		float heightRatio = (g.getHeight()/baseScale)/(float)startHeight;
		
		//Debug.log("WRatio:"+widthRatio+" HRatio:"+heightRatio);
		
		String html = htmlBase;
		
		int finalWidth, finalHeight;
		float finalScale;
		if (heightRatio<widthRatio) //given that heightRatio is x/height, bigger height = smaller ratio
		{
			//height is bigger - the scale multiplier will be determined by height
			float sqrtHeightRatio = (float) Math.sqrt(heightRatio);
			finalWidth = (int) (startWidth*widthRatio/sqrtHeightRatio);
			finalHeight = (int) (startHeight*sqrtHeightRatio);
			finalScale = (float) (baseScale * sqrtHeightRatio);
		}
		else
		{
			//width is bigger - the scale multiplier will be determined by width
			float sqrtWidthRatio = (float) Math.sqrt(widthRatio);
			finalWidth = (int) (startWidth*sqrtWidthRatio);
			finalHeight = (int) (startHeight*heightRatio/sqrtWidthRatio);
			finalScale = (float) (baseScale * sqrtWidthRatio);
		}
		
		if (aspectRatio < 1.0) //take note that these are computed after height ratio
		{
			html = html.replaceAll("%PADDING", "5%");
			finalHeight = (int) (finalHeight*0.95);
		}
		else 
		{
			finalHeight = (int) (finalHeight*0.96);
			html = html.replaceAll("%PADDING", "1%");
		}
		
		html = html.replaceAll("%HEIGHT", Integer.toString(finalHeight));
		html = html.replaceAll("%WIDTH", Integer.toString(finalWidth));
		html = html.replaceAll("%SCALE", Float.toString(finalScale));
		
		
		return html;
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
		}
		else if (data instanceof long[][])
		{
			nestedArray = (long[][])data;
			if(nestedArray[0].length != 2) throw new RuntimeException("Nested graph arrays must be[x][2]");
			if (nestedArray.length == 1) 
			{
				array = nestedArray[0]; 
				type = types.doughnut; 
			}
			else {type = types.doubleBar;}
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
	
	
	public Graph(List<String> names)
	{
		this.names = names;
	}
	
}
