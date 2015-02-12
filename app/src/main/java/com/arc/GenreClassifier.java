package com.arc;



import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import lib.comirva.audio.AudioFeature;
import lib.sound.sampled.AudioInputStream;
import lib.sound.sampled.AudioSystem;
import lib.sound.sampled.UnsupportedAudioFileException;

import android.content.res.Resources;


import java.io.IOException;

import com.arc.ADMMExtractor;
import com.arc.FeatureVectorExtractor;
import com.libsvm.svm_predict;
import com.libsvm.svm_scale;
import com.sagax.player.R;

public class GenreClassifier
{
	private ArrayList<String> genreTags = null;
	private Map<String,double[]> eqTags= null;
	private FeatureVectorExtractor fve ; 
	private String modelPath = "";		
	private BufferedReader modelReader = null;
	private Resources appResource;
	
	
	
	public GenreClassifier(BufferedReader modelReader,BufferedReader tagsReader) throws IOException
	{
		setTagsReader(tagsReader);
		setModelReader(modelReader);
	}
	
	public GenreClassifier(Resources resource) throws IOException
	{
		this.appResource = resource;
		BufferedReader tagReader = new BufferedReader( new InputStreamReader(appResource.openRawResource(com.sagax.player.R.raw.eqtags) ));
		setTagsReader(tagReader);
	}

	public void setTagsReader(BufferedReader reader) throws IOException
	{
		genreTags = new ArrayList<String>();
		eqTags = new HashMap<String,double[]>();
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
				double[] eqValues = new double[6];
				for(int i=0; st.hasMoreTokens(); i++)
				{
					eqValues[i] = Double.valueOf(st.nextToken());
				}
				eqTags.put(tag,eqValues);
				System.out.println(eqTags.get(tag).length);
			}

			line = reader.readLine();
		}

		reader.close();
	}
	public void setModelReader(BufferedReader reader)
	{
		this.modelReader = reader;
	}

	/**
	 * get a copyed music tags string
	 * @return a string array with tags
	 * */
	public ArrayList<String> getTags(){
		if( genreTags != null )
			return (ArrayList<String>)genreTags.clone();
		else
			return null;
	}

	/**
	 * get a single music tag specify by the index
	 * @return a string of music tag
	 * */
	public String getTagByIndex(int tagIndex){
		return this.genreTags.get(tagIndex);
	}


	public double[] getEQSetByTag(String tag){
		return this.eqTags.get(tag);
	}
	/**
	 * give a file path of a music file, it will extract it's feature
	 * and feed it to svm to predict it's genre.
	 * @return a string of this music file's genre
	 **/

	public String predictGenre(String filePath) throws IOException,UnsupportedAudioFileException{
		try{
			fve = new SimpleExtractor(); 
			fve.calculate(filePath);


			// assign arbitrary number for the format.
			String featureString = "0 ";
			// get feature and print as a recognizable format for svm_predict
		    double[] point = fve.getFeature();
        	for (int i = 0; i < point.length; i++) {
            	featureString += ((i+1)+":"+point[i]+" ");
       		}
			//if( modelReader == null){
				modelReader = new BufferedReader( new InputStreamReader( appResource.openRawResource(R.raw.svm)) );
			//}
			// go predict
			String returnTag = svm_predict.goPredict(featureString,modelReader);
			
			double tag = Double.valueOf(returnTag);
			
			return getTagByIndex((int)tag-1);
		}catch(UnsupportedAudioFileException e){
			throw e;
		}
		
		/*
		catch(Exception e){
			throw new Exception(e.toString());
		}*/
		// exception, return a null string.
	}

}
