package com.arc;


import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.arc.util.Peak;
import com.arc.util.Valley;

public class ADMMExtractor extends FrameBasedExtractor{
	public ADMMExtractor(){
		this("MFCC");
	}
	public ADMMExtractor(String method){
		super(method);
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
            for(int i=0;i<m;i++){
        	    featureVector[3*j]+=featureMat[j][i];
            }
            featureVector[3*j]/=featureMat[j].length;
        	featureVector[3*j+1]=new StandardDeviation().evaluate(featureMat[j]);
        	featureVector[3*j+2]=new Peak(0.05).evaluate(featureMat[j]);
        	//featureVector[4*j+3]=new Mean().evaluate(featureMat[j]);
        }
	}
}
