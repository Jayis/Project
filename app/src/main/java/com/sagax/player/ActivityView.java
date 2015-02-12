package com.sagax.player;

import java.lang.ref.PhantomReference;

import com.sagax.player.R;
import com.sagax.player.AddPlaylistActivity.MyGestureDetector;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;


public abstract class ActivityView {
	
	protected MainActivity activity;
	private ImageButton img;
	private MusicManager musicManager = MainActivity.getMusicManagerInstance();
	private MediaManager mediaManager = MainActivity.getMediaManagerInstance();
	private Runnable notification2;
	private Handler handler2 = new Handler();
	protected int phoneWidth ;
	protected int phoneHeight ;
	protected GestureDetector gesture  = new GestureDetector(new MyGestureDetector());
	
	@SuppressLint("NewApi")
	public ActivityView(Activity c){
		activity = (MainActivity)c;
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		phoneWidth = size.x;
		phoneHeight = size.y;
	}
	
	public void setAnimation(){
		
	}
	
	protected OnClickListener open = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final ViewGroup view = (ViewGroup) activity.findViewById(R.id.inner_content);
			ObjectAnimator oa=ObjectAnimator.ofFloat(view, "translationX", 0, phoneWidth);
			
			oa.addListener(new AnimatorListener() {

				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animator arg0) {
				}
			});
			oa.setDuration(200);
			oa.start();
			activity.openStatus = true;
		}
	
	};
	
	protected int getPixels(int dipValue){
        Resources r = activity.getResources();
        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, 
        r.getDisplayMetrics());
        return px;
	}
	

	protected ImageButton mplay;
	private ImageButton mprev;
	private TextView mminiSong;
	private ImageButton mnext;
	private ImageButton mcycle; 
	
	protected void setupMiniPlayer() {
		mplay = (ImageButton)activity.findViewById(R.id.mini_play);
		mprev = (ImageButton)activity.findViewById(R.id.mini_prev);
		mminiSong = (TextView)activity.findViewById(R.id.mini_text);
		mnext = (ImageButton)activity.findViewById(R.id.mini_next);
		mcycle = (ImageButton)activity.findViewById(R.id.mini_once);
		mini_refresh();
		int count = mediaManager.getAllSong().size();

		if(count > 0 && musicManager.getCurrSong() != null){
			mminiSong.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					finish();
					ActivityView av = activity.act.get(activity.statusList[0]);
					activity.init();
					av.display();
					activity.setClose();
					av.setSwipe();
				}
			});
			
			mplay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean status = musicManager.togglePlay();
					if(!status){
						mplay.setImageResource(R.drawable.mini_play);
					}
					else {
						mplay.setImageResource(R.drawable.mini_pause);
						mini_refresh();
					}
				}
			});
			mnext.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					musicManager.playNext();
					mplay.setImageResource(R.drawable.mini_pause);
					mini_refresh();
				}
			});
			mprev.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					musicManager.playPrev();
					mplay.setImageResource(R.drawable.mini_pause);
					mini_refresh();
				}
			});
			mcycle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					int r = musicManager.toggleRepeat();
					if(r == 0){
						musicManager.toggleRepeat();
						mcycle.setImageResource(R.drawable.mini_cycle);
					}
					else {
						mcycle.setImageResource(R.drawable.mini_once);
					}
				}
			});
			mini_refresh();
		}
	}
	
	protected  void mini_refresh() {
		if(musicManager.getCurrSong() != null){
			if(mminiSong != null)
				mminiSong.setText(musicManager.getCurrSong().filename);
			
			if (musicManager.isPlaying()) {
				mplay.setImageResource(R.drawable.mini_pause);
				if(notification2 != null){
					handler2.removeCallbacks(notification2);
				}
				notification2 = new Runnable() {
					public void run() {
						mini_refresh();
					}
				};
				handler2.postDelayed(notification2,100);
			}
			else{
				mplay.setImageResource(R.drawable.mini_play);
			}
		}
	}
	
	protected void setSwipe() {
		RelativeLayout s = (RelativeLayout) activity.findViewById(R.id.inner_content);
		s.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return gesture.onTouchEvent(arg1);
			}
		});
		
	}
	
	class MyGestureDetector extends SimpleOnGestureListener {
		protected MotionEvent mLastOnDownEvent = null;

	    @Override
	    public boolean onDown(MotionEvent e) {
	        mLastOnDownEvent = e;
	        return super.onDown(e);
	    }
		
		@Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
	        if (e1==null)
	            e1 = mLastOnDownEvent;
	        if(e1 == null)
	        	return true;
	        
	        if (e1.getX() < e2.getX()) {
	        	if(e2.getX() - e1.getX() > 150){
	    			final ViewGroup view = (ViewGroup) activity.findViewById(R.id.inner_content);
	    			ObjectAnimator oa=ObjectAnimator.ofFloat(view, "translationX", 0, phoneWidth);
	    			
	    			oa.addListener(new AnimatorListener() {

	    				@Override
	    				public void onAnimationCancel(Animator arg0) {
	    					// TODO Auto-generated method stub
	    				}

	    				@Override
	    				public void onAnimationEnd(Animator arg0) {
	    				}

	    				@Override
	    				public void onAnimationRepeat(Animator arg0) {
	    					// TODO Auto-generated method stub
	    					
	    				}

	    				@Override
	    				public void onAnimationStart(Animator arg0) {
	    				}
	    			});
	    			oa.setDuration(200);
	    			oa.start();
	    			activity.openStatus = true;
	        	}
	        }
	        
	        return true;
	    }
		
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public abstract void display();
	public abstract void finish();
	public abstract void resume();
	public abstract void refresh();
	
}
