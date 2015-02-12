
package com.sagax.player;

import java.util.ArrayList;
import java.util.List;

import com.sagax.player.HorizontalListView.OnSelectedChangeListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ArtistListView extends ActivityView {
	private MediaManager mediaManager;
	private MusicManager musicManager;
	private View contentView;
	private View contentView2;
	
	private ImageButton homeButton = null;
	private ImageView change = null;
	private ListView listview;
	private String[] allSinger = null;
	private LinearLayout linearLayout = null;
	private HorizontalListView horizontalScrollView = null;
	private int length = 3;
	private int currentindex = 0;
	private int buttonWidth = 0;
	private int buttonHeight = 0;
	private List<SongListAdapter> listAdapter = null;
	private GridView gridView = null;
	private int type = 0;
	private ImageAdapter iadapter ;
	private int imgWidth = 0;
	private ArtistAdapter artistAdapter;
    private static final int SWIPE_MIN_DISTANCE = 20;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private Bitmap bitmap = null;
    private GestureDetector gestureDetector2;

	private GestureDetector gestureDetector;
	
	@SuppressLint("NewApi")
	public ArtistListView(Activity activity){
		super(activity);
		
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		buttonWidth = width;
		buttonHeight = getPixels(170);
		
		imgWidth = width/2 - getPixels(40);
	}
	
	public void display(){
		mediaManager = MainActivity.getMediaManagerInstance();	
		musicManager = MainActivity.getMusicManagerInstance();
		
		
		if(type == 0){
			activity.onPreExecute();
			setupUI();
			initPlaylist();
		}else{
			setupGrid();
		}
		setupMiniPlayer();
	}
	
	private void setupGrid(){
		
		
		LayoutInflater inflater = activity.getLayoutInflater();
		contentView2 = inflater.inflate(R.layout.artistgridview, null);
		activity.addContentView(contentView2,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));

		gridView = (GridView)activity.findViewById(R.id.gridView1);
		iadapter = new ImageAdapter(activity,allSinger);
		gridView.setAdapter(iadapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				// TODO Auto-generated method stub
				currentindex = position;
				activity.init();
				type = 0;
				display();
				setSwipe();
				activity.setClose();
				//autoSmoothScroll();
			}
			
		});
		ImageButton home = (ImageButton)activity.findViewById(R.id.homebtn);
		home.setOnClickListener(open);
		ImageView ch = (ImageView)activity.findViewById(R.id.imageView2);
		final GestureDetector gestureDetector2 = new GestureDetector(activity,new MySwipeDetector());
		
		ch.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector2.onTouchEvent(event);
			}
		});
	}

	private void setupUI(){
		LayoutInflater inflater = activity.getLayoutInflater();
		contentView = inflater.inflate(R.layout.artistlist, null);
		activity.addContentView(contentView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
		homeButton = (ImageButton)activity.findViewById(R.id.homebtn);
		change = (ImageView)activity.findViewById(R.id.imageView2);
		listview = (ListView)activity.findViewById(R.id.listView2);
		linearLayout = (LinearLayout)activity.findViewById(R.id.linearLayout1);
		horizontalScrollView = (HorizontalListView)activity.findViewById(R.id.horizon1);
		gestureDetector = new GestureDetector(activity,new MyGestureDetector());
		gestureDetector2 = new GestureDetector(activity,new MySwipeDetector());
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int position = Integer.parseInt( (String)msg.obj );
       	}
    };
	
	@SuppressLint("NewApi")
	private void setupSingerlist(){
		
		allSinger = mediaManager.getAllArtistID();
		length = allSinger.length;
		
		
		change.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector2.onTouchEvent(event);
			}
		});
		
		horizontalScrollView.setOnSelectedChangeListener(new OnSelectedChangeListener() {
			
			@Override
			public void onSelectChanged(HorizontalListView h, int index) {
				// TODO Auto-generated method stub
				currentindex = index;
				refresh();
			}
		});
		
		
		listview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,long arg3) {
				Playlist playlist = new Playlist(mediaManager.getSongsByArtistID(allSinger[currentindex]));
                if (playlist.getSongIndex(position).status != 2) {
                    Toast.makeText(activity.getApplicationContext(), "not in local\nCan't play", Toast.LENGTH_SHORT).show();
                    Log.d("song status", "not in local");
                }
                else {
                    Log.d("song status", "in local");
                    musicManager.setCurrentPlaylist(playlist);
                    musicManager.playIndex(position);
                    MainActivity ma = (MainActivity) activity;
                    ActivityView av = ma.act.get(ma.statusList[1]);
                    av.finish();
                    av = ma.act.get(ma.statusList[0]);
                    ma.init();
                    av.display();
                    av.setSwipe();
                    ma.setClose();
                }
			}
			
		});
		
		listview.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub

				return false;
			}
		});
		
		homeButton.setOnClickListener(open);
	}
	
	private void initPlaylist(){

		new Thread() {
	        @Override
	        public void run() {	
	        	
	        	setupSingerlist();
	        	
	    		listAdapter = new ArrayList<SongListAdapter>();
	    		for(String string : allSinger){
	    			SongListAdapter s = new SongListAdapter(activity,mediaManager.getSongsByArtistID(string),musicManager.getCurrSong());
	    			listAdapter.add(s);
	    		}
	    		
	    		bitmap = getReflection(ActivityView.decodeSampledBitmapFromResource(activity.getResources(), R.drawable.cover, imgWidth,imgWidth));
	    		if(artistAdapter == null){
	    			artistAdapter = new ArtistAdapter(activity);

	    		}
	    		activity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(type == 0){
							horizontalScrollView.setAdapter(artistAdapter);
							horizontalScrollView.setItemWidth(buttonWidth);
							horizontalScrollView.setSelection(currentindex);
							horizontalScrollView.scrollTo(currentindex * buttonWidth);
							
							refresh();
							activity.onPostExecute();
						}
					}
				});
	        }
		}.start();

	}

	
	
	public void refresh(){
		if(listAdapter.size() > 0)
			listview.setAdapter(listAdapter.get(currentindex));
	}
	
	public Bitmap getReflection(Bitmap image){
		final int Gap = 0;
		Bitmap orignalBitmap = image;
		
		int w = orignalBitmap.getWidth();
		int h = orignalBitmap.getHeight();
		
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		
		Bitmap reflect = Bitmap.createBitmap(orignalBitmap, 0, h/2, w, h/2, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(w, h + h/2 + Gap, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(orignalBitmap, 0, 0, null);
		canvas.drawBitmap(reflect, 0, h+Gap, null);
		
		Paint paint = new Paint();
		LinearGradient shaderGradient = new LinearGradient(0, h + Gap, 0, bitmapWithReflection.getHeight() + Gap, 0x99ffffff, 0x00ffffff, TileMode.CLAMP);
		paint.setShader(shaderGradient);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, h, w, bitmapWithReflection.getHeight()+ Gap, paint);
		if(orignalBitmap != null && orignalBitmap.isRecycled()){
			orignalBitmap.recycle();
			orignalBitmap = null;
		}
		if(reflect != null && reflect.isRecycled()){
			reflect.recycle();
			reflect = null;
		}
		
		return bitmapWithReflection;
	}
	
	public class ArtistAdapter extends BaseAdapter{
		private Context mContext;
		public ArtistAdapter(Context c){
			mContext = c;
		}
		public int getCount(){
			return allSinger.length;
		}
		public Object getItem(int position){
			return null;
		}
		public long getItemId(int position){
			return 0;
		}
		public View getView(int position, View view, ViewGroup parent){
			
			String name = mediaManager.getArtistNameByID(allSinger[position]);
			
			ArrayList<Song> songs = mediaManager.getSongsByArtistID(allSinger[position]);
			
			
			if( view == null){
				LayoutInflater vi = LayoutInflater.from( mContext );
				view = vi.inflate(R.layout.artistcell,null);
			}  
			LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(buttonWidth,buttonHeight);
			view.setLayoutParams(layout);
			((TextView)view.findViewById(R.id.artistname)).setText(name);
			((TextView)view.findViewById(R.id.songs)).setText(songs.size()+" songs");
			
			ImageView tmpImg = (ImageView) (view.findViewById(R.id.cover));
			tmpImg.getLayoutParams().width = imgWidth;
		
			tmpImg.setImageBitmap(bitmap);
			
			ImageButton playAllButton = (ImageButton)view.findViewById(R.id.playall);
			
			//Log.d("position",position+"");
			
			playAllButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Playlist playlist = new Playlist(mediaManager.getSongsByArtistID(allSinger[currentindex]));
                    if (playlist.good) {
                        Log.d("status", "good song list");
                        musicManager.setCurrentPlaylist(playlist);
                        musicManager.playIndex(0);
                        ActivityView av = activity.act.get(activity.statusList[4]);
                        av.finish();
                        av = activity.act.get(activity.statusList[0]);
                        activity.init();
                        av.display();
                        av.setSwipe();
                        activity.setClose();
                    }
                    else {
                        Log.d("status", "all songs not in local");
                        Toast.makeText(activity.getApplicationContext(), "All songs are not in local\nCan't play", Toast.LENGTH_SHORT).show();
                    }
				}
			});

			return view;
		}
	}
	
	public class ImageAdapter extends BaseAdapter{
		private Context mContext;
		private String[] idsStrings;
		public ImageAdapter(Context c, String[] s){
			mContext = c;
			idsStrings = s;
		}
		public int getCount(){
			return idsStrings.length;
		}
		public Object getItem(int position){
			return null;
		}
		public long getItemId(int position){
			return 0;
		}
		public View getView(int position, View view, ViewGroup parent){
			LinearLayout layout = new LinearLayout(mContext);
			ImageView imageView = new ImageView(mContext);
			TextView textView = new TextView(mContext);
			
			imageView.setLayoutParams(new GridView.LayoutParams(getPixels(100), getPixels(100)));
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setPadding(8, 8, 8, 8);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setGravity(Gravity.CENTER);
			imageView.setImageResource(R.drawable.cover);
			textView.setText(mediaManager.getArtistNameByID(idsStrings[position]));
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(Color.WHITE);
			textView.setHeight(getPixels(37));
			layout.addView(imageView);
			layout.addView(textView);
			return layout;
		}
	}
	
    class MySwipeDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	//swipe down
                	if(type == 0){
	    				activity.init();
	    				type = 1;
	    				display();
	    				setSwipe();
	    				activity.setClose();
                	}
                }else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
					//swipe up
                	if(type == 1){
	    				activity.init();
	    				type = 0;
	    				display();
	    				setSwipe();
	    				activity.setClose();
	    				horizontalScrollView.setSelection(currentindex);
                	}
				}
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		if(bitmap != null){
			bitmap.recycle();
		}
	}


	@Override
	public void resume() {	
		
	}
		
	
}
