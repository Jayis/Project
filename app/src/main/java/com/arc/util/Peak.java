package com.arc.util;

import java.util.Arrays;

public class Peak{
	double alpha;
	public Peak(double alpha){
		this.alpha=alpha;
	}
	public double evaluate(double[] powers){
		//System.out.println(powers[0]+","+powers[1]+","+powers[powers.length-1]);
		int neighbor = (int)Math.floor(alpha*powers.length);
		Arrays.sort(powers);
		//System.out.println(neighbor+","+powers.length);
		double peak=0;
		for (int t=0;t<neighbor;t++){
			peak += powers[powers.length-1-t];
		}
		return (peak/neighbor);
	}
}