package com.sagax.player;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sagax.player.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AllListAdapter extends ArrayAdapter<Cata>{
    	private LayoutInflater li;
    	private Map<String,Cata> catamap;
    	private List<Cata> catas;
    	private List<Cata> fcatas; 
    	private List<CataAdapter> adapters;
    	private List<CataAdapter> fadapters;
    	public List<Song> selectList;
    	private MusicManager musicManager;
    	private MediaManager mediaManager;
    	int type;
    	MainActivity activity;
    	
    	public AllListAdapter(Context context,int viewid,List<Song> songs,int type){
    		super(context, viewid);

            setActivity((MainActivity)context);

    		catamap = new HashMap<String,Cata>();
    		selectList = new ArrayList<Song>();

    		this.type = type;
    		int id=0;
    		for(int i =0; i<songs.size();i++){
    			Song song = songs.get(i);
    			Character cat = song.title.toUpperCase().charAt(0);
    			String str;
    			if(Character.isDigit(cat))
    				str="0";
    			else
    				str=Character.toString(cat);
    			Cata cata = catamap.get(str);
    			if(cata == null){
    				ArrayList<Song> newl = new ArrayList<Song>();
    				newl.add(song);
    				catamap.put(str, new Cata(str,newl,id));
    				id++;
    			}else{
    				cata.songs.add(song);
    			}
    		}
    		catas = new ArrayList<Cata>();
    		Iterator<Entry<String,Cata>> it = catamap.entrySet().iterator();
    		while(it.hasNext()){
    			catas.add(it.next().getValue());
    		}
    		Collections.sort(catas, new Comparator<Cata>(){
	    		@Override
	    		public int compare(Cata l,Cata r){
	    			return l.title.compareTo(r.title);
	    		}
			});
    		
    		fcatas = new ArrayList<Cata>(catas);
    		adapters=new ArrayList<CataAdapter>();
    		for(int i=0;i<catas.size();i++)
    			adapters.add(new CataAdapter(context,catas.get(i),musicManager.getCurrSong()));
    		fadapters=new ArrayList<CataAdapter>(adapters);
    		li = LayoutInflater.from(context);
    	}
    	
    	public List<Cata> getCata(){
    		return catas;
    	}
    	
    	public String[] getTitles(){
    		List<String> tmpList = new ArrayList<String>();
    		for (int i = 0; i < fcatas.size(); i++) {
				tmpList.add(fcatas.get(i).title);
			}
    		return tmpList.toArray(new String[0]);
    	}
    	
    	public List<Cata> applyFilter(CharSequence arg){
    		if(arg.equals(""))
    			return catas;
    		fcatas=new ArrayList<Cata>();
    		fadapters=new ArrayList<CataAdapter>();
    		for(int i=0;i<catas.size();i++){
    			adapters.get(i).applyFilter(arg);;
    			if(adapters.get(i).songs.size()!=0){
    				fcatas.add(catas.get(i));
    				fadapters.add(adapters.get(i));
    			}
    		}
    		
    		this.notifyDataSetChanged();
    		return fcatas;
    	}
    	
    	public void setActivity(MainActivity m){
    		activity = m;
    		musicManager = activity.getMusicManagerInstance();
    		mediaManager = activity.getMediaManagerInstance();
    	}

    	public int getCount(){
    		return fcatas.size();
    	}
    	public long getItemId(int index){
    		return getItem(index).id;
    	}
    	public Cata getItem(int index){
    		return fcatas.get(index);
    	}
    	public View getView(final int position, View convertView, ViewGroup parent){
    		ViewHolder holder = null;
    		Cata catagory = getItem(position);
    		if(convertView == null || convertView.getTag()==null){
    			holder = new ViewHolder();
    			convertView = li.inflate(R.layout.text_list_catagory, null);
    			holder.title= (TextView) convertView.findViewById(R.id.text_cata_title);
    			holder.list = (LinearLayout) convertView.findViewById(R.id.textcataview);
    			convertView.setTag(holder);
    		}else{
    			holder = (ViewHolder)convertView.getTag();
    		}
    		
    		holder.title.setText(catagory.title);
    		holder.list.removeAllViews();
    		for (int i = 0; i < fadapters.get(position).getCount(); i++) {
    			  View item = fadapters.get(position).getView(i, null, null);
    			  
    			  holder.list.addView(item);
    		}
    		return convertView;
    	}
    	private class ViewHolder{
    		TextView title;
    		LinearLayout list;
    	}


    private class CataAdapter extends SongListAdapter{
        Context context;
        Cata cata;
        ArrayList<Song> songs;
        public CataAdapter(Context context,Cata cata,Song current){
            super(context, cata.songs, current);
            this.context = context;
            this.cata=cata;
            songs = new ArrayList<Song>(cata.songs);
        }
        public Song getItem(int index){
            return songs.get(index);
        }
        public int getCount(){
            return songs.size();
        }

        public View getView(final int pos, View convertView, ViewGroup parent){
            ViewHolder holder = null;
            final Song currSong=getItem(pos);

            holder=new ViewHolder();

            if (type == 0) {
                if(convertView == null||convertView.getTag()==null){

                    convertView = li.inflate(R.layout.text_list_element,null);
                    holder.title=(TextView) convertView.findViewById(R.id.songtitle);
                    holder.duration=(TextView) convertView.findViewById(R.id.songlength);
                    holder.selected = (CheckBox) convertView.findViewById(R.id.select);

                    holder.selected.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            CheckBox checkBox = (CheckBox)arg0;
                            if(checkBox.isChecked()){
                                selectList.add(currSong);
                            }
                            else{
                                selectList.remove(currSong);
                            }

                        }
                    });
                }

                holder.title.setTextColor(Color.rgb(255, 235, 0));
                holder.duration.setTextColor(Color.rgb(255, 235, 0));

                holder.selected.setChecked(selectList.contains(currSong));
                holder.title.setTextColor(Color.rgb(255, 255, 255));
                holder.duration.setTextColor(Color.rgb(255, 255, 255));

                holder.title.setText(currSong.title);
                holder.duration.setText(currSong.gtDuration());
            }
            else {
                convertView = super.getView(pos, convertView, parent);

                holder.title=(TextView) convertView.findViewById(R.id.songtitle);
                holder.title.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        if (currSong.status != 2) {
                            Toast.makeText(activity.getApplicationContext(), "not in local\nCan't play", Toast.LENGTH_SHORT).show();
                            Log.d("song status", "not in local");
                        }
                        else {
                            Log.d("song status", "in local");
                            ArrayList<Song> tmpArrayList = mediaManager.getAllSong();
                            Playlist playlist = new Playlist(tmpArrayList);
                            musicManager.setCurrentPlaylist(playlist);
                            musicManager.playIndex(tmpArrayList.indexOf(currSong));

                            ActivityView av = activity.act.get(activity.statusList[3]);
                            av.mini_refresh();
                            /*
                            ActivityView av = activity.act.get(activity.statusList[3]);
                            av.finish();
                            av = activity.act.get(activity.statusList[0]);
                            activity.init();
                            av.display();
                            av.setSwipe();
                            activity.setClose();
                            */
                        }
                    }
                });
            }

            return convertView;
