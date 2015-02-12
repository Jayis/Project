package com.sagax.player;

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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView.OnEditorActionListener;

import com.sagax.player.R;
import com.sagax.player.AlbumListActivityView.ImageAdapter;
import com.sagax.player.AlbumListActivityView.MySwipeDetector;
import com.sagax.player.ArcScrollView.OnIndexChangeListener;

public class SongListActivityView extends ActivityView {

	private MediaManager mediaManager;
	private MusicManager musicManager;
	private View contentView;
	private View contentView2;
	
	private ImageButton homeButton = null;
	private ImageView change = null;
	private ListView listview;
	private List<Song> allSong;
	private LinearLayout linearLayout = null;
	private int length = 3;
	private int currentindex = 0;
	private int buttonWidth = 0;
	private int buttonHeight = 0;
	private GridView gridView = null;
	private EditText etSearchbox;
	private AllListAdapter adapter;
    private static final int SWIPE_MIN_DISTANCE = 20;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private int type = 1;
	private GestureDetector gestureDetector;
	//private TextFlow textFlow;
	private ArcScrollView arcScrollView;
	private ImageView searchButton;
	private boolean scrolling = false;
	private OnIndexChangeListener linster;

	public SongListActivityView(Activity activity){
		super(activity);
	}
	
	
	@SuppressLint("NewApi")
	public void display(){
		mediaManager = MainActivity.getMediaManagerInstance();	
		musicManager = MainActivity.getMusicManagerInstance();
		
		LayoutInflater inflater = activity.getLayoutInflater();
		contentView = inflater.inflate(R.layout.songsearchlist, null);
		activity.addContentView(contentView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
		
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		buttonWidth = width;
		buttonHeight = getPixels(170);
		
		setupUI();
		setupSonglist();
		refresh();
		setupMiniPlayer();
	}
	

	private void setupUI(){
		homeButton = (ImageButton)activity.findViewById(R.id.homebtn);
		change = (ImageView)activity.findViewById(R.id.imageView2);
		listview = (ListView)activity.findViewById(R.id.listView2);
		linearLayout = (LinearLayout)activity.findViewById(R.id.linearLayout1);
		gestureDetector = new GestureDetector(activity,new MyGestureDetector());
		etSearchbox = (EditText)activity.findViewById(R.id.etSearchbox);
		arcScrollView = new ArcScrollView(activity);
		searchButton = (ImageView)activity.findViewById(R.id.search);
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int position = Integer.parseInt( (String)msg.obj );
			currentindex = position;
			if(!scrolling)
				listview.setSelection(position);
			//Log.d("xxx", position+"");
       	}
    };
	
