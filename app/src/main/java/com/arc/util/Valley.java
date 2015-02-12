package com.arc.util;

import java.util.Arrays;

public class Valley{
	double alpha;
	public Valley(double alpha){
		this.alpha=alpha;
	}
	public double evaluate(double[] powers){
		Arrays.sort(powers);
		//System.out.println(powers[0]+","+powers[1]+","+powers[powers.length-1]);
		int neighbor = (int)Math.floor(alpha*powers.length);
		double valley=0;
		for (int t=0;t<neighbor;t++){
			valley += powers[t];
		}
		return (valley/neighbor);
	}
}