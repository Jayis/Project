package com.arc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.arc.util.Peak;
import com.arc.util.Valley;

import lib.comirva.AudioPreProcessor;
import lib.comirva.audio.FFT;
import lib.comirva.audio.Matrix;


import lib.sound.sampled.AudioInputStream;
import lib.sound.sampled.AudioSystem;
import lib.sound.sampled.UnsupportedAudioFileException;


/*
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
*/


public class AMSCVExtractor implements FeatureVectorExtractor{
	protected int windowSize;
	protected int hopSize;
	protected float sampleRate;
	protected double baseFreq;
	protected int acSubband=6;
	protected int modSubband=6;
	protected double alpha=0.2;
	
	protected AudioPreProcessor preProcessor;
	
	protected double[] feature;
	protected Matrix spectro;
	private double[] inputData;
	private double[] buffer;
	private FFT normalizedPowerFFT;
	
	public AMSCVExtractor(){
		this(AudioPreProcessor.DEFAULT_SAMPLE_RATE,1024);
	}
	public AMSCVExtractor(float sampleRate,int windowSize){
		this.sampleRate=sampleRate;
		this.windowSize=windowSize;
		this.hopSize=windowSize/2;
		this.baseFreq=sampleRate/windowSize;
		
		inputData=new double[windowSize];
		buffer=new double[windowSize];
		
		normalizedPowerFFT = new FFT(FFT.FFT_NORMALIZED_POWER, windowSize, FFT.WND_HANNING);
		
	}
	public void calculate(String filename) throws IOException,UnsupportedAudioFileException{
        File file = new File(filename);
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
		preProcessor = new AudioPreProcessor(ais);
		int samplesRead = preProcessor.append(inputData, hopSize, hopSize);
		Vector<double[]> res = new Vector<double[]>();
		
		
		while(samplesRead == hopSize && res.size()<1024){
			////////// needs to be dynamic later
			for(int i = hopSize,j=0;i<windowSize;j++,i++){
				inputData[j]=inputData[i];
			}
			samplesRead = preProcessor.append(inputData, hopSize, hopSize);
			res.add(singleFFT(inputData));
		}
		
		int limit =(int)Math.pow(2,Math.floor(Math.log(res.size())/Math.log(2)));
		//System.out.println(limit);

		
		int n = limit;
		int m = res.get(0).length;
		System.out.println(n+","+m);
		double[][] transpose = new double[m][n];
		for (int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				transpose[j][i]=res.get(i)[j];
			}
		}
		normalizedPowerFFT = new FFT(FFT.FFT_NORMALIZED_POWER, n, FFT.WND_HANNING);
		for (int j=0;j<m;j++){
			normalizedPowerFFT.transform(transpose[j],null);
		}
		
		spectro = new Matrix(transpose);
		spectro.transpose();
		
		int[][] acFilter=new int[acSubband][2];
		int mrest=m;
		for(int i=1;i<=acSubband;i++){
			acFilter[acSubband-i][1]=mrest-1;
			mrest /= 2;
			acFilter[acSubband-i][0]=mrest-1;
		}
		acFilter[0][0]=0;
		int[][] modFilter = new int[modSubband][2];
		int nrest=n;
		for(int i=1;i<=modSubband;i++){
			modFilter[modSubband-i][1]=nrest-1;
			nrest /= 2;
			modFilter[modSubband-i][0]=nrest-1;
		}
		modFilter[0][0]=0;
		double[][] amspMat = new double[acSubband][modSubband];
		double[][] amsvMat = new double[acSubband][modSubband];
		double[][] amscMat = new double[acSubband][modSubband];
		for(int j=0;j<modSubband;j++){
			for(int i=0;i<acSubband;i++){
				List<Double> list = new ArrayList<Double>();
				for(int l=modFilter[j][0];l<modFilter[j][1];l++){
					for(int k=acFilter[i][0];k<acFilter[i][1];k++){
						list.add(spectro.get(k,l));
					}
				}
				double[] powers = new double[list.size()];
				for(int a=0;a<list.size();a++){
					powers[a]=list.get(a);
				}
				/*
				double[] tmp = powers;
				Arrays.sort(powers);
				//System.out.println(powers[0]+","+powers[1]+","+powers[powers.length-1]);
				int neighbor = (int)Math.floor(alpha*powers.length);
				double peak=0,valley=0;
				for (int t=0;t<neighbor;t++){
					valley += powers[t];
					peak += powers[powers.length-1-t];
				}
				powers = tmp;
				*/
				double peak=new Peak(alpha).evaluate(powers);
				double valley=new Valley(alpha).evaluate(powers);
				
				amspMat[i][j] = Math.log(peak);
				amsvMat[i][j] = Math.log(valley);
				amscMat[i][j] = amspMat[i][j] - amsvMat[i][j];
				
				//System.out.print(amspMat[i][j]+","+amsvMat[i][j]+","+amscMat[i][j]);
				//System.out.print("  ");
			}
			//System.out.println("\n=============");
		}
		
		feature = new double[2*acSubband*modSubband];
		for(int i=0;i<modSubband;i++){
			for(int j=0;j<acSubband;j++){
				feature[2*acSubband*i+2*j]=amsvMat[i][j];
				feature[2*acSubband*i+2*j+1]=amscMat[i][j];
			}
		}
	}
	
	public double[] getFeature(){
		return feature;
	}
	
	private double[] singleFFT(double[] window){
		for (int j = 0; j < windowSize; j++)
		      buffer[j] = window[j];
		normalizedPowerFFT.transform(buffer, null);
		return buffer;
	}
	
	
}
