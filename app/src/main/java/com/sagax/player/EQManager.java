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

import com.arc.GenreClassifier;
import com.sagax.player.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.AsyncTask;
import android.util.Log;


public class EQManager {
	private Equalizer equalizer;
	private BassBoost booster;
	private boolean eqTog,eqOn;
	private GenreClassifier genreClassifier; 
	private Map<String,int[]> eqTags;
	private ArrayList<String> genreTags;
	private Stack<String> queue;
	private String filename;
	private Context context;
	private DBHelper DH;
	private EQProcess eqProcess;
	private String currentFile;
	private final static String TABLE ="genreDB";
	
	public EQManager(Context context){
		BufferedReader tagReader;
		BufferedReader modelReader;
			this.context=context;
			Resources appRes=context.getResources();
		
			tagReader = new BufferedReader( new InputStreamReader(appRes.openRawResource(R.raw.eqtags) ));
		try{
			setTagsReader(tagReader);
		}catch(IOException e){
			e.printStackTrace();
		}
		try{
			tagReader = new BufferedReader( new InputStreamReader(appRes.openRawResource(R.raw.eqtags) ));
			modelReader = new BufferedReader(new InputStreamReader(appRes.openRawResource(R.raw.svm)));
			genreClassifier= new GenreClassifier(context.getResources());
			//genreClassifier.setTagsReader(tagReader);
			Log.d("psuc","aa");
			if(genreClassifier==null)
				Log.d("psuc","adsfadsf");
			
		}catch(IOException e){
			Log.d("pfail",e.toString());
		}

		queue=new Stack<String>();
		DH = new DBHelper(context);
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
	
	public void kick(){
		boolean flag=false;
		//boolean skip=true;
		if(queue.isEmpty())
			return;
		do{
			if(currentFile==null||getGenre(currentFile)!=null){
				currentFile=queue.pop();
				//Log.d("arcqueue","next song "+ currentFile);
			}else{
				String qq = String.valueOf(queue.size());
				/*if(qq=="0")
					Log.d("arcqueue", currentFile+" | "+qq);
				else
					Log.d("arcqueue",currentFile+" | "+qq+" , "+queue.peek());*/
				flag=true;
				break;
			}
		}while(!queue.isEmpty());
		if(flag==true){
			
			eqProcess=new EQProcess();
			eqProcess.execute(this);
		}
	}
	public boolean setPlay(Song song,int sessionId){
		this.filename=song.data;
		if(equalizer != null)
			equalizer.release();
		equalizer = new Equalizer(0,sessionId);
		booster=new BassBoost(0,sessionId);
		eqTog=false;
		eqOn=false;
		setEQLevel();
		return eqOn;
	}
	public boolean toggleEQ(){
		ContentValues cv = new ContentValues();
		SQLiteDatabase db= DH.getWritableDatabase();		
		Log.d("eqTog",String.valueOf(eqTog));
		Log.d("eqs",String.valueOf(equalizer.getBandLevelRange()[0]));
		Log.d("eqs",String.valueOf(equalizer.getBandLevelRange()[1]));
		if(eqTog){
			if(eqOn){
				eqOn=false;
				equalizer.setEnabled(eqOn);
				booster.setEnabled(eqOn);
				cv.put("eqon",0);
			}else{
				eqOn=true;
				equalizer.setEnabled(eqOn);
				booster.setEnabled(eqOn);
				cv.put("eqon",1);
			}
			db.update(TABLE,cv," filePath=\""+currentFile+"\"",null);
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
			String genre = getGenre(filename);
			short levelShort;
			if(genre != null){
				int[] eqset=eqTags.get(genre);
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
	private String getGenre(String filename){
		String result = null;
		
		SQLiteDatabase db = DH.getReadableDatabase();
		String[] columns={"genre"};
		String[] paths={filename};
		//Log.d("inininin",filename);
		Cursor cursor = db.rawQuery("select * from "+ TABLE +" where filePath = \""+ filename+ "\" ",null);
		
		
		//Log.d("inininin",String.valueOf(cursor.moveToFirst()));
		if(cursor.moveToFirst()){
			result=cursor.getString(cursor.getColumnIndex("genre"));
			int on =cursor.getInt(cursor.getColumnIndex("eqon"));
			//Log.d("eqon",String.valueOf(on));
			if(on==1){
				eqOn=true;
				//Log.d("eqon",String.valueOf(eqOn));
			}
			//Log.d("found genre",result+","+filename);
		}
		/*else if (queue.isEmpty()){
			queue.add(filename);
			//eqProcess.execute(this);
		}*/
		else{
			try{
				queue.remove(filename);
			}catch (Exception e){Log.d("arcqueue",filename+" not in queue");}
			queue.push(filename);
		}
		cursor.close();
		//Log.d("arcqueue", currentFile+ " | " +queue.toString());
		String qq = String.valueOf(queue.size());
		/*if(queue.isEmpty())
			Log.d("arcqueue", currentFile+" | "+qq);
		else
			Log.d("arcqueue",currentFile+" | "+qq+" , "+queue.peek());*/
		return result;
	}
	public void doCalc(String filename){
		//cancel current task
		if(eqProcess!=null){
			if(eqProcess.getStatus()==AsyncTask.Status.RUNNING){
				eqProcess.cancel(true);
				//Log.d("queueadd",currentFile);
			}
			queue.add(currentFile);
		}
		//run new task
		this.currentFile=filename;
		Log.d("calccccc",filename);
		eqProcess=new EQProcess();
		eqProcess.execute(this);
	}
	
	public boolean isEQOn(){
		return eqOn;
	}
	public boolean isEQOk(){
		return eqTog;
	}
	public void addList(List<Song> songs){
		for(Song s:songs){
			queue.push(s.data);
		}
	}
	
	public void release(){
		if(equalizer != null)
			equalizer.release();
		if(booster != null)
			booster.release();
	}
	
	private class EQProcess extends AsyncTask<EQManager, Void, Void>{
		EQManager em;
		String genre;
		String filePath;
		@Override
		protected Void doInBackground(EQManager...em) {
			this.em=em[0];
			// TODO Auto-generated method stub
			filePath=this.em.currentFile;
			try{
				/*
				Thread t = new Thread(new predictG(filePath));
				t.start();
				while(t.isAlive()){
					if(isCancelled()){
						Log.d("calc canc", filePath);
						t.stop();
					}
				}*/
				Log.d("calculating",filePath);
				//Thread.sleep(3000);
				genre="Blues";
                /*
				try{
					genre=genreClassifier.predictGenre(filePath);
				}catch(Exception e){Log.d("calcerr",e.toString());}
				*/
				//Log.d("done calc",filePath);
				//if(!isCancelled()){
					SQLiteDatabase db= DH.getWritableDatabase();
					ContentValues values=new ContentValues();
					values.put("filePath", filePath);
					values.put("genre", genre);
					values.put("eqon", 1);
					//Log.d("db insert start",filePath);
					long ID = db.insert(TABLE, null, values);
					//Log.d("db insert end",ID + "," +genre+","+filePath);
					Log.d("status.same",String.valueOf(filePath.equals(this.em.currentFile)));
					if(filePath.equals(this.em.currentFile))
						this.em.setEQLevel();
					this.em.kick();
				//}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
		
		protected void onPostExecute(double[] eqTags) {
			//musicManager.setEQ(eqTags);
		}
		class predictG implements Runnable{
			String filePath;
			public predictG(String filePath){
				this.filePath=filePath;
			}
			public void run(){
				try{
					Log.d("calculating",filePath);
					genre=genreClassifier.predictGenre(filePath);
				}catch(Exception e){
					Log.e("calc err",e.toString());
					e.printStackTrace();
				}
			}
		}
	}
}
