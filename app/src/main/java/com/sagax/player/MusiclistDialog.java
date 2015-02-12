package com.sagax.player;

import java.util.ArrayList;

import com.sagax.player.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class MusiclistDialog extends Dialog{
	private MediaManager mediaManager;
	private ImageButton save,exit;
	private ListView listview;
	private android.view.View.OnClickListener buttonListener;
	private SongAdapter adapter;
	private ArrayList<Song> customlist;
	private String playlistID;
	
	public MusiclistDialog(Context context,MediaManager manager) {
		super(context,android.R.style.Theme_Light_NoTitleBar);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mediaManager = manager;
		
		adapter = new SongAdapter(context,mediaManager.getPlaylist().getSongList());
		
		setContentView(R.layout.musiclistdialog);
		
		save = (ImageButton)findViewById(R.id.save);
		exit = (ImageButton)findViewById(R.id.exit);
		listview = (ListView)findViewById(R.id.listview);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(itemClickListener);
		
		buttonListener = new android.view.View.OnClickListener(){
			@Override
			public void onClick(View v) {
				switch( v.getId() ){
					case R.id.save:
						
						break;
					case R.id.exit:
						
						break;
				}
				// whenever these 2 buttons been clicked
				// dismiss this dialog
				dismiss();
			}

		};
		
		save.setOnClickListener( buttonListener );
		exit.setOnClickListener( buttonListener );

	}
	
	public ArrayList<Song> getSelectedList(){
		return adapter.getSelectedList();
	}
	
	public void setOnSaveButtonListener(android.view.View.OnClickListener listener){
		save.setOnClickListener( listener );
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
			ImageButton b = ((ImageButton)v.findViewById(R.id.checkbutton));
			
			adapter.setSelected(position, !adapter.istSelected(position) );
			b.setSelected( adapter.istSelected(position));
			
			
		}

		
	};
	
	public void show(String playlistID){
		this.playlistID = playlistID;
		ArrayList<Song> checkList = mediaManager.getPlaylistByID(playlistID);
		adapter.checkSelected( checkList );
		super.show();
	}
	
	@Override
	public void dismiss(){
		ArrayList<Song> list = new ArrayList<Song>();
		list.addAll(getSelectedList());
		mediaManager.setPlaylist(list , playlistID );
		adapter.reset();
		adapter.notifyDataSetChanged();
		super.dismiss();
	}
	
	private class SongAdapter extends BaseAdapter{
		private ArrayList<Song> list;
		private Context mContext;
		private boolean selectList[];
		
		public SongAdapter(Context context,ArrayList<Song> list){
			this.mContext = context;
			this.list = list;
			this.selectList = new boolean[list.size()];
			
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Song getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public void setSelected(int position,boolean selection){
			this.selectList[position]=selection;
		}
		
		
		// check if song is in the current playlist 
		public void checkSelected(ArrayList<Song> existList){
			for(Song song : existList){
				for(int i=0; i<list.size(); i++){
					if( song.id.equals( list.get(i).id ) ){
						this.setSelected(i, true);
					}
				}
			}
			notifyDataSetChanged();
		}
		
		public boolean istSelected(int position){
			return this.selectList[position];
		}
		
		public void reset(){
			for(int i=0; i<selectList.length ; i++){
				selectList[i] = false;
			}
		}
		
		public ArrayList<Song> getSelectedList(){
			ArrayList<Song> selected = new ArrayList<Song>();
			// traverse all boolean array and add song to list if it's selected
			for(int i=0;i<list.size();i++){
				if( selectList[i] ){
					selected.add(list.get(i));
				}
			}
			return selected; 
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater vi;
		    vi = LayoutInflater.from( mContext );
		    convertView = vi.inflate(R.layout.editlistcell, null);
    		((TextView)convertView.findViewById(R.id.title)).setText(list.get(position).title);
			((TextView)convertView.findViewById(R.id.artist)).setText("unknown");
			ImageButton b = ((ImageButton)convertView.findViewById(R.id.checkbutton));
			b.setSelected( istSelected(position) );
			
			return convertView;
		}
		
		
		
	} 
	
	
	
}