package com.sagax.player;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Shader; 
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

public class ImageHelper {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
    	
    	Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    	Paint paint = new Paint();
    	paint.setStrokeWidth(5);
    	Canvas c = new Canvas(circleBitmap);
    	 //This draw a circle of Gerycolor which will be the border of image.
    	c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);
    	BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    	paint.setAntiAlias(true);
    	paint.setShader(shader);
    	// This will draw the image.
    	c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2-2, paint);
    	return circleBitmap;
    }
}