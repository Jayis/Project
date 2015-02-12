package com.arc;

import java.util.ArrayList;
import java.util.List;

import lib.comirva.audio.FFT;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.arc.util.Peak;
import com.arc.util.Valley;

public class MSAExtractor extends FrameBasedExtractor{
	int modSubband;
	public MSAExtractor(){
		this("MFCC",4);
	}
	public MSAExtractor(String method){
		this(method,4);
	}
	public MSAExtractor(String method,int modSubband){
		super(method);
		this.modSubband=modSubband;
	}
	protected void analyze(){
		int n=pointList.get(0).getRowDimension();
		int m=pointList.size();
		m =(int)Math.pow(2,Math.floor(Math.log(m)/Math.log(2)));
		System.out.println(m);
		int textureWin=256;
		int times = m/textureWin;
		double[][] transpose=new double[n][textureWin];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				transpose[j][i%textureWin] += pointList.get(i).get(j, 0)/times;
			}
		}
				
		FFT normalizedPowerFFT = new FFT(FFT.FFT_NORMALIZED_POWER, textureWin, FFT.WND_HANNING);
		for(int i=0;i<n;i++){
			normalizedPowerFFT.transform(transpose[i], null);
		}
		
		int[][] modFilter = new int[modSubband][2];
		int mrest=textureWin;
		for(int i=1;i<=modSubband;i++){
			modFilter[modSubband-i][1]=mrest-1;
			mrest /= 2;
			modFilter[modSubband-i][0]=mrest-1;
		}
		modFilter[0][0]=0;
		
		double[][] msapMat=new double[n][modSubband];
		double[][] msavMat=new double[n][modSubband];
		double[][] msacMat=new double[n][modSubband];
		for(int i=0;i<modSubband;i++){
			for(int j=0;j<n;j++){
				List<Double> list = new ArrayList<Double>();
				for(int k=modFilter[i][0];k<modFilter[i][1];k++){
					list.add(transpose[j][k]);
				}
				double[] powers = new double[list.size()];
				for(int a=0;a<list.size();a++){
					powers[a]=list.get(a);
				}
				double peak=new Peak(0.2).evaluate(powers);
				double valley=new Valley(0.2).evaluate(powers);
				
				msapMat[j][i]=Math.log(peak);
				msavMat[j][i]=Math.log(valley);
				msacMat[j][i]=msapMat[j][i]-msavMat[j][i];
			}
		}
		List<Double> list=new ArrayList<Double>();
		for(int j=0;j<n;j++){
			list.add(new Mean().evaluate(msavMat[j]));
			list.add(new StandardDeviation().evaluate(msavMat[j]));
			list.add(new Mean().evaluate(msacMat[j]));
			list.add(new StandardDeviation().evaluate(msacMat[j]));
		}
		
		for(int i=0;i<modSubband;i++){
			double[] arrayv= new double[n];
			double[] arrayc= new double[n];
			for(int j=0;j<n;j++){
				arrayv[j]=msavMat[j][i];
				arrayc[j]=msacMat[j][i];
			}
			list.add(new Mean().evaluate(arrayv));
			list.add(new StandardDeviation().evaluate(arrayv));
			list.add(new Mean().evaluate(arrayc));
			list.add(new StandardDeviation().evaluate(arrayc));
		}
		System.out.println(list.size()+","+n+","+modSubband);
		featureVector=new double[4*(n+modSubband)];
		for(int i=0;i<featureVector.length;i++){
			featureVector[i]=list.get(i);
		}
	}
}