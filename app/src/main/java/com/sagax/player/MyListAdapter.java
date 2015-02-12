package com.sagax.player;

import java.io.File;

import com.sagax.player.R;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MyListAdapter extends ArrayAdapter<String>{
	private Context mContext;
	private String[] albumIds;
	public MyListAdapter(Context context, int layout,String[] albumIds){
		super(context,layout,albumIds);
		this.mContext=context;
		this.albumIds=albumIds;
	}
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		View v;
		ImageView iv;
		
		if (convertView != null)
	        v = convertView;
	    else {
	        LayoutInflater layout = LayoutInflater.from(mContext);
	        v = layout.inflate(R.layout.custom_grid_item, null);
	    }
		
		String albumId=getItem(position);
		iv = (ImageView) v.findViewById(R.id.album_image);
		
		Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		Uri albumPath = ContentUris.withAppendedId(sArtworkUri,Long.valueOf(albumId));
		
	    File file= new File(albumPath.toString());
	    
	    if(!file.exists())
	    	iv.setImageResource(R.raw.unknown);
	    else
	    	iv.setImageURI(albumPath);

	    iv.setScaleType(ScaleType.FIT_CENTER);
		
		return v;
	}
	
}

