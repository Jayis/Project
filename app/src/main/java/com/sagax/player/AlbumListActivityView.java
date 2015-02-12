package com.sagax.player;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
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

import com.sagax.player.GalleryView.OnSelectedChangeListener;




public class AlbumListActivityView extends ActivityView {
	private MediaManager mediaManager;
	private MusicManager musicManager;
	private View contentView;
	private View contentView2;
	
	private ImageButton homeButton = null;
	private ImageView change = null;
	private ListView listview;
	private String[] allAlbum = null;
	private LinearLayout linearLayout = null;
	//private HorizontalScrollView horizontalScrollView = null;
	private int length = 3;
	private int currentindex = 0;
	private int buttonWidth = 0;
	private int buttonHeight = 0;
	private List<SongListAdapter> listAdapter = null;
	private GridView gridView = null;
	//private CoverFlow coverFlow;
	private GalleryView galleryView;
	private ImageButton playAll;
	private TextView albumName;
	private TextView albumArtist;
	private TextView albumDur;
    private static final int SWIPE_MIN_DISTANCE = 20;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private ImageAdapter iadapter;
    private AlbumAdapter aAdapter;
	private int type = 0;
	private GestureDetector gestureDetector2;
	private Bitmap bitmap;
	private int phoneWidth = 0;

	public AlbumListActivityView(Activity activity){
		super(activity);
	}
	
	
	@SuppressLint("NewApi")
	public void display(){
		mediaManager = MainActivity.getMediaManagerInstance();	
		musicManager = MainActivity.getMusicManagerInstance();
		
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		phoneWidth = size.x;
		buttonWidth = getPixels(160);
		buttonHeight = getPixels(170);
		
		if(type  == 0){
			activity.onPreExecute();
			allAlbum = mediaManager.getAllAlbumID();
			length = allAlbum.length;
			setupUI();
		}else{
			setupGrid();
		}
		initPlaylist();
		setupMiniPlayer();
	}
	
	private void setupGrid(){
		LayoutInflater inflater = activity.getLayoutInflater();
		contentView2 = inflater.inflate(R.layout.artistgridview, null);
		activity.addContentView(contentView2,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));

