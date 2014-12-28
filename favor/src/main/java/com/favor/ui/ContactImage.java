package com.favor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

//http://www.rogcg.com/blog/2013/11/01/gridview-with-auto-resized-images-on-android


public class ContactImage extends ImageView {
    public ContactImage(Context context)
    {
        super(context);
    }

    public ContactImage(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ContactImage(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }

}
