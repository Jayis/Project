package com.sagax.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sagax.player.R;

import android.R.bool;
import android.R.integer;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

public class MusicManager {
	
	private Context context;
	private int count;
	private boolean randomOn = false;
	private boolean eqOn = false;
	private int mode = 0;
	private MediaPlayer mediaPlayer;
	private MediaManager mediaManager;
	private double[] eqTags;
	private Playlist playlist;
	public EQManager eqManager;
	private AudioManager am;
	private int repeatStatus = 0;
	    
	    // Constructor
	public MusicManager(Context context){
		this.context = context;
		this.mediaManager = new MediaManager( context );
		//playlist = new Playlist(context);
		playlist = mediaManager.getPlaylist();
		eqManager=new EQManager(context);
		//eqManager.addList(mediaManager.getAllSong());
		am = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
		setUpMediaPlayer();
		initGenreClassifier();
	}
	
	private void initGenreClassifier(){
		BufferedReader modelReader = new BufferedReader( new InputStreamReader( context.getResources().openRawResource(R.raw.svm) ) );
		BufferedReader tagReader = new BufferedReader( new InputStreamReader( context.getResources().openRawResource(R.raw.eqtags) ) );
		
		/*try {
			genreClassifier = new GenreClassifier(context.getResources());
		} catch (IOException e) {
		     	// TODO Auto-generated catch block
		     e.printStackTrace();
		}*/
	}
	 
	private void setUpMediaPlayer(){
		mediaPlayer = new MediaPlayer();
		Song currentSong = playlist.getCurrSong();
		try {
			if(currentSong != null){ 
				mediaPlayer.setDataSource(currentSong.data);
				mediaPlayer.prepare();
				mediaPlayer.start();
				mediaPlayer.pause();
				eqManager.setPlay(currentSong,mediaPlayer.getAudioSessionId());
			}
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mediaPlayer.setOnCompletionListener(musiccomplete);
	}
	
	public int getVol(){
		int val = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		if(val > 90){
			am.setStreamVolume(AudioManager.STREAM_MUSIC,90,0);
			return 90;
		}
		return val;
	}
	
	public void setVol(int val){
		am.setStreamVolume(AudioManager.STREAM_MUSIC,val,0);
	}
	
	public int getMaxVol(){
		return am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}
	
	public boolean isEQOk(){
		return eqManager.isEQOk();
	}
	
    private OnCompletionListener musiccomplete = new OnCompletionListener(){
    	public void onCompletion (MediaPlayer media){
    		if(!media.isLooping())
    			playNext();
    	}
    };
	
    public void release(){
    	eqManager.release();
    }
    
	public Song getCurrSong(){
        Song tmp_song = playlist.getCurrSong();;
/*
        if (tmp_song == null) {
            tmp_song = new Song("null");
        }
*/
		return tmp_song;
	}
    
	public boolean playNext(){
		Song song = playlist.getNextSong();
		try {
			if(song != null){
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.setDataSource(song.data);
				mediaPlayer.prepare();
				mediaPlayer.start();
				eqManager.setPlay(song,mediaPlayer.getAudioSessionId());
				return true;
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public int toggleRepeat(){
		repeatStatus = (repeatStatus + 1)%3;
		
		if(repeatStatus == 0){
			mediaPlayer.setLooping(false);
			playlist.setRepeat(false);
		}else if(repeatStatus == 1){
			mediaPlayer.setLooping(false);
			playlist.setRepeat(true);
		}
		else if(repeatStatus == 2){
			mediaPlayer.setLooping(true);
		}
		return repeatStatus;
	}
	
	public boolean togglePlay(){
		if(mediaPlayer.isPlaying()){
			mediaPlayer.pause();
			return false;
		}
		else{
			mediaPlayer.start();
			return true;
		}
	}
	
	public boolean toggleRandom(){
		return playlist.toggleRandom();
	}
	
	
	public boolean playPrev(){
		Song song = playlist.getPrevSong();

		try {
            if (song != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.data);
                mediaPlayer.prepare();
                mediaPlayer.start();
                eqManager.setPlay(song, mediaPlayer.getAudioSessionId());
                return true;
            }
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void playIndex(int index){
		Song song = playlist.getSongIndex(index);
		mediaPlayer.stop();
		mediaPlayer.reset();
		try {
			mediaPlayer.setDataSource(song.data);
			mediaPlayer.prepare();
			mediaPlayer.start();
			eqManager.setPlay(song,mediaPlayer.getAudioSessionId());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Playlist getCurrentPlaylist(){
		return playlist;
	}
	
	public void setCurrentPlaylist(Playlist playlist){
		this.playlist = playlist;
	}
	
	public String getCurrentTime(){
		int now = mediaPlayer.getCurrentPosition()/1000;
		int h = now/3600;
		int m = (now%3600)/60;
		int s = now%60;
		if(h == 0)
			return String.format("%02d:%02d", m,s);
		else {
			return String.format("%d:%02d:%02d", h,m,s);
		}
	}
	
	public void seekTo(int t){
		mediaPlayer.seekTo(t);
	}
	
	public int getCurrent(){
		return mediaPlayer.getCurrentPosition();
	}
	
	public boolean isPlaying(){
		return mediaPlayer.isPlaying();
	}
	
	public void destory(){
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
	}

    public String getCurrSongLengthStr() {
        int duration = mediaPlayer.getDuration();

        int d = duration / 1000;
        int h = d / 3600;
        int m = (d % 3600) / 60;
        int s = d % 60;
        if (h == 0)
            return String.format("%02d:%02d", m, s);
        else {
            return String.format("%d:%02d:%02d", h, m, s);
        }
    }

    public int getCurrSongLength() {
        return mediaPlayer.getDuration();
    }

	public boolean toggleEQ(){
		return eqManager.toggleEQ();
	}
	public int getDuration(){
		return mediaPlayer.getDuration();
	}
	public boolean isEQOn(){
		return eqManager.isEQOn();
	}
}
