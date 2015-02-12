package com.arc;


import org.apache.commons.math3.stat.descriptive.moment.Mean;

import com.arc.util.Peak;
import com.arc.util.Valley;

public class SimpleExtractor extends FrameBasedExtractor{
	public SimpleExtractor(){
		this("MFCC");
	}
	public SimpleExtractor(String method){
		super(method);
	}
    public SimpleExtractor(String method,String filename){
        super(method,filename);
    }
	protected void analyze(){
		int n=pointList.get(0).getRowDimension();
		int m=pointList.size();
		double[][] featureMat = new double[n][m];
        for (int i = 0; i < pointList.size(); i++) {
            double[] point = pointList.get(i).getRowPackedCopy();
            for (int j = 0; j < point.length; j++) {
            	featureMat[j][i] =point[j];
                }
        }
        featureVector= new double[3*n];
        for(int j=0;j<n;j++){
            /*
            double[] delta=new double[m-1];
            for(int i=1;i<m;i++){
                delta[i-1]=featureMat[j][i]-featureMat[j][i-1];
            }
            double[] deltadelta=new double[m-2];
            for(int i=1;i<m-1;i++){
                deltadelta[i-1]=delta[i]-delta[i-1];
            }
            */
            double[] delta=deltaFunc(featureMat[j],2);
            double[] deltadelta=deltaFunc(delta,2);

        	featureVector[j]=new Mean().evaluate(featureMat[j]);
            featureVector[n+j]=new Mean().evaluate(delta);
            featureVector[2*n+j]=new Mean().evaluate(deltadelta);
        }
	}
    private double[] deltaFunc(double[] array,int m){
        double[] result = new double[array.length-2*m];
        for(int i=m,j=0;i<array.length-m;i++,j++){
            int deno=0;
            for(int k=-m;k<=m;k++){
                result[j]+=array[i+k]*k;
                deno += k^2;
            }
            result[j]/=deno;
        }
        return result;
    }

}
