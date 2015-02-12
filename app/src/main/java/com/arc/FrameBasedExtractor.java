package com.arc;

import java.io.File;
import java.io.IOException;

import lib.comirva.AudioFeatureExtractor;
import lib.comirva.TimbreDistributionExtractor;
import lib.comirva.audio.AudioFeature;
import lib.comirva.audio.PointList;

/*
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
*/

import lib.sound.sampled.AudioFormat;
import lib.sound.sampled.AudioInputStream;
import lib.sound.sampled.AudioSystem;
import lib.sound.sampled.UnsupportedAudioFileException;


public abstract class FrameBasedExtractor implements FeatureVectorExtractor{
	protected AudioFeatureExtractor featureExtractor;
	protected PointList pointList;
	protected double[] featureVector;
    protected String filename;
	
	public FrameBasedExtractor(String frameMethod){
		featureExtractor = new TimbreDistributionExtractor(frameMethod);
	}
	public FrameBasedExtractor(String frameMethod,String filename){
		featureExtractor = new TimbreDistributionExtractor(frameMethod,filename);
        this.filename=filename;
	}
    public void calculate() throws IOException,UnsupportedAudioFileException{
        calculate(this.filename);
    }
    public void calculate(String filename) throws IOException,UnsupportedAudioFileException{
    	File file = new File(filename);
        AudioInputStream ais ;

        ais= AudioSystem.getAudioInputStream(file);
    	AudioFeature af = (AudioFeature)featureExtractor.calculate(ais);

    	pointList = featureExtractor.getMFCC();
    	analyze();

    	
    }
    public double[] getFeature(){
    	return featureVector;
    }
    protected abstract void analyze();
}
