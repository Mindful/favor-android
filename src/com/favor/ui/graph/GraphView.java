package com.favor.ui.graph;

import com.favor.ui.GraphActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class GraphView extends WebView {
	
	
	@Override
	protected void onSizeChanged (int w, int h, int ow, int oh)
	{
		if (w==0 && h==0) return;
		else GraphActivity.getGraph().show();
	}

	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public GraphView(Context context) {
		super(context);
		init();
	}
	
	
	@SuppressLint("SetJavaScriptEnabled")
	private void init()
	{
		WebSettings webSettings = getSettings();
		webSettings.setJavaScriptEnabled(true);
		setBackgroundColor(0x808080);
		setClickable(false); //Not sure this does anything, but it certainly doesn't hurt
		setVerticalScrollBarEnabled(false);
	}


}
