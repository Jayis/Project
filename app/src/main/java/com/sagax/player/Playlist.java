package com.sagax.player;


/**
 * @author JianZhangChen
 * This class is used to store play list information
 * The music manager will take this class type object to play music
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

public class Playlist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5172146263052140577L;
	private ArrayList<Song> songsList = new ArrayList<Song>();
	private ArrayList<Song> randomSongsList = new ArrayList<Song>();
	private int current = 0;
	private int randomCurrent = 0;
	private boolean randomOn = false;
	private String listName;
	private boolean repeatFlag = false;
	//private String avatarPath;
	
	
	public Playlist(ArrayList<Song> playList){
		this.songsList = playList;
		if(songsList.size() > 0)
			setRandomList();
	}
	
	public Playlist(){
	}
	
	public void setRepeat(boolean r){
		repeatFlag = r;
	}

	public boolean getRepeat(){
		return repeatFlag;
	}

	private void setRandomList(){
		randomSongsList = (ArrayList<Song>) songsList.clone();
		Collections.shuffle(randomSongsList);
		randomCurrent = randomSongsList.indexOf(songsList.get(current));
	}
	
	public Song getNextSong(){
		if(!repeatFlag){
			if(current == songsList.size() - 1)
				return null;
		}
		if(randomOn){
			randomCurrent = (randomCurrent+1)%songsList.size();
			current = songsList.indexOf(randomSongsList.get(randomCurrent));
			return randomSongsList.get(randomCurrent);
		}
		else{
			current = (current+1)%songsList.size();
			randomCurrent = randomSongsList.indexOf(songsList.get(current));
			return songsList.get(current);
		}
	}
	
	public Song getPrevSong(){
		if(randomOn){
			randomCurrent = (randomCurrent-1)%songsList.size();
			current = songsList.indexOf(randomSongsList.get(randomCurrent));
			return randomSongsList.get(randomCurrent);
		}
		else{
			current = (current + songsList.size() - 1)%songsList.size();
			randomCurrent = randomSongsList.indexOf(songsList.get(current));
			return songsList.get(current);
		}
	}	
	
	public Song getSongIndex(int index){
		current = index;
		return songsList.get(current);
	}
	
	public Song getCurrSong(){
		if(songsList.size() > 0)
			return songsList.get(current);
		else 
			return null;
	}
	public boolean toggleRandom(){
		if(randomOn)
			randomOn = false;
		else
			randomOn = true;
		return randomOn;
	}
	
	public ArrayList<Song> getSongList(){
		return songsList;
	}
	
	public void setSongList(ArrayList<Song> songs){
		songsList = songs;
		setRandomList();
	}
	
	public Song get(int index){
		return songsList.get(index);
	}
	
	public void add(Song song){		
		songsList.add(song);	
	}
	
	public void add(int index,Song song){
		songsList.add(index, song);
	}
	
	public void remove(Song song){
		songsList.remove(song);
	}
	
	public void remove(int index){
		songsList.remove(index);
	}
	
	public void setListName(String listName){
		this.listName = listName;
	}
	
	public String getListName(){
		return this.listName;
	}
}
