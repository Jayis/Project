package com.sagax.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.util.Log;


public class EQManager {

    private Song curSong;

	private Equalizer equalizer;
	private BassBoost booster;
	private boolean eqTog,eqOn;
	private Map<String,int[]> eqTags;
	private ArrayList<String> genreTags;

	private Context context;

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private SharedPreferences sharedPref;
    private String songTable;

	
	public EQManager(Context context){
		BufferedReader tagReader;
        this.context=context;
        Resources appRes=context.getResources();

		tagReader = new BufferedReader( new InputStreamReader(appRes.openRawResource(R.raw.eqtags) ));
		try{
			setTagsReader(tagReader);
		}catch(IOException e){
			e.printStackTrace();
		}

        database = LoginMainActivity.shareDB();
        dbHelper = LoginMainActivity.shareDBHelper();
        sharedPref = LoginMainActivity.shareSharePref();
        songTable = sharedPref.getString("songTable", null);
	}
	
	public void setTagsReader(BufferedReader reader) throws IOException
	{
		genreTags = new ArrayList<String>();
		eqTags = new HashMap<String,int[]>();
		String line = reader.readLine();
		String tag = null;
		while( line != null )
		{
			StringTokenizer st = new StringTokenizer(line,"\t\n\r\f");
			while( st.hasMoreTokens() )
			{
				// if it's a string , it's tag and read it and break to read its eq value set
				if( st.countTokens() == 1 )
				{
					tag = st.nextToken();
					genreTags.add(tag);
					break;
				}
				// this is for eq values
				int[] eqValues = new int[6];
				for(int i=0; st.hasMoreTokens(); i++)
				{
					eqValues[i] = Integer.valueOf(st.nextToken());
				}
				eqTags.put(tag,eqValues);
				Log.d("eqtags",String.valueOf(eqTags.get(tag)[0]));
			}

			line = reader.readLine();
		}
		reader.close();
	}

	public boolean setPlay(Song song,int sessionId){
		curSong = song;
		if(equalizer != null)
			equalizer.release();
		equalizer = new Equalizer(0,sessionId);
		booster=new BassBoost(0,sessionId);
		eqTog=false;
        // see last eqOn
        if (curSong.eqon == 1) {
            eqOn = true;
        }
        else {
            eqOn = false;
        }

		setEQLevel();
		return eqOn;
	}
	public boolean toggleEQ(){
        ContentValues cv = new ContentValues();

		Log.d("eqTog",String.valueOf(eqTog));
		Log.d("eqs",String.valueOf(equalizer.getBandLevelRange()[0]));
		Log.d("eqs",String.valueOf(equalizer.getBandLevelRange()[1]));
		if(eqTog){
			if(eqOn){
				eqOn=false;
				equalizer.setEnabled(eqOn);
				booster.setEnabled(eqOn);

                curSong.eqon = 0;
			}else{
				eqOn=true;
				equalizer.setEnabled(eqOn);
				booster.setEnabled(eqOn);

                curSong.eqon = 1;
			}
            // save eqon to database
            cv.put(DBHelper.COLUMN_EQON, curSong.eqon);
            dbHelper.updateSongTableByServerID(database, songTable, Integer.valueOf(curSong.id), cv);

				Log.d("seteq",String.valueOf(equalizer.getBandLevel((short)0)));
				Log.d("seteq",String.valueOf(equalizer.getBandLevel((short)1)));
				Log.d("seteq",String.valueOf(equalizer.getBandLevel((short)2)));
				Log.d("seteq",String.valueOf(equalizer.getBandLevel((short)3)));
				Log.d("seteq",String.valueOf(equalizer.getBandLevel((short)4)));
		}
		return eqOn;
	}
	
	private void setEQLevel(){
		if(!eqTog){
			String genre = getGenre(curSong);

			if(genre != null){
				int[] eqset=eqTags.get(genre);
                if (eqset == null) {
                    eqset=eqTags.get("Blues");
                }
                Log.d("eqset", String.valueOf(eqset));
				for(int i=0;i<eqset.length-1;i++){
					Log.d("settags",String.valueOf(eqset[i]-1500));
					equalizer.setBandLevel((short)i,(short)(eqset[i]-1500));
				}
				booster.setStrength((short)eqset[eqset.length-1]);
				eqTog=true;
				equalizer.setEnabled(eqOn);
				booster.setEnabled(eqOn);
			}
		}
	}
	private String getGenre(Song s){
        return s.genre;
	}
	
	public boolean isEQOn(){
		return eqOn;
	}
	public boolean isEQOk(){
		return eqTog;
	}

	public void release(){
		if(equalizer != null)
			equalizer.release();
		if(booster != null)
			booster.release();
	}

}
