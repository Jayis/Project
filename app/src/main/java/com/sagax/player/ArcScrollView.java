package com.sagax.player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class ArcScrollView extends View {

	private Paint textColorPaint;
	private Paint centerPaint;
	private static final int textColor = Color.WHITE;
	private static final int centerColor = Color.YELLOW;
	private Context context;
	private int offset = 200;
	private String letterA [] = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z" };
	private int space = 18;
	double base = 180.f/space ;
	double spanAngle = 0;
	private double mStartAngle = 0;
	private boolean isMoving = false;
	private boolean lock = false;
	private int currentIndex = 0;
	private OnIndexChangeListener listener;
	
	public ArcScrollView(Context context) {
		super(context);
		this.context = context;
		init();
		// TODO Auto-generated constructor stub
	}
	
    public ArcScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
	public ArcScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		textColorPaint = new Paint();
		textColorPaint.setColor(textColor);
		textColorPaint.setTextSize(80);
		textColorPaint.setTextAlign(Align.CENTER);
		textColorPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/BoldItalic.ttf"));
		
		centerPaint = new Paint();
		centerPaint.setColor(centerColor);
		centerPaint.setTextSize(140);
		centerPaint.setTextAlign(Align.CENTER);
		centerPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/BoldItalic.ttf"));
	}
	
	public void setLetter(String[] letters,int i){
		letterA = letters;
		currentIndex = i;
		invalidate();
	}
	
	public int getCurrentIndex(){
		return currentIndex;
	}
	
	public void setCurrentIndex(int i){
		currentIndex = i;
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.translate(this.getWidth() / 2, this.getHeight() + offset);
		
		double radius = this.getHeight()/2 + this.getHeight()/20 + offset;

		if(!isMoving){
			spanAngle = currentIndex * base - 90;
		}
		
		for(int i = 0 ;i < letterA.length;i++){
			
			double angle = 180 - base * i + spanAngle;
			if(angle > 0.f && angle < 180.f){
				double x = radius * Math.cos(Math.toRadians(angle));
				double y = -radius * Math.sin(Math.toRadians(angle));
				canvas.save();
				canvas.rotate(90.f - (float)angle, (float)x, (float)y);
				if(Math.abs(angle - 90.f) < 4.5f){
					currentIndex = i;
					
					int size = calcTextSize(140, 80, 4.5, Math.abs(angle - 90.f));
					centerPaint.setTextSize(size);
					if(!isMoving)
						canvas.drawText(letterA[i]+"", (float)x, (float)y, centerPaint);
					else{
						listener.onIndexChanged(this, currentIndex);
						canvas.drawText(letterA[i]+"", (float)x, (float)y, centerPaint);
					}
				}
				else
					canvas.drawText(letterA[i]+"", (float)x, (float)y, textColorPaint);
				canvas.restore();
			}
			
		}
		

	} 
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX() - getWidth() / 2;
		float y = event.getY() - (this.getHeight() + offset);
		
		float distanceX = x;
		float distanceY = y;

		// Get the distance from the center of the circle in terms of a radius
		double touchEventRadius = (float) Math.sqrt((Math.pow(distanceX, 2) + Math.pow(distanceY, 2)));
		
		double touchAngle;
		touchAngle = (float) ((java.lang.Math.atan2(y, x) / Math.PI * 180) % 360);
		touchAngle = (touchAngle < 0 ?  -touchAngle : 360 - touchAngle);
		
		
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mStartAngle = touchAngle;
			isMoving = true;
			break;
		case MotionEvent.ACTION_MOVE:
			double tmpAngle = touchAngle - mStartAngle;
			if(tmpAngle + spanAngle < -90 || tmpAngle + spanAngle >  (letterA.length-1) * base - 90){
				if(tmpAngle + spanAngle < -90){
					spanAngle = -90;
					isMoving = false;
				}
				if(tmpAngle + spanAngle >  (letterA.length-1) * base - 90){
					spanAngle = (letterA.length-1) * base - 90;
					isMoving = false;
				}
					
				lock = true;
			}else{
				lock = false;
			}
			if(!lock){
				spanAngle += (touchAngle - mStartAngle);
				mStartAngle = touchAngle;
			}
			invalidate();
			break;
		default:
			isMoving = false;
			invalidate();
		}
		return true;
	}
	
	private int calcTextSize(int max, int min, double maxDistance, double curDistance){
		double precentage = 1-(curDistance / maxDistance);
		return (int)((max - min)*precentage + min);
	}
	
	private double calcNearAngel(double angle){
		return Math.round(angle/base) * base;
	}
	
	public void setOnIndexChangeListener(OnIndexChangeListener l) {
		listener = l;
	}
	
	public interface OnIndexChangeListener {

		public abstract void onIndexChanged(ArcScrollView a, int index);

	}
}
