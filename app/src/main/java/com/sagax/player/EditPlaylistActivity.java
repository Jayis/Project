package com.sagax.player;

import java.util.ArrayList;

import com.sagax.player.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class EditPlaylistActivity extends Activity {
	private MediaManager mediaManager;
	private MusicManager musicManager;
	
	private EditText playlistname = null;
	private ImageButton saveButton = null;
	private ImageButton cancelButton = null;
	private DragSortListView listview;
	/*private MusiclistDialog editDialog;
	private ImageButton addSongButton , cancelButton , playButton , editButton ;
	private ImageButton buttons[] ; 
	private View toolbarView;
	private DragSortListView listview;*/
	//private EditlistAdapter editlistAdapter;
	private EditlistAdapter editlistAdapter;
	private OnClickListener toolbarListener;
	private Context mContext;
	private String id;
	private ArrayList<Song> playlist;
	private Button addButton = null;
	private boolean newFlag = false;
	private Button deleteButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		Intent intent = this.getIntent();
		id = intent.getStringExtra("ID");
		mediaManager = MainActivity.getMediaManagerInstance();
		musicManager = MainActivity.getMusicManagerInstance();
		mContext = this;
		
		setContentView(R.layout.playlistedit);
		
		setupUI();
		if(id.equals("new")){
			newFlag = true;
		}
		setupContent(newFlag);
		setupMiniPlayer();
		/*cancelButton = (ImageButton)findViewById(R.id.menu);
		playButton = (ImageButton)findViewById(R.id.play);
		editButton = (ImageButton)findViewById(R.id.edit);
		addSongButton = (ImageButton) findViewById(R.id.create);
		
		buttons = new ImageButton[] { cancelButton ,playButton , editButton };
			
		listview = (DragSortListView)findViewById(R.id.dragsortlistview);
		listview.setDropListener(onDrop);
		listview.setRemoveListener(onRemove);
		listview.setDragScrollProfile(ssProfile);

		toolbarView = (View)findViewById(R.id.toolbar);
		toolbarView.setVisibility(View.GONE);

	
		
		editDialog = new MusiclistDialog(mContext,mediaManager);	
		setEditlistView(id);*/
		
	}

	
	private void setupUI(){
		playlistname = (EditText)findViewById(R.id.textView1);
		saveButton = (ImageButton)findViewById(R.id.imageButton1);
		cancelButton = (ImageButton)findViewById(R.id.imageButton2);
		listview = (DragSortListView)findViewById(R.id.dragsortlistview);
		addButton = (Button)findViewById(R.id.button1);
		deleteButton = (Button)findViewById(R.id.delete);
	}
	
	private void setupContent(boolean newplay){
		
		if(newplay){
			playlistname.setText("");
			playlistname.setFocusable(true);
			id = String.valueOf(mediaManager.createPlaylist(playlistname.getText().toString()));
			playlist = new ArrayList<Song>();
		}
		else{
			playlistname.setText(mediaManager.getPlaylistNameByID(id));
			editlistAdapter = new EditlistAdapter(mContext,id);
			listview.setAdapter(editlistAdapter);
			listview.setOnItemClickListener(null);
			playlist = mediaManager.getPlaylistByID(id);
		}
		
		deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mediaManager.removePlaylistById(id);
				finish();
			}
		});
		
		addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, AddPlaylistActivity.class);
				intent.putExtra("ID",id);
				mContext.startActivity(intent);
			}
		});
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(playlist != null)
					mediaManager.setPlaylist(playlist, id, playlistname.getText().toString());
				finish();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if(newFlag)
					mediaManager.removePlaylistById(id);
				finish();
			}
		});
		
		
		listview.setDropListener(onDrop);
		listview.setRemoveListener(onRemove);
		listview.setDragScrollProfile(ssProfile);
	}
	
	private class EditlistAdapter extends ArrayAdapter<Song>{
		private Context mContext;
		
		private String playlistID;
		public EditlistAdapter(Context context,String playlistID){
			super(context, R.layout.playlisteditcell);
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
			
			((TextView)convertView.findViewById(R.id.song)).setText(playlist.get(position).title);
			
			return convertView;
		}
			
	}
	
	protected void onResume() {
		super.onResume();
		
		if(playlist != null){
			editlistAdapter = new EditlistAdapter(mContext, id);
			listview.setAdapter(editlistAdapter);
		}
		
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
		
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
        if (musicManager.getCurrSong() != null) {
            if (mminiSong != null)
                mminiSong.setText(musicManager.getCurrSong().filename);

            if (musicManager.isPlaying()) {
                mplay.setImageResource(R.drawable.mini_pause);
                if (notification2 != null) {
                    handler2.removeCallbacks(notification2);
                }
                notification2 = new Runnable() {
                    public void run() {
                        mini_refresh();
                    }
                };
                handler2.postDelayed(notification2, 100);
            } else {
                mplay.setImageResource(R.drawable.mini_play);
            }
        }
	}
	
	private DragSortListView.DropListener onDrop =
	        new DragSortListView.DropListener() {
	            @Override
	            public void drop(int from, int to) {
	           		editlistAdapter.swapItem(from, to);
	            }
	        };

	    private DragSortListView.RemoveListener onRemove = 
	        new DragSortListView.RemoveListener() {
	            @Override
	            public void remove(int which) {
	            	editlistAdapter.remove(which);
	            	
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
	/*private void setEditlistView(final String PlaylistID){
		editlistAdapter = new EditlistAdapter(mContext,PlaylistID);
		listview.setAdapter(editlistAdapter);
		listview.setOnItemClickListener(null);
		
		addSongButton.setSelected(true);
		addSongButton.setOnClickListener( new OnClickListener(){
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
	
		
		// check if this list is empty
		if( editlistAdapter.getCount() == 0 ){
			editMode();
		}else{
			viewMode();
		}
		
		
		
		
		toolbarListener = new OnClickListener(){
			@Override
			public void onClick(View v) {
				switch(v.getId()){
					case R.id.menu:
						// cancel button
						if( addSongButton.getVisibility() == View.GONE){
							// this is view mode 
							// if touch this, it will finish this activity
							finish();
						}else{
							// it's in editing mode
							// hit this button to go back to view mode
							cancelButton.setImageResource(android.R.drawable.ic_menu_sort_by_size);
							viewMode();
						}
						
						break;
					case R.id.edit:
						cancelButton.setImageResource(R.drawable.ok_btn);
						cancelButton.setAdjustViewBounds(true);
						
						
						editMode();
						break;
					case R.id.play:
						// under construction
						Playlist playlist = new Playlist( mediaManager.getPlaylistByID(PlaylistID) );
						musicManager.setCurrentPlaylist(playlist);
						musicManager.playIndex(0);
						
						break;
					case R.id.query:
						// under construction
						break;
				}
			}
		};
		
		for(ImageButton button : buttons){
			button.setOnClickListener( toolbarListener);
		}
		
		
	}
	
	private void editMode(){
		toolbarView.setVisibility(View.GONE);
		addSongButton.setVisibility(View.VISIBLE);
		listview.setDragEnabled(true);
		
	}
	
	private void viewMode(){
		toolbarView.setVisibility(View.VISIBLE);
		addSongButton.setVisibility(View.GONE);
		listview.setDragEnabled(false);
		
		listview.setOnItemClickListener( new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				//setEditlistView(playlistAdapter.getItemPlaylistId(position));
			
				Playlist playlist = new Playlist( mediaManager.getPlaylistByID(id) );
				musicManager.setCurrentPlaylist(playlist);
				musicManager.playIndex(position);
			
			}
		});
		
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
			((TextView)convertView.findViewById(R.id.artist)).setText(playlist.get(position).artist);
			((TextView)convertView.findViewById(R.id.duration)).setText(playlist.get(position).gtDuration());
			// if it's on editing mode ( which add song button is enabled )
			// change the list view to drag enabled mode
			
			if ( addSongButton.getVisibility() == View.VISIBLE ){
				((ImageView)convertView.findViewById(R.id.drag_handle)).setVisibility(View.VISIBLE);
				((TextView)convertView.findViewById(R.id.duration)).setVisibility(View.INVISIBLE);
			}else{
				((ImageView)convertView.findViewById(R.id.drag_handle)).setVisibility(View.INVISIBLE);
				((TextView)convertView.findViewById(R.id.duration)).setVisibility(View.VISIBLE);	
			}
			//
			
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
           		editlistAdapter.swapItem(from, to);
            }
        };

    private DragSortListView.RemoveListener onRemove = 
        new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
            	editlistAdapter.remove(which);
            	
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
		
	*/
}
