package com.sagax.player;

import java.util.List;

public class Cata{
	int id;
	List<Song> songs;
	String title;
	public Cata(String title,List<Song> songs,int id){
		this.title=title;
		this.songs=songs;
		this.id=id;
	}
}