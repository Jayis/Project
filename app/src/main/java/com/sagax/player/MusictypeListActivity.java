package com.sagax.player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;

import java.util.ArrayList;

import com.sagax.player.R;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class MusictypeListActivity extends Activity {

	private ImagePlaylistView playlistView;
	private ListView listview;
	private MediaManager mediaManager;
	private ImageListAdapter adapter;
	private OnItemClickListener itemClickListener;
	
	@SuppressLint({ "NewApi", "InlinedApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mediaManager = new MediaManager(this);
		
		String ID = mediaManager.getAllAlbumID()[0];
		String name = mediaManager.getAlbumNameByID(ID);
		Playlist playlist = new Playlist( mediaManager.getSongsByAlbumID(ID) );
		
		
		playlistView = new ImagePlaylistView(this,name,playlist.getSongList().size()+"songs",playlist);
		
		
		setContentView(R.layout.activityalbumlist);
		
		
		listview = (ListView)findViewById(R.id.listview);
		
		adapter = new ImageListAdapter(this);
		listview.setAdapter(adapter);
	
		
		addContentView(playlistView,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT) );
		playlistView.setVisibility(View.GONE);
		
		
		itemClickListener = new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view , int position , long id ){
				Playlist playlist = adapter.getItem(position);
				playlistView.setImagePlaylistView( adapter.getItemTitle(position), adapter.getItemSubTitle(position), playlist);
				playlistView.setVisibility(View.VISIBLE);
				listview.setVisibility(View.GONE);
			}
		};
		
		listview.setOnItemClickListener(itemClickListener);
		
		
	}
	public void onResume(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // Show home screen when pressing "back" button,
            //  so that this app won't be closed accidentally
        	if(playlistView.getVisibility() == View.VISIBLE){
        		playlistView.setVisibility(View.GONE);
        		listview.setVisibility(View.VISIBLE);
        		
        	}else{
        		finish();
        	}
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.album_list, menu);
		return true;
	}
	
	
	private class ImageListAdapter extends BaseAdapter{
		
		private Context mContext;
		private String[] albumID ;
		
		public ImageListAdapter(Context context){
			this.mContext = context;
			albumID = mediaManager.getAllAlbumID();
			
		}
		
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return albumID.length;
		}

		@Override
		public Playlist getItem(int position) {
			ArrayList<Song> songs = mediaManager.getSongsByAlbumID(albumID[position]);
			Playlist playlist = new Playlist(songs);
			return playlist;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.valueOf(albumID[position]);
		}
		
		public String getItemTitle(int position){
			String id = albumID[position];
			return mediaManager.getAlbumNameByID(id);
		}
		
		public String getItemSubTitle(int position){
			String id = albumID[position];
			return mediaManager.getAlbumArtistByID(id);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

		
			LayoutInflater vi;
		    vi = LayoutInflater.from( mContext );
		    v = vi.inflate(R.layout.imagelistcell, null);
    
		    String id = albumID[position];
		    // set song title to list content
			((TextView)v.findViewById(R.id.title)).setText(mediaManager.getAlbumNameByID(id));
			
			if( mediaManager.getAlbumArtistByID(id) == null ){
				((TextView)v.findViewById(R.id.subtitle)).setText("unknown");	
			}else{
				((TextView)v.findViewById(R.id.subtitle)).setText(mediaManager.getAlbumArtistByID(id));
			}
			return v;
		}
	
	}

}
