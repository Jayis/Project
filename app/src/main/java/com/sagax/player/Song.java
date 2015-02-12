package com.sagax.player;


import java.io.Serializable;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class Song implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6302415532980089323L;
	public String id;
	public String artist;
	public String album;
	public String title;
	public String data;
	public long album_id;
	public String filename;
	public int duration;
	public Uri albumPath;
	public Song(String title){
		this("-1","-1","-1",title,"-1","-1",-1);
	}
	public Song(String id,String artist,String album,String title,String data,String album_id,int duration) {
		this.id = id;
		this.album = album;
		this.title = title;
		this.data = data;
		this.artist = artist;
		this.album_id =  (long) Integer.parseInt(album_id);
		this.duration = duration;
		
		String[] token = data.split("/");
		filename = token[token.length-1];
		
		Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		albumPath = ContentUris.withAppendedId(sArtworkUri, this.album_id);
	}
	@SuppressLint("DefaultLocale")
	public String gtDuration(){
		int d = duration/1000;
		int h = d/3600;
		int m = (d%3600)/60;
		int s = d%60;
		if(h == 0)
			return String.format("%02d:%02d", m,s);
		else {
			return String.format("%d:%02d:%02d", h,m,s);
		}
	}
	
	
}