	@SuppressLint("NewApi")
	private void setupSonglist(){
		allSong = mediaManager.getAllSong();
		
		adapter = new AllListAdapter(activity, android.R.layout.simple_list_item_1, allSong,1);
		adapter.setActivity((MainActivity)activity);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(null);
		
		linster = new OnIndexChangeListener() {
			
			@Override
			public void onIndexChanged(ArcScrollView a, int index) {
				// TODO Auto-generated method stub
				listview.setSelection(index);
			}
		};
		
		arcScrollView.setLetter(adapter.getTitles(), 0);
		arcScrollView.setClickable(true);
		arcScrollView.setOnIndexChangeListener(linster);
		linearLayout.addView(arcScrollView);
		
		listview.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				// TODO Auto-generated method stub
				if(arg1 == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
					scrolling = true;
				}else if(arg1 == OnScrollListener.SCROLL_STATE_IDLE){
					scrolling = false;
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int firstVisible, int visibleCount, int totalCount) {
				// TODO Auto-generated method stub
				if(scrolling){
					currentindex = firstVisible;
					arcScrollView.setCurrentIndex(currentindex);
				}
			}
		});
		
		etSearchbox.setOnFocusChangeListener(new View.OnFocusChangeListener() {

		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		        	searchButton.setImageBitmap(null);
		        }else{
		        	searchButton.setImageResource(R.drawable.icon_search);
		        }
		    }

		});
		
		etSearchbox.addTextChangedListener(new TextWatcher() {
		    @Override
		    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		        // TODO Auto-generated method stub
		        List<Cata> catas = adapter.applyFilter(arg0);
		        arcScrollView.setLetter(adapter.getTitles(), 0);
		    }
		    @Override
		    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		            int arg3) {
		        // TODO Auto-generated method stub
		    }
		    @Override
		    public void afterTextChanged(Editable arg0) {
		        // TODO Auto-generated method stub
		    }
		});
		
		etSearchbox.setOnEditorActionListener(new OnEditorActionListener() {
	        public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
	        	
	            if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
	                return false;
	            } else if (actionId == EditorInfo.IME_ACTION_SEARCH|| event == null|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			        List<Cata> catas = adapter.applyFilter(etSearchbox.getText());
			        InputMethodManager in = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		            in.hideSoftInputFromWindow(activity.getCurrentFocus().getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
		        	etSearchbox.clearFocus();
		        	searchButton.requestFocus();
		        	if(etSearchbox.getText().toString() == null || etSearchbox.getText().toString().equals(""))
		        		searchButton.setImageResource(R.drawable.icon_search);  
		        	else {
		        		searchButton.setImageBitmap(null);
					}
	            }
	            return false;
	        }
	    });
		
		homeButton.setOnClickListener(open);
		
		final GestureDetector gestureDetector2 = new GestureDetector(activity,new MySwipeDetector());
		
		change.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector2.onTouchEvent(event);
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
			//textFlow.invalidate();
	        
	        return true;
	    }
		
	}
	
	public void refresh(){
		//allSong = mediaManager.getAllSong();
	}
	
	private Bitmap getCover(Uri uri){
		ContentResolver res = activity.getContentResolver();
		Bitmap bitmap = null;
		if (uri != null) {
			ParcelFileDescriptor fd = null;
	        try {
	        	fd = res.openFileDescriptor(uri, "r");
	        	bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
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
			bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
		}
		return bitmap;
		
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
			Bitmap bitmap = getCover(mediaManager.getAlbumArtByID(idsStrings[position]));
			
			imageView.setImageBitmap(bitmap);
			textView.setText(mediaManager.getAlbumNameByID(idsStrings[position]));
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(Color.WHITE);
			textView.setHeight(getPixels(37));
			layout.addView(imageView);
			layout.addView(textView);
			return layout;
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
                		
                		change.setImageResource(R.drawable.division_up);
                		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getPixels(25));
                		lp.setMargins(0, getPixels(210), 0, 0);
                		change.setLayoutParams(lp);
                		type = 1;
                	}
                }else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
					//swipe up
                	if(type == 1){
                		change.setImageResource(R.drawable.division);
                		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getPixels(25));
                		lp.setMargins(0, getPixels(40), 0, 0);
                		change.setLayoutParams(lp);
                		type = 0;
                	}
				}
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }
    /*private class TextAdapter extends BaseAdapter {
        private Context mContext;
		private Handler mHandler;
		private List<Cata> words;
		private LayoutInflater li;
        private ImageView[] mImages;
        
        public TextAdapter(Context c,List<Cata> cata) {
            mContext = c;
            words = cata;
        }

        public TextAdapter(Context c,Handler handler,List<Cata> cata) {
            mContext = c;
			mHandler = handler;
            words = cata;
        }
        
    	public void applyFilter(List<Cata> newword){
    		words = newword;
    		this.notifyDataSetChanged();
    	}
        
        public int getCount() {
            return words.size();
        }
        
        public Object getItem(int position) {
            return words.get(position);
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        
        	int minus = Math.abs(currentindex - position);
        	//convertView = li.inflate(R.layout.textflow,null);
			//Use this code if you want to load from resources
        	TextView i;
        	if(convertView == null){
        		i = new TextView(mContext);
        	}else{
        		i = (TextView)convertView;
        	}
            i.setLayoutParams(new TextFlow.LayoutParams(getPixels(80), getPixels(160)));
            i.setTextColor(Color.WHITE);
            
            i.setTextSize(30);
            i.setGravity(Gravity.CENTER);
            i.setText(words.get(position).title);
            //i.setText(minus+"");
            return i;
        }
        public float getScale(boolean focused, int offset) { 
            return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset))); 
        } 
        
    }*/
}
