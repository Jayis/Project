package com.sagax.player;

import java.util.ArrayList;

public class Cata{
	int id;
	ArrayList<Song> songs;
	String title;
	public Cata(String title,ArrayList<Song> songs,int id){
		this.title=title;
		this.songs=songs;
		this.id=id;
	}
}