		gridView = (GridView)activity.findViewById(R.id.gridView1);
		if(iadapter == null)
			iadapter = new ImageAdapter(activity,allAlbum);
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
		contentView = inflater.inflate(R.layout.albumlist, null);
		activity.addContentView(contentView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
		homeButton = (ImageButton)activity.findViewById(R.id.homebtn);
		change = (ImageView)activity.findViewById(R.id.imageView2);
		listview = (ListView)activity.findViewById(R.id.listView2);
		linearLayout = (LinearLayout)activity.findViewById(R.id.linearLayout1);
		
		playAll = (ImageButton)activity.findViewById(R.id.playall);
		albumName = (TextView)activity.findViewById(R.id.textView1);
		albumArtist = (TextView)activity.findViewById(R.id.textView2);
		albumDur = (TextView)activity.findViewById(R.id.textView3);
		galleryView = new GalleryView(activity, null, (phoneWidth - buttonWidth)/2);
		gestureDetector2 = new GestureDetector(activity,new MySwipeDetector());
		bitmap = getReflection(ActivityView.decodeSampledBitmapFromResource(activity.getResources(), R.drawable.cover, getPixels(150),getPixels(150)));
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int position = Integer.parseInt( (String)msg.obj );
			currentindex = position;
			refresh();
       	}
    };
	
	@SuppressLint("NewApi")
	private void setupAlbumlist(){
		//coverFlow.setMaxRotationAngle(0);
		
		playAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Playlist playlist = new Playlist(mediaManager.getSongsByAlbumID(allAlbum[currentindex]));
                if (playlist.good) {
                    Log.d("status", "good song list");
                    musicManager.setCurrentPlaylist(playlist);
                    musicManager.playIndex(0);
                    ActivityView av = activity.act.get(activity.statusList[2]);
                    av.finish();
                    av = activity.act.get(activity.statusList[0]);
                    activity.init();
                    av.display();
                    av.setSwipe();
                    activity.setClose();
                }
                else{
                    Log.d("status", "all songs not in local");
                    Toast.makeText(activity.getApplicationContext(), "All songs are not in local\nCan't play", Toast.LENGTH_SHORT).show();
                }
			}
		});
		
		homeButton.setOnClickListener(open);
		
		if(aAdapter == null)
			aAdapter = new AlbumAdapter(activity);

		galleryView.setAdapter(aAdapter);
		galleryView.setItemWidth(buttonWidth);
		galleryView.setOnSelectedChangeListener(new OnSelectedChangeListener() {
			
			@Override
			public void onSelectChanged(GalleryView h, int index) {
				// TODO Auto-generated method stub
				currentindex = index;
				refresh();
			}
		});
		galleryView.setSelection(currentindex);
		//galleryView.scrollTo(currentindex * buttonWidth);
		/*coverFlow.setAdapter(aAdapter);
        coverFlow.setSpacing(-120);
        coverFlow.setSelection(currentindex, true);
        coverFlow.setAnimationDuration(1000);
        Gallery.LayoutParams gLayoutParams = new Gallery.LayoutParams(Gallery.LayoutParams.MATCH_PARENT, getPixels(170));
        coverFlow.setLayoutParams(gLayoutParams);


		coverFlow.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
				mHandler.obtainMessage(0, String.valueOf(position)).sendToTarget();
			}

			public void onNothingSelected(AdapterView<?> parent){}
		
		});*/
		
		listview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,long arg3) {
				Playlist playlist = new Playlist(mediaManager.getSongsByAlbumID(allAlbum[currentindex]));
                if (playlist.getSongIndex(position).status != 2) {
                    Toast.makeText(activity.getApplicationContext(), "not in local\nCan't play", Toast.LENGTH_SHORT).show();
                    Log.d("song status", "not in local");
                }
                else {
                    Log.d("song status", "in local");
                    musicManager.setCurrentPlaylist(playlist);
                    musicManager.playIndex(position);
                    ActivityView av = activity.act.get(activity.statusList[2]);
                    av.finish();
                    av = activity.act.get(activity.statusList[0]);
                    activity.init();
                    av.display();
                    av.setSwipe();
                    activity.setClose();
                }
			}
			
		});

		change.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector2.onTouchEvent(event);
			}
		});

	}
	
	private void initPlaylist(){

		new Thread() {
	        @Override
	        public void run() {
	    		listAdapter = new ArrayList<SongListAdapter>();
	    		for(String string : allAlbum){
	    			SongListAdapter s = new SongListAdapter(activity,mediaManager.getSongsByAlbumID(string),musicManager.getCurrSong());
	    			listAdapter.add(s);
	    		}
	    		setupAlbumlist();
	    		activity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						linearLayout.removeAllViews();
						linearLayout.addView(galleryView);
						refresh();
						activity.onPostExecute();
					}
				});
	        }
		}.start();
		

	}
	
	public void refresh(){
		if(listAdapter.size() > 0){
			listview.setAdapter(listAdapter.get(currentindex));
			albumName.setText(mediaManager.getAlbumNameByID(allAlbum[currentindex]));
			albumArtist.setText(mediaManager.getAlbumArtistByID(allAlbum[currentindex]));
			albumDur.setText(mediaManager.getSongsByAlbumID(allAlbum[currentindex]).size()+"songs");
		}
	}
	
	private Bitmap decodeFile(FileDescriptor f,int WIDTH,int HIGHT){
		
		BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(f,null,o);
		final int REQUIRED_WIDTH=WIDTH;
		final int REQUIRED_HIGHT=HIGHT;
        int scale=1;
		while(o.outWidth/scale/2>=REQUIRED_WIDTH && o.outHeight/scale/2>=REQUIRED_HIGHT)
		    scale*=2;
        
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize=scale;
		return BitmapFactory.decodeFileDescriptor(f,null,o2);
	}
	
	private Bitmap getCover(Uri uri){
		ContentResolver res = activity.getContentResolver();
		Bitmap bitmap = null;
		if (uri != null) {
			ParcelFileDescriptor pfd = null;
	        try {
	        	pfd = res.openFileDescriptor(uri, "r");
	        	if (pfd != null) 
	        	{
	        		FileDescriptor fd = pfd.getFileDescriptor();
	                bitmap =  decodeFile(fd,getPixels(100),getPixels(100));
	            }
	        	
	        } catch (FileNotFoundException e) {
	        } catch (IOException e){
	        	e.printStackTrace();
	        }
	        finally {
	        	try {
	        		if (pfd != null)
	        			pfd.close();
				} catch (IOException e) {
				}
			}
		}
		
		return bitmap;
		
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
			Bitmap bitmap= getCover(mediaManager.getAlbumArtByID(idsStrings[position]));
			if(bitmap != null)
				imageView.setImageBitmap(bitmap);
			else {
				imageView.setImageResource(R.drawable.cover);
			}
			textView.setText(mediaManager.getAlbumNameByID(idsStrings[position]));
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(Color.WHITE);
			textView.setHeight(getPixels(37));
			layout.addView(imageView);
			layout.addView(textView);

			return layout;
		}
	}
	
    private class AlbumAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;
        private FileInputStream fis;
		private Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		private Handler mHandler;
        
        
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
        
        	//Log.d("position", position+"");
			//Use this code if you want to load from resources
        	ImageViewRecyclable i = new ImageViewRecyclable(mContext);
            Bitmap b = getCover(mediaManager.getAlbumArtByID( allAlbum[position] ));
            if(b == null)
            	i.setImageBitmap(bitmap);
            else
            	i.setImageBitmap(getReflection(b));
           
            i.setLayoutParams(new CoverFlow.LayoutParams(buttonWidth,buttonWidth));
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
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}


	@Override
	public void resume() {	
		
	}
	
    class MySwipeDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	//swipe down
                	if(type == 0){
	                	MainActivity ma = activity;
	    				ma.init();
	    				type = 1;
	    				display();
	    				setSwipe();
	    				activity.setClose();
                	}
                }else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
					//swipe up
                	if(type == 1){
	                	MainActivity ma = activity;
	    				ma.init();
	    				type = 0;
	    				galleryView.setSelection(currentindex);
	    				display();
	    				setSwipe();
	    				activity.setClose();
                	}
				}
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }


}
