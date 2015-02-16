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
    public String genre;
	public long album_id;
	public String filename;
	public int duration;
	public Uri albumPath;
    public int status;
    public String url;
    public int eqon;

	public Song(String title){
		this("-1","null","null",title,"null","-1",-1, "null", "null", "null", -1);
	}
	public Song(String id,String artist,String album,String title,String data,String album_id,int duration, String genre, String url, String filename, Integer eqon) {
		this.id = id;
		this.album = album;
		this.data = data;
		this.artist = artist;
		this.album_id =  (long) Integer.parseInt(album_id);
		this.duration = duration;
        this.genre = genre;
        this.url = url;

        if (eqon == 1) {
            this.eqon = 1;
        }
        else {
            this.eqon = 0;
        }
		
		//String[] token = data.split("/");
		//filename = token[token.length-1];
        this.filename = filename;
		
		//Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		//albumPath = ContentUris.withAppendedId(sArtworkUri, this.album_id);
        albumPath = null;

        // status:
        // 0 -> not in local,1 -> downloading, 2 -> in local
        if (data.compareTo("null") == 0) {
            this.status = 0;
        }
        else {
            this.status = 2;
        }

        if (title.compareTo("unknown")==0){
            this.title = filename;
        }
        else {
            this.title = title;
        }
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
