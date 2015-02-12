package com.sagax.player;

import com.sagax.player.R;

import lib.slideout.SlideoutActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.MenuItem;


public class SampleActionbarActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
	    	finish();
	    }
	    setContentView(R.layout.sample_actionbar);
	    
	}
	public void onResume(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
			SlideoutActivity.prepare(SampleActionbarActivity.this, R.id.inner, width);
			startActivity(new Intent(SampleActionbarActivity.this, MenuActivity.class));
			overridePendingTransition(0, 0);
		}
		return true;
	}
	
}
