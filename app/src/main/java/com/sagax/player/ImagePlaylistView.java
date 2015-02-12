package com.sagax.player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.sagax.player.R;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class ImagePlaylistView extends LinearLayout{

	private Context mContext;
	private Playlist currentPlaylist;
	private ListView playlistView;
	private TextView footerView,mainTitleView,subTitleView;
	private String mainTitle,secTitle;
	private ImageView coverView;
	private ImageButton editButton,searchButton,playButton;
	private SongListAdapter adapter;
	
	public ImagePlaylistView(Context context,String mainTitle,String secTitle,Playlist playlist) {
		super(context);
		this.mContext = context;
		this.currentPlaylist = playlist;
		this.mainTitle = mainTitle;
		this.secTitle = secTitle;
		initView();
	}
	
	
	public void setImagePlaylistView(String mainTitle,String secTitle,Playlist playlist){
		this.currentPlaylist = playlist;
		this.mainTitle = mainTitle;
		this.secTitle = secTitle;
		adapter = new SongListAdapter(mContext,currentPlaylist);
		playlistView.setAdapter(adapter);
		mainTitleView.setText(mainTitle);
		subTitleView.setText(secTitle);
		footerView.setText(playlist.getSongList().size()+"songs");
	}
	
	
	// private method for initialize the view 
	// depends on different configuration
	private void initView(){
		/*
		 * initialize the list view
		 */
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate( R.layout.imagelistview , null );
		playlistView = (ListView) view.findViewById( R.id.listView );
		coverView = (ImageView) view.findViewById(R.id.avatar);
		setOrientation( 1 );	

		
		adapter = new SongListAdapter(mContext,currentPlaylist);
		playlistView.setAdapter(adapter);
		
		// add footer to list view 
		// footer is only a simple textview
		footerView = (TextView) view.findViewById( R.id.footer );
		footerView.setTextColor( mContext.getResources().getColor(android.R.color.black));
		footerView.setText(secTitle);
		
		
		
		// initialize header view
		// get default header
		// get headerview from listheaderview.xml
		mainTitleView = ((TextView)view.findViewById( R.id.mainTitleText ));
		mainTitleView.setText(mainTitle);
		subTitleView = ((TextView)view.findViewById( R.id.secondTitleText ));
		subTitleView.setText(secTitle);
		
		
		editButton = (ImageButton)view.findViewById( R.id.editbutton);
		editButton.setOnClickListener( new OnClickListener(){
			// toggle the edit mode
			public void onClick(View v){
				
			}
		});

		
		addView( view );
		
		
		//setBackgroundResource( R.drawable.back );
		
		
	}
	
	// set the playlist a onitem listener
	public void setOnItemClickListener(OnItemClickListener listener){
		playlistView.setOnItemClickListener(listener);	
	}
	
	public void setCoverView(Uri uri){
		ContentResolver res = this.mContext.getContentResolver();
		Bitmap bitmap = null;
		if (uri != null) {
			ParcelFileDescriptor fd = null;
	        try {
	        	fd = res.openFileDescriptor(uri, "r");
	        	bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
	        	coverView.setImageBitmap(bitmap);
	        	coverView.setScaleType(ImageView.ScaleType.FIT_XY);
	        	coverView.setAdjustViewBounds(true);
	        	coverView.setMaxHeight(190);
	        	coverView.setMaxWidth(190);
	        	
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
	}
	
	private class SongListAdapter extends BaseAdapter{
		private ArrayList<Song> songs;
		private Context mContext;
		
		public SongListAdapter(Context context,Playlist playlist) {
			mContext = context;
			songs = playlist.getSongList();
			
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View v = convertView;
			// init the view
			if( v == null){
				LayoutInflater vi;
		        vi = LayoutInflater.from( mContext );
		        v = vi.inflate(R.layout.regular_list, null);
		        
		        // set song title to list content
				((TextView)v.findViewById(R.id.songtitle)).setText(songs.get(position).title);
				((TextView)v.findViewById(R.id.artist)).setText(songs.get(position).artist);
				((TextView)v.findViewById(R.id.songlength)).setText(songs.get(position).gtDuration());
			}
			
			return v;
		}



		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return songs.size();
		}



		@Override
		public Song getItem(int position) {
			// TODO Auto-generated method stub
			return songs.get(position);
		}



		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		
	}
	

}