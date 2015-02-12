package com.sagax.player;

import java.util.ArrayList;

import com.sagax.player.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class MusicListActivity extends Activity {
	public static final int requestCode = 444;
	private Intent intent; 
	private Context mContext;
	// all buttons in this xml file
	private Button[] buttons; 
	private PlaylistView playlistView;
	private MediaManager mediaManager;
	
	// set all buttons' action event 
	private OnClickListener buttonListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		intent = getIntent();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musiclistactivity);
		// init media manager 
		mediaManager = new MediaManager( this );
		mContext = this;
		// init all button from xml 
		buttons = new Button[]{
			// artist button
			(Button)findViewById(R.id.button1),
			// album button
			(Button)findViewById(R.id.button2),
			// playlist button
			(Button)findViewById(R.id.button3),
		};
		
		buttonListener = new OnClickListener(){
			public void onClick(View v){
				
				ArrayList<String> content = new ArrayList<String>();
				// when a button gets clicked, it go through its action event
				switch( v.getId() ){
					case R.id.button1:
						for(String id : mediaManager.getAllArtistID()){
							content.add( mediaManager.getArtistNameByID(id));
						}			
						playlistView.setHeaderTitle("Artists");
						playlistView.setOnItemClickListener(artistClickListener);
						break;
					case R.id.button2:
						for(String id : mediaManager.getAllAlbumID()){
							content.add( mediaManager.getAlbumNameByID(id));
						}
						Toast.makeText( mContext , "size"+content.size() , Toast.LENGTH_SHORT).show();
						playlistView.setHeaderTitle("Albums");
						playlistView.setOnItemClickListener(albumClickListener);
						break;
					case R.id.button3:
						for(String id : mediaManager.getAllPlaylistID()){
							content.add( mediaManager.getPlaylistNameByID(id));
						}
						playlistView.setHeaderTitle("Playlists");
						playlistView.setOnItemClickListener(playlistClickListener);
						break;
				}
				playlistView.setCollectionList(content);
				playlistView.setHeaderStyle(PlaylistView.HEADER_STYLE_SIMPLE);
				playlistView.setVisibility(View.VISIBLE);
			}
			
		};
		
		// set button a listener
		for(Button bt : buttons){
			bt.setOnClickListener(buttonListener);
		}
		
		//create a null playlistview
		playlistView = new PlaylistView(getApplicationContext(),new ArrayList<String>(),"");
		addContentView(playlistView,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT) );
		playlistView.setVisibility(View.GONE);
	
		
		
		
	}
	public void onResume(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.music_list, menu);
		return true;
	}
	

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // Show home screen when pressing "back" button,
            //  so that this app won't be closed accidentally
        	if(playlistView.getVisibility() == View.VISIBLE){
        		playlistView.setVisibility(View.GONE);
        	}else{
        		finish();
        	}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	
	private void createListView(ArrayList<String> list){
		
		
	} 
	
	
	private OnItemClickListener playClickListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View view , int position , long id ){
			Bundle bundle = new Bundle();
			bundle.putInt("position", position);
			ArrayList<String> ids = new ArrayList<String>();
			for(Song song : playlistView.getCurrentPlaylist().getSongList() ){
				ids.add(song.id);
			}
			bundle.putStringArrayList("ids", ids);
			intent.putExtras(bundle);
			setResult(MusicListActivity.requestCode, intent);
			finish();
		}
	};
	
	private OnItemClickListener albumClickListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View view , int position , long id ){
			ArrayList<Song> songs = mediaManager.getSongsByAlbumID( mediaManager.getAllAlbumID()[position] );
			Playlist playlist = new Playlist(songs);
			playlistView.setCurrentPlaylist(playlist);
			
			playlistView.setOnItemClickListener(playClickListener);
		}
	};
	private OnItemClickListener artistClickListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View view , int position , long id ){
			ArrayList<Song> songs = mediaManager.getSongsByArtistID( mediaManager.getAllArtistID()[position] );
			Playlist playlist = new Playlist(songs);
			playlistView.setCurrentPlaylist(playlist);
			playlistView.setOnItemClickListener(playClickListener);
		}
	};
	private OnItemClickListener playlistClickListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View view , int position , long id ){
			ArrayList<Song> songs = mediaManager.getPlaylistByID( mediaManager.getAllPlaylistID()[position] );
			Playlist playlist = new Playlist(songs);
			playlistView.setCurrentPlaylist(playlist);
			playlistView.setOnItemClickListener(playClickListener);
		}
	};
	
	
	
}
