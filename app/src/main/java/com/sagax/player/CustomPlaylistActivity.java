package com.sagax.player;

import java.util.ArrayList;
import java.util.Collections;

import com.sagax.player.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class CustomPlaylistActivity extends Activity {
	private MediaManager mediaManager;
	private MusiclistDialog editDialog;
	private ImageButton button;
	private DragSortListView listview;
	private PlaylistAdapter playlistAdapter;
	private EditlistAdapter editlistAdapter;
	private Context mContext;
	private AlertDialog createListnameDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//mediaManager = new MediaManager(this);
		mediaManager = MainActivity.getMediaManagerInstance();
		mContext = this;
		setContentView(R.layout.customplaylistactivity);
		button = (ImageButton) findViewById(R.id.create);
		
		// init create list dialog
		initlistnameDialog();
		
		listview = (DragSortListView)findViewById(R.id.dragsortlistview);
		listview.setDropListener(onDrop);
		listview.setRemoveListener(onRemove);
		listview.setDragScrollProfile(ssProfile);

		View view = (View)findViewById(R.id.toolbar);
		view.setVisibility(View.GONE);

		
		editDialog = new MusiclistDialog(mContext,mediaManager);	
		setPlaylistView();
		
			  
		
		
		
		
	}
	public void onResume(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}

	private void setPlaylistView(){
		playlistAdapter = new PlaylistAdapter(this);
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
		editlistAdapter = new EditlistAdapter(this,PlaylistID);
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
	
	private void initlistnameDialog(){
		LayoutInflater li = LayoutInflater.from( this );
		View promptsView = li.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( this );

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
		
	} 
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.custom_playlist, menu);
		return true;
	}

	
	/*
	 * Inner class for control drag and drop sorting mechanism  
	 */
	private DragSortListView.DropListener onDrop =
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
			setEditlistView(playlistAdapter.getItemPlaylistId(position));
			
		}
		
		
	};
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // Show home screen when pressing "back" button,
            //  so that this app won't be closed accidentally
        	
        	if( button.isSelected() ){
        		
        		setPlaylistView();
        		
        	}else{
        		finish();
        	}
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
		
	
}
