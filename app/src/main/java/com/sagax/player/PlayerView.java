package com.sagax.player;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera.Area;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sagax.player.R;
import com.sagax.player.CircularSeekBar.OnCircularSeekBarChangeListener;

public class PlayerView extends ActivityView{

	
	@SuppressLint("NewApi")
	public PlayerView(Activity c) {
		super(c);
		initMusicManager();
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		actWidth = screenWidth * 25 / 36;
		// TODO Auto-generated constructor stub
	}

	private MusicManager musicManager = null;
	private Song currentSong = null;
	private TextView textView;
	private TextView album = null;
	private TextView artist = null;
	private TextView duration;
	//private TextView currentTime;
	private ImageButton play = null;
	private ImageButton next = null;
	private ImageButton prev = null;
	private ImageButton random = null;
	private ImageButton repeat = null;
	private ImageButton EQButton = null;
	private CircularSeekBar circularSeekBar;
	private ImageButton home = null;
	private int screenWidth;
	private int actWidth;

	//private ImageButton playlistButton = null;
	private SeekBar seekBar;
	private Runnable notification;
	private Handler handler = new Handler();
	private ImageViewRecyclable cover = null;
	private ImageView area = null;
	
	private Song songNow = null;
	
	public static int people = 5566;
	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		LayoutInflater inflater = activity.getLayoutInflater();
		View tmpView;
		tmpView = inflater.inflate(R.layout.activity_main, null);
		activity.addContentView(tmpView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));	
		
		setUpUI();
		setClickEvents();
		refresh();
	}
	
	private void initMusicManager(){
		//musicManager = new MusicManager(activity.getApplicationContext());
		musicManager = MainActivity.getMusicManagerInstance();
		currentSong = musicManager.getCurrSong();
	}

	
	@SuppressLint("NewApi")
	private void setUpUI(){
		textView = (TextView)activity.findViewById(R.id.textView2);
		// textView.setText(currentSong.filename);
		
		album = (TextView)activity.findViewById(R.id.textView4);
		//album.setText(currentSong.album);
		
		artist = (TextView)activity.findViewById(R.id.textView3);
		// artist.setText(currentSong.artist);
		
		duration = (TextView)activity.findViewById(R.id.textView5);
		// duration.setText(currentSong.gtDuration());
		// *currentTime = (TextView)activity.findViewById(R.id.textView1);
		// currentTime.setText(musicManager.getCurrentTime());*/

		play = (ImageButton)activity.findViewById(R.id.imageButton5);
		next = (ImageButton)activity.findViewById(R.id.imageButton2);
		prev = (ImageButton)activity.findViewById(R.id.imageButton1);
		
		//playlistButton = (ImageButton)findViewById(R.id.button1);
		EQButton = (ImageButton)activity.findViewById(R.id.imageButton6);
		random = (ImageButton)activity.findViewById(R.id.imageButton8);
		repeat = (ImageButton)activity.findViewById(R.id.imageButton7);
		
		seekBar = (SeekBar)activity.findViewById(R.id.seekBar1);
		seekBar.setMax(musicManager.getMaxVol());
		seekBar.setProgress(musicManager.getVol());
		
		circularSeekBar = (CircularSeekBar)activity.findViewById(R.id.circularSeekBar1);

		// circularSeekBar.setMax(currentSong.duration);
		// circularSeekBar.setProgress(0);
		cover = (ImageViewRecyclable)activity.findViewById(R.id.imageView4);
		area = (ImageView)activity.findViewById(R.id.imageView3);
		
		circularSeekBar.getLayoutParams().width = actWidth;
		circularSeekBar.getLayoutParams().height = actWidth;
		cover.getLayoutParams().width = screenWidth/2;
		cover.getLayoutParams().height = screenWidth/2;
		area.getLayoutParams().width = actWidth;
		area.getLayoutParams().height = actWidth;
		home = (ImageButton)activity.findViewById(R.id.homebtn);
		//setCover();
		
		//listButton = (Button)findViewById(R.id.button);
		
	}
	
	private void setClickEvents(){
		play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean status = musicManager.togglePlay();
				if(!status){
					play.setImageResource(R.drawable.play);
				}
				else {
					play.setImageResource(R.drawable.pause);
					
					startPlayProgressUpdater();
				}
			}
		});
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(musicManager.playNext()){
					play.setImageResource(R.drawable.pause);
					refresh();
				}else{
					Toast.makeText(activity, "Last Song of Playlist!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(musicManager.playPrev()) {
                    play.setImageResource(R.drawable.pause);
                    refresh();
                }
                else{
                    Toast.makeText(activity, "First Song of Playlist!", Toast.LENGTH_SHORT).show();
                }
			}
		});
		random.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean status = musicManager.toggleRandom();
				if(status){
					random.setImageResource(R.drawable.random_on);
				}
				else {
					random.setImageResource(R.drawable.random);
				}
			}
		});
		EQButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(musicManager.isEQOk()){
					boolean eqOn = toggleEQ();
					if(eqOn){
						EQButton.setImageResource(R.drawable.active);
					}
					else {
						EQButton.setImageResource(R.drawable.identified);
					}
				}
				// TODO Auto-generated method stub
			}
		});
		repeat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int r = musicManager.toggleRepeat();
				if(r==0){
					repeat.setImageResource(R.drawable.cycle);
				}
				else if(r == 1){
					repeat.setImageResource(R.drawable.cycle_on);
				}
				else if(r == 2){
					repeat.setImageResource(R.drawable.repeat);
				}
			}
		});

		seekBar.setOnTouchListener(new OnTouchListener() {
			@Override 
			public boolean onTouch(View v, MotionEvent event) {
				volChange(v);
				return false; }
		});
		
		circularSeekBar.setOnSeekBarChangeListener(new OnCircularSeekBarChangeListener() {
			
			@Override
			public void onProgressChanged(CircularSeekBar circularSeekBar,
					int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				if(fromUser){
					musicManager.seekTo(progress);
				}
				circularSeekBar.setTime(musicManager.getCurrentTime());		
			}
		});
		
		home.setOnClickListener(open);
	}
	
	private boolean toggleEQ(){
		return musicManager.toggleEQ();
	}
	
	public void refresh(){
		currentSong = musicManager.getCurrSong();
		textView.setText(currentSong.title);
		album.setText(currentSong.album);
		artist.setText(currentSong.artist);
		setCover();
		circularSeekBar.setMax(currentSong.duration);
		circularSeekBar.setProgress(0);
		duration.setText(currentSong.gtDuration());
		if(!musicManager.isPlaying()){
			play.setImageResource(R.drawable.play);
		}
		else {
			play.setImageResource(R.drawable.pause);
			startPlayProgressUpdater();
		}
		
		if(musicManager.isEQOk()){
			EQButton.setImageResource(R.drawable.identified);
		}else{
			EQButton.setImageResource(R.drawable.m);
		}
		
		if(musicManager.isEQOn()){
			EQButton.setImageResource(R.drawable.active);
		}
		
	}
	
	private void volChange(View v){
		SeekBar s = (SeekBar)v;
		musicManager.setVol(s.getProgress());
	}
	
	public void startPlayProgressUpdater() {
		if(musicManager.isEQOk()){
			if(musicManager.isEQOn())
				EQButton.setImageResource(R.drawable.active);
			else
				EQButton.setImageResource(R.drawable.identified);
		}else{
			EQButton.setImageResource(R.drawable.m);
		}
		if(seekBar != null){
			seekBar.setProgress(musicManager.getVol());
		}
		if(circularSeekBar != null){
			circularSeekBar.setProgress(musicManager.getCurrent());
			circularSeekBar.setTime(musicManager.getCurrentTime());
			//currentTime.setText(musicManager.getCurrentTime());
		}
		if (musicManager.isPlaying()) {
			Song tmp = musicManager.getCurrSong();
			if(tmp != null && tmp != songNow){
				songNow = tmp;
				refresh();
			}
			
			if(notification != null){
				handler.removeCallbacks(notification);
			}
			notification = new Runnable() {
				public void run() {
					startPlayProgressUpdater();
				}
			};
			handler.postDelayed(notification,100);
		}
		else{
			play.setImageResource(R.drawable.play);
		}
	}
	
	private void setCover(){
		ContentResolver res = activity.getContentResolver();
		Uri uri = currentSong.albumPath;
		if (uri != null) {
			ParcelFileDescriptor fd = null;
	        try {
	        	fd = res.openFileDescriptor(uri, "r");
	        	Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
	        	Bitmap circleBitmap = ImageHelper.getRoundedCornerBitmap(bitmap);
	        	if((BitmapDrawable)cover.getDrawable()!=null && ((BitmapDrawable)cover.getDrawable()).getBitmap() != null)
	        		((BitmapDrawable)cover.getDrawable()).getBitmap().recycle();
	        	cover.setImageBitmap(circleBitmap);
	        	if(bitmap != null){
	        		bitmap.recycle();
	        	}
	        	//cover.setImageURI(currentSong.albumPath);
	        } catch (FileNotFoundException e) {
	        	Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.play_cover);
	        	//Bitmap circleBitmap = ImageHelper.getRoundedCornerBitmap(bitmap);
	        	cover.setImageBitmap(bitmap);
	        } catch (IOException e){
	        	e.printStackTrace();
	        }
	        finally {
	        	try {
	        		if (fd != null)
	        			fd.close();
				} catch (IOException e) {
				}
			}
		}
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		if(handler != null){
	   		handler.removeCallbacks(notification);
	   		//handler = null;  
		}
   		/*musicManager.destory();*/
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		//startPlayProgressUpdater();
	}
	

}
