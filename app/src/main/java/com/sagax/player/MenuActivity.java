package com.sagax.player;

import com.sagax.player.R;

import lib.slideout.SlideoutHelper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;


public class MenuActivity extends FragmentActivity{

	private ImageButton peopleButton = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mSlideoutHelper = new SlideoutHelper(this);
	    mSlideoutHelper.activate();
	    getSupportFragmentManager().beginTransaction().add(R.id.slideout_placeholder, new MenuFragment(), "menu").commit();
	    init();
	    setClick();
	    mSlideoutHelper.open();

	}
	public void onResume(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	public void init(){
		peopleButton = (ImageButton)findViewById(R.id.people);
	}

	public void setClick(){
		peopleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSlideoutHelper.close();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			mSlideoutHelper.close();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	public SlideoutHelper getSlideoutHelper(){
		return mSlideoutHelper;
	}
	
	private SlideoutHelper mSlideoutHelper;

}
