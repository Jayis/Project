package com.sagax.player;

import java.util.ArrayList;

import com.sagax.player.R;

import android.content.Context;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SongListAdapter extends BaseAdapter{
	private ArrayList<Song> songs;
	private Context mContext;
	private String currentIdString;
	
	public SongListAdapter(Context context,ArrayList<Song> playlist, Song current) {
		mContext = context;
		songs = playlist;
		if(current != null)
			currentIdString = current.id;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View v = convertView;
		// init the view
		if( v == null){
			LayoutInflater vi;
	        vi = LayoutInflater.from( mContext);
	        v = vi.inflate(R.layout.songlist, parent, false);
		}  
	        // set song title to list content
		TextView tmp = ((TextView)v.findViewById(R.id.songtitle));
		TextView songLength = ((TextView)v.findViewById(R.id.songlength));
		tmp.setText(songs.get(position).title);
		tmp.setTextColor(Color.rgb(255, 235, 0));
		songLength.setText(songs.get(position).gtDuration());
		songLength.setTextColor(Color.rgb(255, 235, 0));
		if(!currentIdString.equals(songs.get(position).id)){
			((ImageView)v.findViewById(R.id.current)).setVisibility(ViewGroup.INVISIBLE);
			tmp.setTextColor(Color.rgb(255, 255, 255));
			songLength.setTextColor(Color.rgb(255, 255, 255));
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