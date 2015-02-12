package com.sagax.player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sagax.player.R;
import com.sagax.player.SongListActivityView.MySwipeDetector;

import android.R.bool;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistActivityView extends ActivityView {
	private MediaManager mediaManager;
	private MusicManager musicManager;
	
	private MusiclistDialog editDialog;
	private ImageButton button;

	private AlertDialog createListnameDialog;
	private View contentView;
	
	private ImageButton newPlay = null;
	private ImageButton homeButton = null;
	private TextView playlistText = null;
	private TextView note = null;
	private ImageView change = null;
	private ListView listview;
	
	private String[] allPlaylist = null;
	private LinearLayout linearLayout = null;
	private HorizontalScrollView horizontalScrollView = null;
	private int length = 3;
	private int currentindex = 0;
	private int buttonWidth = 0;
	private int buttonHeight = 0;
	private int screenWidth = 0;
	
	private List<SongListAdapter> listAdapter = null;
	private List<LinearLayout> contents= null;
	//private LinearLayout content;

	private GestureDetector gestureDetector;

	@SuppressLint("NewApi")
	public PlaylistActivityView(Activity activity){
		super(activity);
		
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
	}
	
	
	public void display(){
		mediaManager = MainActivity.getMediaManagerInstance();	
		musicManager = MainActivity.getMusicManagerInstance();
		
		LayoutInflater inflater = activity.getLayoutInflater();
		contentView = inflater.inflate(R.layout.playlist, null);
		activity.addContentView(contentView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
		
		buttonWidth = 180;
		buttonHeight = 100;
		
		setupUI();
		setupPlaylist();
		initPlaylist();
		setupMiniPlayer();
		autoSmoothScroll();
	}

	private void setupUI(){
		newPlay = (ImageButton)activity.findViewById(R.id.newone);
		homeButton = (ImageButton)activity.findViewById(R.id.homebtn);
		playlistText = (TextView)activity.findViewById(R.id.textView1);
		note = (TextView)activity.findViewById(R.id.textView2);
		change = (ImageView)activity.findViewById(R.id.imageView2);
		listview = (ListView)activity.findViewById(R.id.listView2);
		linearLayout = (LinearLayout)activity.findViewById(R.id.linearLayout1);
		horizontalScrollView = (HorizontalScrollView)activity.findViewById(R.id.horizon1);
		gestureDetector = new GestureDetector(activity,new MyGestureDetector());
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int position = Integer.parseInt( (String)msg.obj );
       	}
    };
	
	private void setupPlaylist(){
		allPlaylist = mediaManager.getAllPlaylistID();
		length = allPlaylist.length;
		setupLayout();
		

		
		horizontalScrollView.setOnTouchListener(new OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {	
	        	
	        	if(event.getAction() == MotionEvent.ACTION_MOVE){
	        		if(contents.size() > 0)
	        			contents.get(currentindex).setVisibility(ViewGroup.INVISIBLE);
	        		//content.setVisibility(ViewGroup.INVISIBLE);
	        	}
	        	
	        	
	            if (gestureDetector.onTouchEvent(event)) {
	            	
	            	
		            if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
		            	horizontalScrollView.smoothScrollTo(getPixels(buttonWidth) * currentindex, 0);
		            	//content.setVisibility(ViewGroup.VISIBLE);
		            	refresh();
		            }
	                return true;
	            }else{
	            	
		            if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
		            	horizontalScrollView.scrollTo(getPixels(buttonWidth) * currentindex, 0);
		            	//content.setVisibility(ViewGroup.VISIBLE);
		            	refresh();
		            }
	            }
	            return false;
	        }
	    });
		
		listview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,long arg3) {
				Playlist playlist = new Playlist(mediaManager.getPlaylistByID(allPlaylist[currentindex]));
				musicManager.setCurrentPlaylist(playlist);
				musicManager.playIndex(position);
				MainActivity ma = (MainActivity) activity;
				ActivityView av = ma.act.get(ma.statusList[4]);
				av.finish();
				av = ma.act.get(ma.statusList[0]);
				ma.init();
				av.display();
				av.setSwipe();
				ma.setClose();
			}
			
		});
		
		final GestureDetector gestureDetector2 = new GestureDetector(activity,new MySwipeUpDetector());
		
		change.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector2.onTouchEvent(event);
			}
		});
		
		final GestureDetector gestureDetector3 = new GestureDetector(activity,new MySwipeDetector());
		
		if(mediaManager.getAllSong().size() > 0){
			newPlay.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					return gestureDetector3.onTouchEvent(event);
				}
			});
			
			newPlay.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(activity, EditPlaylistActivity.class);
					intent.putExtra("ID","new");
					activity.startActivity(intent);
					if(listAdapter.size() > 0)
						listAdapter.get(currentindex).notifyDataSetChanged();
				}
			});
		}
		homeButton.setOnClickListener(open);
	}
	
	private void initPlaylist(){
		
		new Thread() {
	        @Override
	        public void run() {
	    		listAdapter = new ArrayList<SongListAdapter>();
	    		for(String string : allPlaylist){
	    			SongListAdapter s = new SongListAdapter(activity,mediaManager.getPlaylistByID(string),musicManager.getCurrSong());
	    			listAdapter.add(s);
	    		}
	    		
	    		activity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						refresh();
					}
				});
	        }
		}.start();
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		protected MotionEvent mLastOnDownEvent = null;
		protected MotionEvent mLastOnUpEvent = null;
	    @Override
	    public boolean onDown(MotionEvent e) {
	        mLastOnDownEvent = e;
	        return super.onDown(e);
	    }
		
		@Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
	        if (e1==null)
	            e1 = mLastOnDownEvent;
	        
	        if (e2 == null || e1 == null)
	        	return true;
	        
	        
	        if (e1.getX() < e2.getX()) {
	        	if(e2.getX() - e1.getX() > buttonWidth/2){
	        		currentindex = Math.max(0, currentindex-1);
	        	}
	        } else {
	        	if(e1.getX() - e2.getX() > buttonWidth/2){
	        		currentindex = Math.min(length - 1, currentindex + 1);
	        	}
	        }
	        
	        return true;
	    }
		
	}
	
	public void refresh(){
		if(allPlaylist.length > 0){
			listview.setAdapter(listAdapter.get(currentindex));
			playlistText.setText(mediaManager.getPlaylistNameByID(allPlaylist[currentindex]));
			note.setText(listAdapter.get(currentindex).getCount() + " songs");
			if(listAdapter.get(currentindex).getCount() > 0)
				contents.get(currentindex).setVisibility(ViewGroup.VISIBLE);
			else
				contents.get(currentindex).setVisibility(ViewGroup.VISIBLE);		
		}else{
			playlistText.setText("無播放清單");
		}
	}
	
	private void setupLayout(){
		if(contents == null)
			contents = new ArrayList<LinearLayout>();
		else
			contents.clear();
		linearLayout.removeAllViews();
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams((screenWidth - getPixels(buttonWidth))/2, getPixels(buttonHeight));
		ImageView tmpImageView = new ImageView(activity);
		tmpImageView.setLayoutParams(params2);
		linearLayout.addView(tmpImageView);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(buttonWidth), getPixels(buttonHeight));
		params.gravity=Gravity.CENTER;
		for(int i = 0;i < length;i++){
			
			LayoutInflater vi;
	        vi = LayoutInflater.from( activity);
	        View v = vi.inflate(R.layout.playlistcell, linearLayout, false);
			v.setLayoutParams(params);
			
			ImageButton editButton = (ImageButton) v.findViewById(R.id.edit);
			ImageButton playallButton = (ImageButton) v.findViewById(R.id.play);;
			
			editButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(activity, EditPlaylistActivity.class);
					intent.putExtra("ID",allPlaylist[currentindex]);
					activity.startActivity(intent);
				}
			});
			
			playallButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					
					if(listAdapter.get(currentindex).getCount() > 0){
						Playlist playlist = new Playlist(mediaManager.getPlaylistByID(allPlaylist[currentindex]));
						musicManager.setCurrentPlaylist(playlist);
						musicManager.playIndex(0);
						MainActivity ma = (MainActivity) activity;
						ActivityView av = ma.act.get(ma.statusList[4]);
						av.finish();
						av = ma.act.get(ma.statusList[0]);
						ma.init();
						av.display();
						av.setSwipe();
						ma.setClose();
					}
				}
			});
			LinearLayout layout = (LinearLayout) v.findViewById(R.id.content);
			layout.setVisibility(ViewGroup.INVISIBLE);
			contents.add(layout);
			linearLayout.addView(v);
			/*ImageView tmpView = new ImageView(activity);
			tmpView.setImageResource(R.drawable.blank);
			tmpView.setScaleType(ScaleType.CENTER_INSIDE);
			tmpView.setLayoutParams(params);
			linearLayout.addView(tmpView);*/
		}
		
		ImageView tmpImageView2 = new ImageView(activity);
		tmpImageView2.setLayoutParams(params2);
		linearLayout.addView(tmpImageView2);
	}
	
	
    private static final int SWIPE_MIN_DISTANCE = 20;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    class MySwipeDetector extends SimpleOnGestureListener {
    	 

		@Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if ((e2.getY() - e1.getY()) *  (e2.getY() - e1.getY()) + 
                	(e2.getX() - e1.getX()) *  (e2.getX() - e1.getX())> SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
    				Intent intent = new Intent(activity, EditPlaylistActivity.class);
    				intent.putExtra("ID","new");
    				activity.startActivity(intent);
    				if(listAdapter.size() > 0)
    					listAdapter.get(currentindex).notifyDataSetChanged();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }
    private int type = 1;
    
    class MySwipeUpDetector extends SimpleOnGestureListener {

		@Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	//swipe down
                	if(type == 0){
                		change.setImageResource(R.drawable.division_up);
                		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getPixels(25));
                		lp.setMargins(0, getPixels(210), 0, 0);
                		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                		change.setLayoutParams(lp);
                		type = 1;
                	}
                }else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
					//swipe up
                	if(type == 1){
                		
                		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getPixels(25));
                		lp.setMargins(0, getPixels(58), 0, 0);
                		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                		change.setLayoutParams(lp);
                		change.setImageResource(R.drawable.division);
                		type = 0;
                	}
				}
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }
	private void autoSmoothScroll() {

        horizontalScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //hsv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            	horizontalScrollView.scrollTo(getPixels(buttonWidth) * currentindex, 0);
            }
        },100);
    }
	// set current play list collection view
	/*private void setPlaylistView(){
		playlistAdapter = new PlaylistAdapter(mContext);
		listview.setAdapter(playlistAdapter);
		listview.setOnItemClickListener(onItemClickListener);
		button.setSelected(false);
		button.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				createListnameDialog.show();
			}
		});
		
		
	}
	
	private void setEditlistView(final String PlaylistID){
		editlistAdapter = new EditlistAdapter(mContext,PlaylistID);
		listview.setAdapter(editlistAdapter);
		listview.setOnItemClickListener(null);
		button.setSelected(true);
		button.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				editDialog.show(PlaylistID);
			}
		});
		editDialog.setOnSaveButtonListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				
				//setPlaylistView();
				editDialog.dismiss();
				editlistAdapter.notifyDataSetChanged();
			}
		});
		
	}
	
	
	// set play list dialog view
	private void initlistnameDialog(){
		LayoutInflater li = LayoutInflater.from( mContext );
		View promptsView = li.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( mContext );

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);
		final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder.setCancelable(false)
                      .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                String listname = userInput.getText().toString();
                                if( listname.isEmpty() ){
                                    listname = "My playlist";
                                }
                                mediaManager.createPlaylist(listname);
                                playlistAdapter.notifyDataSetChanged();
                            }
                       })
                      .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                        }
                        });
        createListnameDialog = alertDialogBuilder.create();

	}

	
	
	// list adapter for play list collection
	private class PlaylistAdapter extends BaseAdapter{
		private Context mContext;
		private String[] playlistID;
		
		public PlaylistAdapter(Context context){
			mContext = context;
			playlistID = mediaManager.getAllPlaylistID();
			
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return playlistID.length;
		}

		@Override
		public ArrayList<Song> getItem(int position) {
			// TODO Auto-generated method stub
			String id = playlistID[position];
			return mediaManager.getPlaylistByID(id);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public String getItemPlaylistId(int position){
			return playlistID[position];
		}
		
		public void remove(int position){
			mediaManager.removePlaylistById( playlistID[position] );
			notifyDataSetChanged();
		}
		
		@Override
		public void notifyDataSetChanged(){
			playlistID = mediaManager.getAllPlaylistID();
			super.notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			LayoutInflater vi;
			vi = LayoutInflater.from( mContext );
			convertView = vi.inflate(R.layout.playlistlistcell, null);
			
			String playlistName = mediaManager.getPlaylistNameByID( playlistID[position] );
			((TextView)convertView.findViewById(R.id.title)).setText(playlistName);
			
			String size = mediaManager.getPlaylistByID(playlistID[position]).size()+"songs";
			((TextView)convertView.findViewById(R.id.size)).setText(size);
			
			final ImageButton b = ((ImageButton)convertView.findViewById(R.id.checkbutton));
			
			// if this playlist contains zero song, set image button to non-play style
			if( this.getItem(position).size() == 0 ){
				b.setSelected( false );
			}else{
				b.setSelected( true );
			}
			
			b.setOnClickListener( new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if( b.isSelected()){
						// go to play mode
						
					}else{
						// go to edit list
						setEditlistView( getItemPlaylistId(position) );
					}
			
					
				}
			});
			return convertView;
		}
		
		
	}
	
	private class EditlistAdapter extends ArrayAdapter<Song>{
		private Context mContext;
		private ArrayList<Song> playlist;
		private String playlistID;
		public EditlistAdapter(Context context,String playlistID){
			super(context, R.layout.playlisteditcell,
			          R.id.title);
			this.mContext = context;
			this.playlistID = playlistID;
			playlist = mediaManager.getPlaylistByID(playlistID);
			
			
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return playlist.size();
		}

		@Override
		public Song getItem(int position) {
			// TODO Auto-generated method stub
			return playlist.get(position);
		}
		
		public void swapItem(int from,int to){
			Song song = playlist.get(from);
			playlist.remove(from);
			playlist.add(to, song);
			//Collections.swap(playlist,from,to);
			mediaManager.setPlaylist(playlist, playlistID);
			notifyDataSetChanged();
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		@Override
		public void notifyDataSetChanged(){
			playlist = mediaManager.getPlaylistByID(playlistID);
			super.notifyDataSetChanged();
		}

		public void remove(int position){
			playlist.remove(position);
			mediaManager.setPlaylist(playlist, playlistID);
			notifyDataSetChanged();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater vi;
			vi = LayoutInflater.from( mContext );
			convertView = vi.inflate(R.layout.playlisteditcell, null);
			
			((TextView)convertView.findViewById(R.id.title)).setText(playlist.get(position).title);
			((TextView)convertView.findViewById(R.id.artist)).setText(playlist.get(position).gtDuration());
			//((ImageButton)convertView.findViewById(R.id.drag_handle)).setSelected(true);
			
			return convertView;
		}
		
	} */
	
	

	
	/*
	 * Inner class for control drag and drop sorting mechanism  
	 */
	/*private DragSortListView.DropListener onDrop =
        new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
            	if( button.isSelected() ){
            		editlistAdapter.swapItem(from, to);
            		
            	}
            }
        };

    private DragSortListView.RemoveListener onRemove = 
        new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
            	if( !button.isSelected() ){
            		playlistAdapter.remove(which);
            	}else{
            		editlistAdapter.remove(which);
            	}
				
            }
        };

    private DragSortListView.DragScrollProfile ssProfile =
        new DragSortListView.DragScrollProfile() {
            @Override
            public float getSpeed(float w, long t) {
                if (w > 0.8f) {
                    // Traverse all views in a millisecond
                    //return ((float) adapter.getCount()) / 0.001f;
                	return 5.0f * w;
                } else {
                    return 10.0f * w;
                }
            }
       
		};
		
	
	private OnItemClickListener onItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
			//setEditlistView(playlistAdapter.getItemPlaylistId(position));
			/*
			String id = playlistAdapter.getItemPlaylistId(position);
			Playlist playlist = new Playlist( mediaManager.getPlaylistByID(id) );
			musicManager.setCurrentPlaylist(playlist);
			musicManager.playIndex(position);

			
			Intent intent = new Intent(mContext, EditPlaylistActivity.class);
			intent.putExtra("ID",playlistAdapter.getItemPlaylistId(position));
			mContext.startActivity(intent);
			
			
		}
	};*/
	
	
	private void checkAndUpdate(){
		String[] old = allPlaylist;
		String[] newlists = mediaManager.getAllPlaylistID();
		if(newlists.length != old.length){
			if(newlists.length > old.length){
				//Add
				for(int i = newlists.length - 1; i >= 0;i--){
					boolean same = false;
					for(int j = old.length - 1; j >= 0; j--){
						if(newlists[i].equals(old[j])){
							same = true;
							break;
						}
					}
					if(!same){
						allPlaylist = newlists;
						currentindex = i;
						length = allPlaylist.length;
						setupLayout();
					}
				}
			}else{
				//Delete
				for(int i = old.length - 1; i >= 0;i--){
					boolean same = false;
					for(int j = newlists.length - 1; j >= 0; j--){
						if(newlists[j].equals(old[i])){
							same = true;
							break;
						}
					}
					if(!same){
						allPlaylist = newlists;
						currentindex = 0;
						length = allPlaylist.length;
						setupLayout();
					}
				}
			}
		}
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}


	@Override
	public void resume() {	
		if(listAdapter != null){
			checkAndUpdate();
			listAdapter.clear();
			for(String string : allPlaylist){
				SongListAdapter s = new SongListAdapter(activity,mediaManager.getPlaylistByID(string),musicManager.getCurrSong());
				listAdapter.add(s);
			}
			if(listAdapter.size() > 0)
				listview.setAdapter(listAdapter.get(currentindex));
			refresh();
			autoSmoothScroll();
		}

	}
		
}
