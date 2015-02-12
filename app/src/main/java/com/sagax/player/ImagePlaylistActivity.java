package com.sagax.player;

import com.sagax.player.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;



public class ImagePlaylistActivity extends Activity {
	
	private final int TITLE_ALL = 0 , TITLE_ALBUM = 1 ,TITLE_SINGER = 2 , TITLE_TYPE = 3;	
	private ImagePlaylistView listview;
	private MediaManager mediaManager;
	private MusicManager musicManager;
	private OnItemClickListener itemClickListener;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		
		Intent intent = this.getIntent();
		String id = String.valueOf( intent.getStringExtra("ID") );
		int type = intent.getIntExtra("TYPE", -1);
		String title = intent.getStringExtra("TITLE");
		String subtitle = intent.getStringExtra("SUBTITILE");
		
		// init media manager
		mediaManager = MainActivity.getMediaManagerInstance();
		musicManager = MainActivity.getMusicManagerInstance();
		final Playlist playlist;
		
		if(type == TITLE_TYPE ){
			playlist = new Playlist( mediaManager.getSongsByTypeID(id) );
			listview = new ImagePlaylistView(this,title,playlist.getSongList().size()+"songs",playlist);
				
			
		}else{
			playlist = new Playlist( mediaManager.getSongsByAlbumID(id) );
			listview = new ImagePlaylistView(this,title,subtitle,playlist);
			listview.setCoverView( mediaManager.getAlbumArtByID(id) );
		}
	
		itemClickListener = new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,long arg3) {
				musicManager.setCurrentPlaylist(playlist);
				musicManager.playIndex(position);
				
			}
			
		};
		
		
		listview.setOnItemClickListener( itemClickListener );
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		setContentView(listview);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title);	
		((TextView)findViewById(R.id.text)).setText(title);
		
	}
	public void onResume(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	
	
	
}