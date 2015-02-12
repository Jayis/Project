package com.sagax.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class MediaManager
{

    private String songTableName;
    private Cursor cursor_songs;
    private SQLiteDatabase database;
    private SharedPreferences sharedPref;

    private long curNewAlbumID;
    private long curNewArtistID;
    private long curNewGenreID;
    ///

	private Context context;
	private Cursor playlistCursor;
	private Playlist currentPlaylist;

    
    private String allplaylistName = "";
    
	// tags for projection
	public static final int MEDIA_ID = 0 , MEDIA_ALBUM_ID = 1 , MEDIA_ARTIST = 2 , MEDIA_ALBUM = 3 ,
					  MEDIA_TITLE = 4 , MEDIA_DATA = 5 , MEDIA_DURATION = 6 , MEDIA_ARTIST_ID = 7 ;

	// projection for accessing the DB 
	private String[] projection = { 
									MediaStore.Audio.Media._ID,
                					MediaStore.Audio.Media.ALBUM_ID,
            		   	            MediaStore.Audio.Media.ARTIST,
									MediaStore.Audio.Media.ALBUM,
                					MediaStore.Audio.Media.TITLE,
									MediaStore.Audio.Media.DATA,
               						MediaStore.Audio.Media.DURATION,
									MediaStore.Audio.Media.ARTIST_ID,
								  };


	private String[] projection_playlist = {
                                            MediaStore.Audio.Playlists._ID,
                                            MediaStore.Audio.Playlists.NAME
										   };
    
    private Map<String,String> playlistMap;
    
    
	// store all song in a map by using _ID as primary key.
	private Map<String,Song> songMap;
	private ArrayList<Song> songList;
	// store all keys( Artists,Albums ) with song's ID 
	private Map<String,ArrayList<String>> albumMap;
    private Map<String,String> albumTitleMaptoID;
    private Map<String,String> albumIDMaptoTitle;

	private Map<String,ArrayList<String>> artistMap;
	private Map<String,String> artistNameMaptoID;
    private Map<String,String> artistIDMaptoName;

    private Map<String,String> genreTypeMaptoID;
    private Map<String,String> genreIDMaptoType;

	private Map<String,Uri> albumArtMap;
	
	
	// genreMap and a sorted genre list
	private Map<String,String> genreMap;
	private ArrayList<String> genreList;

	private int numberOfSongs = 0;



	public MediaManager(Context context){
        sharedPref = LoginMainActivity.shareSharePref();
        songTableName = sharedPref.getString("songTable", null);
        database = LoginMainActivity.shareDB();
        cursor_songs = database.rawQuery("SELECT * FROM " + songTableName, null);

		this.numberOfSongs = cursor_songs.getCount();
        /*
        playlistCursor = context.getContentResolver().query(
                                                            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                                                            projection_playlist,
                                                            MediaStore.Audio.Playlists.NAME + "!=?",
                                                            new String[] { "0" },
                                                            MediaStore.Audio.Playlists.DATE_ADDED
                                                            );
        */

		initAllLists();
	}
	
	public String[] getAllTypeID(){
        ArrayList<String> allGenreID = new ArrayList<String>();

        for (int i = 0; i < genreTypeMaptoID.size(); i++) {
            allGenreID.add(String.valueOf(i));
        }

        return allGenreID.toArray(new String[0]);
	}
	
	public String[] getAllAlbumID(){
        ArrayList<String> allAlbumID = new ArrayList<String>();

        for (int i = 0; i < albumTitleMaptoID.size(); i++) {
            allAlbumID.add(String.valueOf(i));
        }

        return allAlbumID.toArray(new String[0]);
        /*
		mediaCursor = context.getContentResolver().query(
				 MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				 new String[]{ MediaStore.Audio.Media.ALBUM_ID , MediaStore.Audio.Media.ALBUM },
				 MediaStore.Audio.Media.IS_MUSIC+"!=?",
				 new String[]{"0"},
				 MediaStore.Audio.Media.ALBUM_KEY + " ASC"
				 );
		//String sortedAlbumID[] = new String[mediaCursor.getCount()];
		ArrayList<String> sortedAlbumID = new ArrayList<String>();
		String tempID = "";
		for(mediaCursor.moveToFirst() ; !mediaCursor.isAfterLast(); mediaCursor.moveToNext() ){
			
			// skip the same id
			if( tempID.contains( mediaCursor.getString(0) ) ){
				continue;
			}
			tempID = mediaCursor.getString(0);
			sortedAlbumID.add( mediaCursor.getString(0) );
			
		}
		mediaCursor.close();
		return sortedAlbumID.toArray(new String[0]);
		//return albumTitleMap.keySet().toArray(new String[0]);
		*/
	}

	public String[] getAllArtistID(){
        ArrayList<String> allArtistID = new ArrayList<String>();

        for (int i = 0; i < artistNameMaptoID.size(); i++) {
            allArtistID.add(String.valueOf(i));
        }

        return allArtistID.toArray(new String[0]);
        /*
		return artistNameMap.keySet().toArray(new String[0]);
		*/
	}

	public String[] getAllSongID(){
		ArrayList<String> sortedSongID = new ArrayList<String>();
		for( Song song : songList){
			sortedSongID.add( song.id );
		}
		return sortedSongID.toArray(new String[0]);
	}
	
	public ArrayList<Song> getAllSong(){
		return songList;
	}

	
	public ArrayList<Song> getSongsByTypeID(String typeID){

        String qType = genreIDMaptoType.get(typeID);

        cursor_songs = database.rawQuery("SELECT " + DBHelper.COLUMN_SERVERID + " FROM " + songTableName + " WHERE " + DBHelper.COLUMN_GENRE + " = '" + qType + "'", null);

        cursor_songs.moveToFirst();

        ArrayList<Song> list = new ArrayList<Song>();
        for (int i = 0; i < cursor_songs.getCount(); i++) {
            list.add( songMap.get( String.valueOf( cursor_songs.getInt(cursor_songs.getColumnIndex(DBHelper.COLUMN_SERVERID)) ) ) );
        }
        cursor_songs.close();
        currentPlaylist = new Playlist( list );

        return list;
/*
		if( genreMap==null){
			initTypeList();
		}
		// if the album can not be found , return null
		if( !genreMap.containsKey( typeID ) )
			return null;

		// append genre member uri
		String CONTENTDIR = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY;
		Uri uri = Uri.parse(
	            new StringBuilder()
	            .append(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString())
	            .append("/")
	            .append(typeID)
	            .append("/")
	            .append(CONTENTDIR)
	            .toString());

		// query the db for list of song id of same genre
		mediaCursor = context.getContentResolver().query(
				uri,
				new String[]{ MediaStore.Audio.Media._ID ,
							},
				null,
			 	null,
			 	MediaStore.Audio.Media.TITLE_KEY
		  );

		ArrayList<Song> list = new ArrayList<Song>();
		for(mediaCursor.moveToFirst() ; !mediaCursor.isAfterLast(); mediaCursor.moveToNext() ){
			list.add( songMap.get( mediaCursor.getString(0) ) );
		}
		mediaCursor.close();
		currentPlaylist = new Playlist( list );

		return list;
		*/
	}
	
	public ArrayList<Song> getSongsByAlbumID(String albumID){
		// if the album can not be found , return null
		if( !albumMap.containsKey( albumID ) )
			return null;
		
		ArrayList<Song> list = new ArrayList<Song>();
		for(String songID : albumMap.get( albumID )){
			list.add( songMap.get( songID ) );
		}
		currentPlaylist = new Playlist( list );
		return list;
	}

	public ArrayList<Song> getSongsByArtistID(String artistID){
		// if the artist can not be found , return null
		if( !artistMap.containsKey( artistID ) )
			return null;
		
		ArrayList<Song> list = new ArrayList<Song>();
		for(String songID : artistMap.get( artistID )){
			list.add( songMap.get( songID ) );
		}
		currentPlaylist = new Playlist( list );
		return list;
	}

	public String getTypeNameByID(String typeID){
		return genreIDMaptoType.get(typeID);
	}
	
	public String getAlbumNameByID(String albumID){
		return albumIDMaptoTitle.get(albumID);
	}

	public String getArtistNameByID(String artistID){
		return artistIDMaptoName.get(artistID);
	}

	public String getSongNameByID(String SongID){
		return songMap.get(SongID).title;
	}
	
	public Song getSongByID(String SongID){
		return songMap.get(SongID);
	}

	public Uri getAlbumArtByID(String albumID){
		return albumArtMap.get(albumID);
	}

	public String getAlbumArtistByID(String albumID){
		String id = albumMap.get(albumID).get(0);
		return songMap.get(id).artist;
	}

	public Playlist getPlaylist(){
		if( currentPlaylist == null){
			
			currentPlaylist = new Playlist( songList );
		}
			
		return this.currentPlaylist;
	}


	private void initAllLists(){
		// init the map
		this.songMap = new HashMap<String,Song>();
		this.songList = new ArrayList<Song>();
		// init all attribute lists.
		this.albumMap = new HashMap<String,ArrayList<String>>();
		this.albumTitleMaptoID = new HashMap<String,String>();
        this.albumIDMaptoTitle = new HashMap<String,String>();

		this.albumArtMap = new HashMap<String,Uri>();
		
		this.artistMap = new HashMap<String,ArrayList<String>>();
		this.artistNameMaptoID = new HashMap<String,String>();
        this.artistIDMaptoName = new HashMap<String,String>();

        this.genreTypeMaptoID = new HashMap<String,String>();
        this.genreIDMaptoType = new HashMap<String,String>();

		curNewAlbumID = 0;
        curNewArtistID = 0;
        curNewGenreID = 0;

		// move the cursor to the first row( Song )
		cursor_songs.moveToFirst();
		// iterate the cursor
		for( int i=0; i < cursor_songs.getCount(); i++ ){
            if ( !albumTitleMaptoID.containsKey(cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_ALBUM))) ) {
                albumTitleMaptoID.put( cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_ALBUM)), String.valueOf(curNewAlbumID) );
                albumIDMaptoTitle.put( String.valueOf(curNewAlbumID), cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_ALBUM)) );
                curNewAlbumID++;
            }
            if ( !artistNameMaptoID.containsKey(cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_ARTIST))) ) {
                artistNameMaptoID.put( cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_ARTIST)), String.valueOf(curNewArtistID) );
                artistIDMaptoName.put( String.valueOf(curNewArtistID), cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_ARTIST)) );
                curNewArtistID++;
            }
            if ( !genreTypeMaptoID.containsKey(cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_GENRE))) ) {
                genreTypeMaptoID.put( cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_GENRE)), String.valueOf(curNewGenreID) );
                genreIDMaptoType.put( String.valueOf(curNewGenreID), cursor_songs.getString(cursor_songs.getColumnIndex(DBHelper.COLUMN_GENRE)) );
                curNewGenreID++;
            }

			Song currentSong = createSong( cursor_songs );

			songList.add(currentSong);
			songMap.put( currentSong.id, currentSong );

			// use parsed album art Uri as value
			if( !albumMap.containsKey( String.valueOf(currentSong.album_id) )){
				albumMap.put( String.valueOf(currentSong.album_id) , new ArrayList<String>() );
				albumArtMap.put( String.valueOf(currentSong.album_id) , currentSong.albumPath );
			}

			if( !artistMap.containsKey( artistNameMaptoID.get(currentSong.artist) )){
				artistMap.put( artistNameMaptoID.get(currentSong.artist) , new ArrayList<String>() );
			}


			// add ID to list
			albumMap.get( String.valueOf(currentSong.album_id) ).add( currentSong.id );
			artistMap.get( artistNameMaptoID.get(currentSong.artist) ).add( currentSong.id );


            cursor_songs.moveToNext();
		}

        cursor_songs.close();


        // init playlist map
        playlistMap = new HashMap<String,String>();
        /*
        playlistCursor.moveToFirst();
        for( int i=0; i<playlistCursor.getCount(); i++ ){
        
            if( !playlistMap.containsKey( playlistCursor.getString(0) )){
                playlistMap.put( playlistCursor.getString(0) , playlistCursor.getString(1));
                allplaylistName += playlistCursor.getString(1)+"\n";
            }
            playlistCursor.moveToNext();
        }
        playlistCursor.close();
        */
	}
	
	/*
	private void initTypeList(){




		mediaCursor = context.getContentResolver().query(
							MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
							new String[]{ MediaStore.Audio.Genres._ID ,
										  MediaStore.Audio.Genres.NAME
										},
							null,
						 	null,
						 	MediaStore.Audio.Genres.DEFAULT_SORT_ORDER 
					  );
		 
		this.genreMap = new HashMap<String,String>();
		this.genreList = new ArrayList<String>();
		
		ArrayList<String> genreID = new ArrayList<String>();
		ArrayList<String> genreName = new ArrayList<String>();
		
		mediaCursor.moveToFirst();
		// iterate the cursor
		for( int i=0; i < mediaCursor.getCount(); i++ ){
			//System.out.println( "CONTENT:" + mediaCursor.getString( 1 ) );

			genreID.add( mediaCursor.getString( 0 ) );
			genreName.add( mediaCursor.getString( 1 ) );
			
			
			mediaCursor.moveToNext();
		}
		
		// this loop is used to prevent the genre directory contains 0 song
		for( int i = 0; i < genreID.size() ; i++ ){
			String typeID = genreID.get(i); 
			// test if the number of member is 0
			String CONTENTDIR = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY;
			Uri uri = Uri.parse(
					new StringBuilder()
					.append(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString())
					.append("/")
					.append(typeID)
					.append("/")
					.append(CONTENTDIR)
					.toString());
			
			// query the db for list of song id of same genre
			Cursor testNumberCursor = context.getContentResolver().query(
					uri,
					new String[]{ MediaStore.Audio.Media._ID ,
					},
					null,
					null,
					MediaStore.Audio.Media.TITLE_KEY 
					);
						
			if( testNumberCursor.getCount() == 0){
				continue;
			}
			
			genreList.add( typeID );
			genreMap.put(typeID, genreName.get(i));
			
			
		}
		 

	}
	*/
	

    public String[] getAllPlaylistID(){
        return playlistMap.keySet().toArray(new String[0]);
    }


    public String getPlaylistNameByID(String id){
        return playlistMap.get(id);
    }


    public ArrayList<Song> getPlaylistByID(String id){
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(id) );
        Cursor c = context.getContentResolver().query(
                                           uri,
                                           new String[] { MediaStore.Audio.Playlists.Members.AUDIO_ID },
                                           null,
                                           //new String[] { MediaStore.Audio.Playlists.Members.AUDIO_ID },
                                           //MediaStore.Audio.Media.IS_MUSIC +" != 0 ",
                                           null,
                                           null
                                           //MediaStore.Audio.Playlists.Members.PLAY_ORDER
                                           );
        ArrayList<Song> playlist = new ArrayList<Song>();
        String songs = "";
        c.moveToFirst();
        for( int i =0; i< c.getCount(); i++ ){
            String audio_id = c.getString(0);
            if(songMap.containsKey(audio_id)){ //bugs: song not in songMap
            	playlist.add( songMap.get(audio_id));
            }
            c.moveToNext();
        }
        
        //Toast.makeText( context , id + playlist.toString() , Toast.LENGTH_SHORT).show();
        c.close();
        return playlist;
    }

	public int createPlaylist( String playlistName ){
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, playlistName);
        Uri uri = context.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
        
        if( uri != null ){
            Cursor c = queryPlaylist(context,
                                     MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                                     new String[] { MediaStore.Audio.Playlists._ID },
                                     MediaStore.Audio.Playlists.NAME + "=?",
                                     new String[] { playlistName },
                                     MediaStore.Audio.Playlists.NAME);
            /*
            int playlistId = -1;
            if( c.getCount() > 0){
            	
            }else{
            	playlistId = intFromCursor(c);
            }*/
            
            int playlistId = intFromCursor(c);
            
            // update this new playlist to map immediatelly
            playlistMap.put(String.valueOf(playlistId),playlistName);

            c.close();
            return playlistId;
        }
        
        
        return -1;
	}
    
    public int removePlaylistById( String id ){
        //long YOUR_PLAYLIST_ID = Long.parseLong( id );
        //Uri uri = MediaStore.Audio.Playlists.getContentUri(YOUR_PLAYLIST_ID);
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = {id};
        playlistMap.remove( id );
        return context.getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
    }
    
    
    public boolean setPlaylist(ArrayList<Song> playlist , String playlistid ){
        
		long YOUR_PLAYLIST_ID = Long.parseLong( playlistid );
		Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", YOUR_PLAYLIST_ID);
        context.getContentResolver().delete(uri, null, null);      
        int size = playlist.size();
        ContentValues values [] = new ContentValues[size];
        for (int k = 0; k < size; ++k) {
            values[k] = new ContentValues();
            values[k].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, k);
            values[k].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, playlist.get(k).id);
            context.getContentResolver().insert(uri, values[k]);
        }
        context.getContentResolver().bulkInsert(uri, values);  

		return true;
	}
    
    public boolean setPlaylist(ArrayList<Song> playlist , String playlistid, String playlistName){
        
		long YOUR_PLAYLIST_ID = Long.parseLong( playlistid );
		Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", YOUR_PLAYLIST_ID);
        context.getContentResolver().delete(uri, null, null);      
        int size = playlist.size();
        ContentValues values [] = new ContentValues[size];
        for (int k = 0; k < size; ++k) {
            values[k] = new ContentValues();
            values[k].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, k);
            values[k].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, playlist.get(k).id);
            context.getContentResolver().insert(uri, values[k]);
        }
        context.getContentResolver().bulkInsert(uri, values);        
        ContentValues values2 = new ContentValues(1);
        values2.put(MediaStore.Audio.Playlists.NAME, playlistName);
        context.getContentResolver().update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values2, MediaStore.Audio.Playlists._ID + "=?", new String[] {playlistid});
        playlistMap.put(playlistid, playlistName);
		return true;
	}
    
    
    
    /*
     *
     *  Private method for this instance
     *
     *
     */
     
     
    
    private int intFromCursor(Cursor c) {
        int id = -1;
        if (c != null) {
            c.moveToLast();
            /*
            if (!c.isAfterLast()) {
                id = c.getInt(0);
            }*/
            id = c.getInt(0);
        }
        c.close();

        return id;
    }

    
    private Cursor queryPlaylist(Context context, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String sortOrder, int limit) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            if (limit > 0) {
                uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }
    }
    
    private Cursor queryPlaylist(Context context, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String sortOrder) {
        return queryPlaylist(context, uri, projection, selection, selectionArgs, sortOrder, 0);
    }


	// create new song by current cursor
	// make sure the cursor is in legal position or it'll have unpredicable behavior.
	private Song createSong(Cursor c){
        return new Song(
                String.valueOf(c.getInt(c.getColumnIndex(DBHelper.COLUMN_SERVERID))) ,
                c.getString(c.getColumnIndex(DBHelper.COLUMN_ARTIST)) ,
                c.getString(c.getColumnIndex(DBHelper.COLUMN_ALBUM)) ,
                c.getString(c.getColumnIndex(DBHelper.COLUMN_TITLE)) ,
                c.getString(c.getColumnIndex(DBHelper.COLUMN_LOCALURI)) ,
                albumTitleMaptoID.get(c.getString(c.getColumnIndex(DBHelper.COLUMN_ALBUM))) ,
                300,
                c.getString(c.getColumnIndex(DBHelper.COLUMN_GENRE)),
                c.getString(c.getColumnIndex(DBHelper.COLUMN_URL)),
                c.getString(c.getColumnIndex(DBHelper.COLUMN_FILENAME)),
                c.getInt(c.getColumnIndex(DBHelper.COLUMN_EQON))
        );
        /*
		return new Song( c.getString(MEDIA_ID) ,
						 c.getString(MEDIA_ARTIST) ,
						 c.getString(MEDIA_ALBUM) ,
						 c.getString(MEDIA_TITLE) ,
						 c.getString(MEDIA_DATA) ,
						 c.getString(MEDIA_ALBUM_ID) ,
						 c.getInt(MEDIA_DURATION)
					    );
					    */
	}

}




