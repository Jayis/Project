package com.sagax.player;

import java.util.ArrayList;

import com.sagax.player.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.LinearLayout;


public class ListViewerActivity extends Activity {
	int i = 0 ;
	private ArrayList<String> list;
	private DragSortListView lv;
	private final Context mContext = this;
	private MediaManager mediaManager;
	private ArrayAdapter<String> adapter;

	 
	private DragSortListView.DropListener onDrop =
        new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                String item=adapter.getItem(from);
                adapter.notifyDataSetChanged();
                adapter.remove(item);
                adapter.insert(item, to);
            }
        };

    private DragSortListView.RemoveListener onRemove = 
        new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                adapter.remove(adapter.getItem(which));
            }
        };

    private DragSortListView.DragScrollProfile ssProfile =
        new DragSortListView.DragScrollProfile() {
            @Override
            public float getSpeed(float w, long t) {
                if (w > 0.8f) {
                    // Traverse all views in a millisecond
                    return ((float) adapter.getCount()) / 0.001f;
                } else {
                    return 10.0f * w;
                }
            }
       
		};


	private OnItemClickListener listener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent,View view,int position,long id){
			if( i == 0 ){
				String[] albumIDs = mediaManager.getAllAlbumID();
				ArrayList<Song> songs = mediaManager.getSongsByAlbumID( albumIDs[position] );
				String[] name = new String[ songs.size() ];
				for(int index=0; index< name.length ; index++ ){
					name[index] = songs.get(index).title;
				}
				lv.setAdapter( new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1,name) );
			
			}
			if( i == 1 ){
			
				String[] artists = mediaManager.getAllArtistID();
				ArrayList<Song> songs = mediaManager.getSongsByArtistID( artists[position] );
				String[] name = new String[ songs.size() ];
				for(int index=0; index< name.length ; index++ ){
					name[index] = songs.get(index).title;
				}
				lv.setAdapter( new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1,name) );
			
			}
			/*
			String key = mediaManager.getAllArtistName()[position];
			ArrayList<Song> songs = mediaManager.getSongsByArtist( key );
			String[] name = new String[ songs.size() ];
			for(int i=0; i< name.length ; i++ ){
				name[i] = songs.get(i).title;
			}
			lv.setAdapter( new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1,name) );
			*/
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation( 1 );
		mediaManager  = new MediaManager( getApplicationContext() );
		final TextView tv = new TextView( this );
		Button bt = new Button( this );

		bt.setOnClickListener( new OnClickListener(){
			public void onClick(View v){
				i = ( i + 1 ) % 3;

				ArrayList<String> cells = new ArrayList<String>();
				switch( i ){
					case 0:
						tv.setText( " Albums "+ String.valueOf( mediaManager.getAllAlbumID().length ) );
						for(String albumId : mediaManager.getAllAlbumID() ){
							cells.add( mediaManager.getAlbumNameByID( albumId ) );
						}
						break;
					case 1:
						tv.setText( " Artists " + String.valueOf( mediaManager.getAllArtistID().length) );
						for(String ID : mediaManager.getAllArtistID() ){
							cells.add( mediaManager.getArtistNameByID( ID ) );
						}
						break;
					case 2:
						tv.setText( " Songs " + String.valueOf( mediaManager.getAllSongID().length) );
						for(String ID : mediaManager.getAllSongID() ){
							cells.add( mediaManager.getSongNameByID( ID ) );
						}
						break;
				
				}

				lv.setAdapter( new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1, cells ));

			}
		
		});

		
		LayoutInflater layoutInflater = getLayoutInflater();

		// in listviewer.xml 
		View view = layoutInflater.inflate( R.layout.listviewer , null );
		lv = ( DragSortListView ) view.findViewById( R.id.sortablelist );
		
		tv.setText( " Albums "+ String.valueOf( mediaManager.getAllAlbumID().length ) );
		ArrayList<String> albumNames = new ArrayList<String>();
		for(String albumId : mediaManager.getAllAlbumID() ){
			albumNames.add( mediaManager.getAlbumNameByID( albumId ) );
		}
	
		
		adapter = new ArrayAdapter<String>(this, R.layout.list_item_handle_right,R.id.text, albumNames );
		lv.setAdapter( adapter );	
		lv.setDropListener(onDrop);
		lv.setRemoveListener(onRemove);
		lv.setDragScrollProfile(ssProfile);
		lv.setOnItemClickListener( listener );

	


		layout.addView( tv );
		layout.addView( bt );
		layout.addView( lv );

		setContentView( layout );

	}
	public void onResume(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
   
	
}
