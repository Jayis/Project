package com.sagax.player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sagax.player.R;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageButton;

import org.apache.http.impl.client.DefaultHttpClient;

public class MainActivity extends Activity {
    private static List<Map<String, Object>> DLQsongs;
    public static DownloadManager dm;

	private static Context context;
	private static MediaManager mediaManager = null;
	private static MusicManager musicManager = null;
	public Map<String,ActivityView> act = null;
	private Map<String,Button> imgButton = null;
	public String[] statusList = { "home" , "artist" , "album" , "song" , "playlist"};
	private Map<String, Integer> buttonList = null;
	private ImageButton backButton;
	private String Err=null;
	private String current = "";
	public boolean openStatus = false;
	private int phoneWidth;
	private GestureDetector g = new GestureDetector(new MyGestureDetector());
	
	public static MediaManager getMediaManagerInstance() {
		// singleton
		if( mediaManager == null){
			synchronized( MainActivity.class){
				mediaManager = new MediaManager(context);
			}
		}
		return mediaManager;
	}
	
	public static MusicManager getMusicManagerInstance() throws IndexOutOfBoundsException {
		// singleton
		if( musicManager == null){
			synchronized( MainActivity.class){
				musicManager = new MusicManager(context);
			}
		}
		return musicManager;
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("OnCreate");
		// the order cannot be changed, the context must be initialized before these
		// managers' getInstance method been called.
		context = this;
 		if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 )
 			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

        // Harry
        DLQsongs = new ArrayList<Map<String, Object>>();
        this.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        //

