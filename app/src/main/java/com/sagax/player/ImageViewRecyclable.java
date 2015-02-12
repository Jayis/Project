package com.sagax.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.SlidingDrawer.OnDrawerCloseListener;

public class ImageViewRecyclable extends ImageView
{
    private Bitmap bitmap;
    private int paddingWidth = 0;

    public ImageViewRecyclable(Context context)
    {
        super(context);
    }
    
    public ImageViewRecyclable(Context context,AttributeSet attrs)
    {
        super(context,attrs);
    }
    
    public void setPadding(int w){
    	paddingWidth = w;
    }

    @Override
    public void setImageBitmap(Bitmap bm)
    {
        super.setImageBitmap(bm);
        if (bitmap != null) bitmap.recycle();
        this.bitmap = bm;
    }
   
}