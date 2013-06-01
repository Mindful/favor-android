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
	
	//old height was 500
	//old width was 340
	private final float baseScale = 2.0f;
	
	protected String htmlWithDimensions(GraphView g)
	{
		//TODO: if names > saved maximum, multiply width * 1+((names.length-max)/max)
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
		
		Debug.log("view w:"+g.getWidth()+" view h:"+g.getHeight());
		Debug.log("Start w:"+startWidth+" Start h:"+startHeight);
		
		float widthRatio = (g.getWidth()/baseScale)/(float)startWidth;
		float heightRatio = (g.getHeight()/baseScale)/(float)startHeight;
		
		Debug.log("WRatio:"+widthRatio+" HRatio:"+heightRatio);
		
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
			Debug.log("padding");
		}
		else 
		{
			finalHeight = (int) (finalHeight*0.96);
			html = html.replaceAll("%PADDING", "1%");
		}
		
		html = html.replaceAll("%HEIGHT", Integer.toString(finalHeight));
		html = html.replaceAll("%WIDTH", Integer.toString(finalWidth));
		html = html.replaceAll("%SCALE", Float.toString(baseScale));
		
		
		
		//we should zoom as much as we can by finding which of the elements will break out of its 
		//bounds first, which is a function of their size compared to the view size
		
		//TODO: dimensions calculations based on porportions - calculate the multiplier and then split
		//it in half (ish) so it's distributed over the pixel width and zoom evently
		//the actual mathematical operation I'm looking for here is sqrt, since we end up multiplying
		//it in twice - so it's sqrt of the porportion times the pixels and the zoom
		
		//note using the zoom instead of the pixel size only works with phones that have the same 
		//aspect ratio, although I can probably get away with just computing an average change for
		//phones with _similar_ aspect ratios
		//Debug.log("W:"+w+" H:"+h);
		return html;
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
	
	
	public Graph(List<String> names, Context context)
	{
		//TODO: the problem is here - we're trying to build the htmlbase before numbers are set
		this.names = names;
		//this.htmlBase = htmlBase(context);
	}
	
}