		try{
		mediaManager = getMediaManagerInstance();
		musicManager = getMusicManagerInstance();
		}catch(Exception e){
			Log.e("init error",e.toString());
			Err="noSong";
		}

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		phoneWidth = size.x;
		init();
		current = statusList[0];
        /*
		if(musicManager.getCurrSong() != null){
			act.get(current).display();
			act.get(current).setSwipe();
			setClose();
		}
		*/
	}
	
    private ProgressDialog progressdialog;

    public void onPreExecute(){ 
    	if(progressdialog == null){
    		progressdialog = new ProgressDialog(this);
    		progressdialog.setMessage("��J��");
    	}
        progressdialog.show();    
    }

    public void onPostExecute(){
    	progressdialog.dismiss();
    }
	
	@SuppressLint("NewApi")
	public void setClose() {
		
		// TODO Auto-generated method stub
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewGroup view = (ViewGroup) findViewById(R.id.inner_content);
				ObjectAnimator oa=ObjectAnimator.ofFloat(view, "translationX", phoneWidth, 0);
				oa.setDuration(200);
				oa.start();
				openStatus = false;
			}
		});
		AbsoluteLayout view = (AbsoluteLayout) findViewById(R.id.backlayout);
		view.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				g.onTouchEvent(arg1);
				return false;
			}
		});
	}
	
	public void init(){
		setContentView(R.layout.main);
		if(act == null){
			act = new HashMap<String,ActivityView>();
			imgButton = new HashMap<String, Button>();
			buttonList = new HashMap<String, Integer>();
			
			//if(Err==null){
				act.put(statusList[0],new PlayerView(this));
				act.put(statusList[1],new ArtistListView(this));
				act.put(statusList[2],new AlbumListActivityView(this));
				act.put(statusList[3],new SongListActivityView(this));
				act.put(statusList[4],new PlaylistActivityView(this));
			//}
		}
			
		backButton = (ImageButton) findViewById(R.id.backbtn);
		imgButton.clear();
		buttonList.clear();
			
		Button homeButton = (Button)findViewById(R.id.home);
		imgButton.put(statusList[0], homeButton);
		buttonList.put(statusList[0], R.drawable.menu_play_on);
		
		Button artistButton = (Button)findViewById(R.id.artist);
		imgButton.put(statusList[1], artistButton);
		buttonList.put(statusList[1], R.drawable.menu_play_on);
		
		Button albumButton = (Button)findViewById(R.id.album);
		imgButton.put(statusList[2], albumButton);
		buttonList.put(statusList[2], R.drawable.menu_play_on);		

		Button songButton = (Button)findViewById(R.id.song);
		imgButton.put(statusList[3], songButton);
		buttonList.put(statusList[3], R.drawable.menu_play_on);	
		
		Button playlistButton = (Button)findViewById(R.id.playlist);
		imgButton.put(statusList[4], playlistButton);
		buttonList.put(statusList[4], R.drawable.menu_play_on);	
		
		setupListener();
	}
	
	public void setupListener(){
		for(String s : statusList){
			
			final String nextActivityView = s;
			
			if(s == statusList[0])
				continue;
			
			Button tmp = imgButton.get(s);
			tmp.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					act.get(current).finish();
					// assign current to next view 
					current = nextActivityView;
					init();
					act.get(current).display();
					act.get(current).setSwipe();
					openStatus = false;
					setClose();
				}
			});
		}
	}
	
	public void removeListener(){
		for(String s : statusList){
			imgButton.get(s).setOnClickListener(null);
		}
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
        	if(openStatus){
				final ViewGroup view = (ViewGroup) findViewById(R.id.inner_content);
				view.clearAnimation();
				ObjectAnimator ob=ObjectAnimator.ofFloat(view, "translationX", phoneWidth, 0);
				ob.setDuration(400);
				ob.start();
				openStatus = false;
        	}else
        		finish();
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    

    
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {     
    	super.onActivityResult(requestCode, resultCode, data); 
    	
    	if(requestCode == 5566){
    		if (resultCode == Activity.RESULT_OK) { 
    			String newText = data.getStringExtra("switch");
    			// TODO Update your TextView.
    		} 
    	}
    }

    @SuppressLint("NewApi")
	@Override
    protected void onDestroy() {
    	super.onDestroy();
   	 	
   	 	for(String string : act.keySet())
   	 		act.get(string).finish();
   	 	
   	 	musicManager.release();

        // Harry
        this.unregisterReceiver(onComplete);
	}
	
    protected void onResume() {
   	 	super.onResume();
   	 	// call when the activity resumed , if anything needs to update 
   	 	// call the override method.
   	 	System.out.println("OnResume");
 		for(String string : act.keySet())
 			act.get(string).resume();
   }
    
    protected void onRestart() {
		super.onRestart();
 		/*for(String string : act.keySet())
 			act.get(string).refresh();*/
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
	        
	        if (e2.getX() < e1.getX()) {
	        	if(e1.getX() - e2.getX() > 150){
					ViewGroup view = (ViewGroup) findViewById(R.id.inner_content);
					ObjectAnimator oa=ObjectAnimator.ofFloat(view, "translationX", phoneWidth, 0);
					oa.setDuration(200);
					oa.start();
					openStatus = false;
	        	}	   
	        }
	        return true;
	    }
		
	}

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            // your code
            //this_button.setText("in Local");
            //tools.showString("complete", this_act);
            long tmp_query;
            Cursor tmp_cursor;
            int tmp_status;
            Button tmp_butt;
            Song tmp_song;

            for (int i = 0; i < DLQsongs.size(); i++ ) {
                tmp_query = (Long) DLQsongs.get(i).get("queryID");
                Log.d("qID", String.valueOf(tmp_query));
                tmp_cursor = dm.query(new DownloadManager.Query().setFilterById(tmp_query));

                if (tmp_cursor!=null) {
                    tmp_cursor.moveToFirst();

                    tmp_status = tmp_cursor.getInt(tmp_cursor.getColumnIndex(dm.COLUMN_STATUS));

                    if (tmp_status == dm.STATUS_SUCCESSFUL) {
                        // update Song in songList
                        tmp_song = mediaManager.getSongByID((String) DLQsongs.get(i).get("songID"));
                        tmp_song.status = 2;
                        tmp_song.data = (String)DLQsongs.get(i).get("localurl");

                        //
                        tmp_butt = (Button) DLQsongs.get(i).get("DL_butt");
                        tmp_butt.setText("Download Complete");

                        DLQsongs.remove(i);
                        break;
                    }
                }
            }
        }
    };

    public static DownloadManager shareDM () {
        return dm;
    }
    public static List<Map<String, Object>> shareDLQ () {return DLQsongs;}
}
