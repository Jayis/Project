package com.sagax.player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class AddPlaylistActivity extends Activity {

	
	private Context mContext;
	private ListView listview;
	private LinearLayout linearLayout;
	private GestureDetector gestureDetector;
	private MediaManager mediaManager;
	private MusicManager musicManager;
	private int buttonWidth = 0;
	private int buttonHeight = 0;
	private int currentindex = 0;
	private ArrayList<AllListAdapter> listAdapter;
	private String[] allAlbum;
	private String id;
	private ImageButton saveButton;
	private ImageButton cancelButton;
	private CoverFlow coverFlow;
	private TextView albumName;
	private TextView albumArtist;
	
	private ArrayList<Song> songList = null;
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		Intent intent = this.getIntent();
		id = intent.getStringExtra("ID");
		mediaManager = MainActivity.getMediaManagerInstance();
		musicManager = MainActivity.getMusicManagerInstance();
		mContext = this;
		songList = new ArrayList<Song>();
		
		if(!id.equals("new"))
			songList = mediaManager.getPlaylistByID(id);
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		buttonWidth = width;
		buttonHeight = getPixels(170);
 
		setContentView(R.layout.addplaylist);	
		setupUI();
		setupAlbumlist();
		initPlaylist();
		refresh();
		setCheck();
		setupMiniPlayer();
	}
	private void setCheck(){
		HashMap<String,Integer> albums = new HashMap<String,Integer>();
		for(int i = 0;i < allAlbum.length; i++){
			albums.put(allAlbum[i], i);
		}
		for(Song song : songList){
			AllListAdapter adapter = listAdapter.get(albums.get(song.album_id+""));
			adapter.selectList.add(song);
		}
	}
	
	
	private void setupUI(){
		listview = (ListView)findViewById(R.id.listView2);
		linearLayout = (LinearLayout)findViewById(R.id.linearLayout1);
		gestureDetector = new GestureDetector(new MyGestureDetector());
		saveButton = (ImageButton)findViewById(R.id.save);
		cancelButton = (ImageButton)findViewById(R.id.cancel);
		coverFlow = new CoverFlow(this);
		albumName = (TextView)findViewById(R.id.textView1);
		albumArtist = (TextView)findViewById(R.id.textView2);
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int position = Integer.parseInt( (String)msg.obj );
			currentindex = position;
			refresh();
       	}
    };
	
	private void setupAlbumlist(){
		allAlbum = mediaManager.getAllAlbumID();
		
		coverFlow.setAdapter(new AlbumAdapter(this));
		
		//coverFlow.setMaxRotationAngle(0);
        coverFlow.setSpacing(-120);
        coverFlow.setSelection(0, true);
        coverFlow.setAnimationDuration(1000);
        Gallery.LayoutParams gLayoutParams = new Gallery.LayoutParams(Gallery.LayoutParams.MATCH_PARENT, 340);
        coverFlow.setLayoutParams(gLayoutParams);


		coverFlow.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
				mHandler.obtainMessage(0, String.valueOf(position)).sendToTarget();
			}

			public void onNothingSelected(AdapterView<?> parent){}
		
		});
		
		linearLayout.addView(coverFlow);
		
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ArrayList<Song> songArrayList = new ArrayList<Song>();
				for (AllListAdapter adapter : listAdapter) {
					songArrayList.addAll(adapter.selectList);
				}
				if(!id.equals("new"))
					mediaManager.setPlaylist(songArrayList, id);
				finish();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	public void refresh(){
		listview.setAdapter(listAdapter.get(currentindex));
		albumName.setText(mediaManager.getAlbumNameByID(allAlbum[currentindex]));
		albumArtist.setText(mediaManager.getAlbumArtistByID(allAlbum[currentindex]));
	}
	
	private void initPlaylist(){
		listAdapter = new ArrayList<AllListAdapter>();
		for(String string : allAlbum){
			AllListAdapter s = new AllListAdapter(this,android.R.layout.simple_list_item_1,mediaManager.getSongsByAlbumID(string),0);
			s.notifyDataSetChanged();
			listAdapter.add(s);
		}
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
	        	if(e2.getX() - e1.getX() > buttonWidth/4){
	        		currentindex = Math.max(0, currentindex-1);
	        	}
	        } else {
	        	if(e1.getX() - e2.getX() > buttonWidth/4){
	        		currentindex = Math.min(allAlbum.length - 1, currentindex + 1);
	        	}
	        }
	        
	        return true;
	    }
		
	}
	protected int getPixels(int dipValue){
        Resources r = getResources();
        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, 
        r.getDisplayMetrics());
        return px;
	}
	public Bitmap getReflection(Bitmap image){
		final int Gap = 0;
		
		int w = image.getWidth();
		int h = image.getHeight();
		
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		
		try {
			Bitmap reflect = Bitmap.createBitmap(image, 0, 3*h/4, w, h/4, matrix, false);
			Bitmap bitmapWithReflection = Bitmap.createBitmap(w, h + h/4 + Gap, Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmapWithReflection);
			canvas.drawBitmap(image, 0, 0, null);
			canvas.drawBitmap(reflect, 0, h+Gap, null);
			
			Paint paint = new Paint();
			LinearGradient shaderGradient = new LinearGradient(0, h + Gap, 0, bitmapWithReflection.getHeight() + Gap, 0x99ffffff, 0x00ffffff, TileMode.CLAMP);
			paint.setShader(shaderGradient);
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			canvas.drawRect(0, h, w, bitmapWithReflection.getHeight()+ Gap, paint);
			if(image != null){
				image.recycle();
				image = null;
			}
			if(reflect != null){
				reflect.recycle();
				reflect = null;
			}
			
			return bitmapWithReflection;
		} catch (Exception e) {
			// TODO: handle exception
			return image;
		}
	}
	private Bitmap getCover(Uri uri){
		ContentResolver res = getContentResolver();
		Bitmap bitmap = null;
		if (uri != null) {
			ParcelFileDescriptor fd = null;
	        try {
	        	fd = res.openFileDescriptor(uri, "r");
	        	bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
	        } catch (FileNotFoundException e) {
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
		
		if(bitmap == null){
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
		}
		return bitmap;
		
	}
	
	protected ImageButton mplay;
	private ImageButton mprev;
	private TextView mminiSong;
	private ImageButton mnext;
	private ImageButton mcycle;
	private Runnable notification2;
	private Handler handler2 = new Handler();
	
	protected void setupMiniPlayer() {
		mplay = (ImageButton)findViewById(R.id.mini_play);
		mprev = (ImageButton)findViewById(R.id.mini_prev);
		mminiSong = (TextView)findViewById(R.id.mini_text);
		mnext = (ImageButton)findViewById(R.id.mini_next);
		mcycle = (ImageButton)findViewById(R.id.mini_once);
		mini_refresh();

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
	
	protected  void mini_refresh() {
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
	
	
    private class AlbumAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;
        private FileInputStream fis;
		private Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		private Handler mHandler;
	
        private ImageView[] mImages;
        
        public AlbumAdapter(Context c) {
            mContext = c;
        }

        public AlbumAdapter(Context c,Handler handler) {
            mContext = c;
			mHandler = handler;
        }
        
        public int getCount() {
            return allAlbum.length;
        }
        
        public Object getItem(int position) {
            return position;
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        
			//Use this code if you want to load from resources
            ImageView i = new ImageView(mContext);
            i.setImageBitmap(getReflection(getCover(mediaManager.getAlbumArtByID( allAlbum[ position ] ))));
            i.setLayoutParams(new CoverFlow.LayoutParams(getPixels(160), getPixels(160)));
            i.setScaleType(ImageView.ScaleType.CENTER_INSIDE); 
            
            //Make sure we set anti-aliasing otherwise we get jaggies
            BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
            drawable.setAntiAlias(true);


            return i;
        }
        /** Returns the size (0.0f to 1.0f) of the views 
         * depending on the 'offset' to the center. */ 
        public float getScale(boolean focused, int offset) { 
            /* Formula: 1 / (2 ^ offset) */ 
            return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset))); 
        } 
        
    }
}