/*
            if(convertView == null||convertView.getTag()==null){
                holder=new ViewHolder();
                if(type == 0){
                    convertView = li.inflate(R.layout.text_list_element,null);
                    holder.title=(TextView) convertView.findViewById(R.id.songtitle);
                    holder.duration=(TextView) convertView.findViewById(R.id.songlength);
                    holder.selected = (CheckBox) convertView.findViewById(R.id.select);

                    holder.selected.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            CheckBox checkBox = (CheckBox)arg0;
                            if(checkBox.isChecked()){
                                selectList.add(currSong);
                            }
                            else{
                                selectList.remove(currSong);
                            }

                        }
                    });
                }
                else{
                    convertView = li.inflate(R.layout.songlist,null);
                    holder.title=(TextView) convertView.findViewById(R.id.songtitle);
                    //holder.duration=(TextView) convertView.findViewById(R.id.songlength);
                    holder.currentImageView = (ImageView)convertView.findViewById(R.id.current);
                    holder.title.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            if (currSong.status != 2) {
                                Toast.makeText(activity.getApplicationContext(), "not in local\nCan't play", Toast.LENGTH_SHORT).show();
                                Log.d("song status", "not in local");
                            }
                            else {
                                Log.d("song status", "in local");
                                ArrayList<Song> tmpArrayList = mediaManager.getAllSong();
                                Playlist playlist = new Playlist(tmpArrayList);
                                musicManager.setCurrentPlaylist(playlist);
                                musicManager.playIndex(tmpArrayList.indexOf(currSong));
                                ActivityView av = activity.act.get(activity.statusList[3]);
                                av.finish();
                                av = activity.act.get(activity.statusList[0]);
                                activity.init();
                                av.display();
                                av.setSwipe();
                                activity.setClose();
                            }
                        }
                    });
                }

                convertView.setTag(holder);
            }else{

                holder = (ViewHolder)convertView.getTag();
            }



            holder.title.setTextColor(Color.rgb(255, 235, 0));
            holder.duration.setTextColor(Color.rgb(255, 235, 0));
            if(type == 0){
                holder.selected.setChecked(selectList.contains(currSong));
                holder.title.setTextColor(Color.rgb(255, 255, 255));
                holder.duration.setTextColor(Color.rgb(255, 255, 255));
            }
            else{
                if (musicManager.getCurrSong() != null) {
                    if(!musicManager.getCurrSong().id.equals(currSong.id)){
                        holder.currentImageView.setVisibility(ViewGroup.INVISIBLE);
                        holder.title.setTextColor(Color.rgb(255, 255, 255));
                        holder.duration.setTextColor(Color.rgb(255, 255, 255));
                    }
                }
            }


            holder.title.setText(currSong.title);
            holder.duration.setText(currSong.gtDuration());
            return convertView;
            */
        }

        private class ViewHolder{
            TextView title;
            TextView duration;
            CheckBox selected;
            ImageView currentImageView;
            int position;
        }
        public void applyFilter(CharSequence arg){
            String cs = arg.toString().toLowerCase();
            ArrayList<Song> list;
            if (cs == null || cs.length() == 0)
            {
                list = new ArrayList<Song>(cata.songs);
            }else{
                list = new ArrayList<Song>();
                for(int i=0;i<cata.songs.size();i++){
                    Song song=cata.songs.get(i);
                    String songName = song.title.toLowerCase();
                    String artist = song.artist.toLowerCase();
                    if(songName.contains(cs) || artist.contains(cs)){
                        list.add(song);
                    }

                }
            }
            songs = list;
        }

    }

    /*
    	private class CataAdapter extends ArrayAdapter<Song>{
    		Context context;
    		Cata cata;
    		List<Song> songs;
    		public CataAdapter(Context context,Cata cata){
    			super(context,android.R.layout.simple_list_item_1,cata.songs);
    			this.context = context;
    			this.cata=cata;
    			songs=new ArrayList<Song>(cata.songs);
    		}
    		public Song getItem(int index){
    			return songs.get(index);
    		}
    		public int getCount(){
    			return songs.size();
    		}
    		public View getView(final int pos, View convertView, ViewGroup parent){
        		ViewHolder holder = null;
        		final Song currSong=getItem(pos);
        		
        		
        		if(convertView == null||convertView.getTag()==null){
        			holder=new ViewHolder();
        			if(type == 0){
            			convertView = li.inflate(R.layout.text_list_element,null);
            			holder.title=(TextView) convertView.findViewById(R.id.songtitle);
            			holder.duration=(TextView) convertView.findViewById(R.id.songlength);
            			holder.selected = (CheckBox) convertView.findViewById(R.id.select);
            			
            			holder.selected.setOnClickListener(new OnClickListener() {
    						
    						@Override
    						public void onClick(View arg0) {
    							// TODO Auto-generated method stub
    							CheckBox checkBox = (CheckBox)arg0;
    							if(checkBox.isChecked()){
    								selectList.add(currSong);
    							}
    							else{
    								selectList.remove(currSong);
    							}
    							
    						}
    					});        				
        			}
        			else{
            			convertView = li.inflate(R.layout.text_list_element2,null);
            			holder.title=(TextView) convertView.findViewById(R.id.songtitle);
            			holder.duration=(TextView) convertView.findViewById(R.id.songlength);
            			holder.currentImageView = (ImageView)convertView.findViewById(R.id.current);
            			holder.title.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
                                if (currSong.status != 2) {
                                    Toast.makeText(activity.getApplicationContext(), "not in local\nCan't play", Toast.LENGTH_SHORT).show();
                                    Log.d("song status", "not in local");
                                }
                                else {
                                    Log.d("song status", "in local");
                                    ArrayList<Song> tmpArrayList = mediaManager.getAllSong();
                                    Playlist playlist = new Playlist(tmpArrayList);
                                    musicManager.setCurrentPlaylist(playlist);
                                    musicManager.playIndex(tmpArrayList.indexOf(currSong));
                                    ActivityView av = activity.act.get(activity.statusList[3]);
                                    av.finish();
                                    av = activity.act.get(activity.statusList[0]);
                                    activity.init();
                                    av.display();
                                    av.setSwipe();
                                    activity.setClose();
                                }
							}
						});
        			}
        			
        			convertView.setTag(holder);
        		}else{
        			
        			holder = (ViewHolder)convertView.getTag();
        		}
        		
        		holder.title.setTextColor(Color.rgb(255, 235, 0));
        		holder.duration.setTextColor(Color.rgb(255, 235, 0));
        		if(type == 0){
        			holder.selected.setChecked(selectList.contains(currSong));
    				holder.title.setTextColor(Color.rgb(255, 255, 255));	
    				holder.duration.setTextColor(Color.rgb(255, 255, 255));	
        		}
        		else{
                    if (musicManager.getCurrSong() != null) {
                        if(!musicManager.getCurrSong().id.equals(currSong.id)){
                            holder.currentImageView.setVisibility(ViewGroup.INVISIBLE);
                            holder.title.setTextColor(Color.rgb(255, 255, 255));
                            holder.duration.setTextColor(Color.rgb(255, 255, 255));
                        }
                    }
        		}


        		holder.title.setText(currSong.title);
        		holder.duration.setText(currSong.gtDuration());
        		return convertView;
    		}
    		private class ViewHolder{
        		TextView title;
        		TextView duration;
        		CheckBox selected;
        		ImageView currentImageView;
        		int position;
    		}
    		public void applyFilter(CharSequence arg){
    			String cs = arg.toString().toLowerCase();
    			List<Song> list;
	            if (cs == null || cs.length() == 0)
	            {
	                list = new ArrayList<Song>(cata.songs);
	            }else{
	            	list = new ArrayList<Song>();
	            	for(int i=0;i<cata.songs.size();i++){
	            		Song song=cata.songs.get(i);
	            		String songName = song.title.toLowerCase();
	            		String artist = song.artist.toLowerCase();
	            		if(songName.contains(cs) || artist.contains(cs)){
	            			list.add(song);
	            		}
	            	
	            	}
	            }
	            songs = list;
    		}
    		
    	}
    	*/
    }
