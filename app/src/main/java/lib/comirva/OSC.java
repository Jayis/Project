package lib.comirva;


import lib.comirva.audio.FFT;
import lib.comirva.audio.Matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

/**
 * <b> Octave Based Spectral Contrast (OSC) </b>
 * <p>Description</p>
 * This class is the implementation of OSC and used to calculate the frequency of the input signal.
 * The signal is cut into several frames and being transformed by FFT.
 * Each transformed frame will be apply with a octave-based filter which will cut the frame into six sub-band 
 * each sub-band will calculate the peak and valley of the signal, and finally return a array with sub-band
 * KL_transform ( {Difference between peak and valley(1~6), valley(1~6)} )
 * 
 * @author JianZhang Chen
 *
 */



public class OSC
{
	// constant value
	private final int initHZ = 200;
	private final double alpha = 0.02; 
	
	// initial parameters
	private float sampleRate;
	private int windowSize;
	private int hopSize;
	private int numberOfSubBand;
	
	//implementation details
	private double[] inputData;
	private double[] buffer;
	private Matrix KT_Tranform;
	private FFT normalizedPowerFFT;
	private double frequencyInterval;
	
	
	public OSC(float sampleRate){
		this(sampleRate,4096,6);
	}

	/***
	 * Constructor of OSC 
	 * 
	 * @param sampleRate - the sample rate of the audio input stream 
	 * @param windowSize - the frame size 
	 * @param numberOfSubBand - number of the sub-band filter
	 * @throws IllegalArgumentException
	 */
	
	public OSC(float sampleRate, int windowSize, int numberOfSubBand) throws IllegalArgumentException
	{
	  
		
	    //check for correct window size
	    if(windowSize < 32)
	    {
	        throw new IllegalArgumentException("window size must be at least 32");
	    }
	    else
	    {
	        int i = 32;
	        while(i < windowSize && i < Integer.MAX_VALUE)
	          i = i << 1;

	        if(i != windowSize)
	            throw new IllegalArgumentException("window size must be 2^n");
	    }
	    

	    //check sample rate
	    sampleRate = Math.round(sampleRate);
	    if(sampleRate < 1)
	      throw new IllegalArgumentException("sample rate must be at least 1");

	    if(numberOfSubBand <=0 )
	      throw new IllegalArgumentException("number of sub-band cannot be 0");
	    
	    
	   

	    this.sampleRate = sampleRate;
	    this.windowSize = windowSize;
	    this.hopSize = windowSize/2; //50% Overleap
	    this.numberOfSubBand = numberOfSubBand;
	    this.frequencyInterval = sampleRate / windowSize;
	    
	    //create power fft object
	    normalizedPowerFFT = new FFT(FFT.FFT_NORMALIZED_POWER, windowSize, FFT.WND_HANNING);

	 
	 
	 }

	/**
	 * Main program of this OSC processor, it will need AudioPreProcessor to transform the audio file
	 * and to calculate the OSC    
	 * It will throw a IllegalArgumentExcetpion if the input audio is a null value and 
	 * the sample rate of the input stream should be the same with the constructor
	 * 
	 * @param in - the audio input stream, which have been processed  
	 * @return double[] - a array with every frame which is the result of OSC  
	 * @throws IllegalArgumentException - if the audio preprocessor is null value or different sample rate indicate by the constructor
	 * @throws IOException - IOException if there are any problems regarding the input stream
	 */
	
	public double[] process(AudioPreProcessor in) throws IllegalArgumentException, IOException
	{
		
		//check in
	    if(in == null)
	      throw new IllegalArgumentException("the audio preprocessor must not be a null value");

	    //check for correct input format
	    if(in.getSampleRate() != sampleRate)
	        throw new IllegalArgumentException("sample rates of inputstream differs from sample rate of the sone processor");

		
		
		double[] buffer = new double[windowSize];
		
		
		double time = System.currentTimeMillis() ;
		int sampleSize = in.append(buffer, hopSize, hopSize);
	
		
		int numberOfFrame = in.frameSize % windowSize , frameCounter = 0;
		
		Vector osc = new Vector();
		
		while( sampleSize == hopSize )
		{
			// making overlapping of buffer
			for(int i=hopSize,j=0; i < buffer.length; i++, j++)
			{
				buffer[j] = buffer[i]; 
			}
			
			sampleSize = in.append(buffer, hopSize, hopSize);
			
			double[] temp = new double[buffer.length];
			temp = processWindow(buffer);
			if( temp == null)
				continue;
			
			osc.add(temp);
			frameCounter++;
		}
		
		System.out.println("OSC size:"+osc.size());
		time = System.currentTimeMillis() - time;
		Iterator iterator = osc.iterator();
		double [][] matrixArray = new double[osc.size()][];
		int i = 0;
		while( iterator.hasNext() ){
			// assign from vector to 2d array
			matrixArray[i] = (double[])iterator.next();
			/*
			for(double value : matrixArray[i])
				System.out.print(value+" ");
			
			System.out.println(matrixArray.length+" "+i);
			*/
			i++;
			
		}
		
		// put the result to matrix
		Matrix x = new Matrix(matrixArray);
		// apply with Karhunen-Loeve Transform
		x = k_lTransform(x);
		System.out.println("Done in "+time/1000 + "sec Size "+osc.size()+" sampleRate:"+in.getSampleRate()+" rowSize:"+x.getRowPackedCopy().length);
		
		// return the result with each row put into a array
		return x.getRowPackedCopy();
	}
	
	
	/***
	 * this subroutine will process a single frame of the audio input stream
	 * it will apply FFT to the single frame and use octave-based filter to separate
	 * the result into several sub-band and calculate peak and valley of each sub-band,
	 * then return the OSC sub-band 
	 * @param window - single frame of the audio input stream
	 * @return double[] - the array of OSC sub-band with peak - valley and valley
	 */
	
	
	private double[] processWindow(double[] window)
	{
		
		// copy window to buffer 
		// avoid changing the value
		double[] buffer = new double[window.length];
		for(int i=0;i<window.length;i++)
			buffer[i] = window[i];
		
		// create the subBand array
		double[] subBand = new double[numberOfSubBand*2];
		
		// do FFT 
		normalizedPowerFFT.transform(buffer, null);
		
		// performing the octave scale filter		
		for(int i=0; i < numberOfSubBand ; i++){
			
			// sub-band filter's index 
			int start = 0; 
			int end = (int)( (initHZ * Math.pow(2,i)) / frequencyInterval );
			
			// if the last interval isn't enough to cover the rest of buffer
			// assign the last index of the buffer to the filter 
			if( i == numberOfSubBand - 1 && end < buffer.length-1 )
				end = buffer.length-1;				
			
			
			// store valley to array from the half of the array to the end
			subBand[i] = getValley(buffer,start,end);
			// store SC to array from 0 the half of array
			subBand[i + numberOfSubBand ] = getPeak(buffer,start,end) - subBand[i]; 
			
			
			// assigning the beginning of the next sub-band's index
			start = end+1;
			
			if( subBand[i] == Double.NEGATIVE_INFINITY || subBand[i] == Double.POSITIVE_INFINITY ){
				return null;
			}
			
		}
		 
		
		

	
		
		// sort the array from small to big value
		/*
		for(int i=0, filterIndex = 1 , filterStart = 0; i< buffer.length && filterIndex < numberOfSubBand; i++)
		{
		
		
			if( buffer[i] > initHZ*Math.pow(2.0, filterIndex-1 ) )
			{
				if( i < buffer.length/3 ){
					System.out.println(i);
				}
				
				// store valley to array from the half of the array to the end
				subBand[filterIndex - 1] = getValley(buffer,filterStart,i);
				// store SC to array from 0 the half of array
				subBand[filterIndex + numberOfSubBand - 1] = getPeak(buffer,filterStart,i) - subBand[filterIndex - 1]; 
				
				filterIndex++;
				filterStart = i;
			}
			
		}
		
		*/
		
		return subBand;
	}
	
	
	/**
	 * The method is used to calculate <p>the value of peak</p> of a single sub-band 
	 * @param buffer - the array of being transformed by FFT 
	 * @param start - the start index of a sub-band filter
	 * @param end - the end index of a sub-band filter
	 * @return the value of valley 
	 */
	private double getPeak(double buffer[],int start,int end){
		
		// sort the array with the interval ( in ascending order )
		Arrays.sort(buffer,start,end);
		// calculate the alpha*N (interval length multiply with alpha)
		int length = (int)( alpha * ( end - start ) );
		
		
		// the summation from i = end to ( end - alpha*N )
		double sum = 0;
		for(int i=end; i>= end - length; i--){
			sum += buffer[i];
		}
		
	
		// finally divide with length and return with log10
		return Math.log10( sum / length );
	}
	
	
	/**
	 * The method is used to calculate <p>the value of valley</p> of a single sub-band 
	 * @param buffer - the array of being transformed by FFT 
	 * @param start - the start index of a sub-band filter
	 * @param end - the end index of a sub-band filter
	 * @return the value of valley 
	 */
	
	private double getValley(double buffer[],int start,int end){
		// sort the array with the interval ( in ascending order )
		Arrays.sort(buffer,start,end);
		// calculate the alpha*N (interval length multiply with alpha)
		int length = (int)( alpha * ( end - start ) );
		
		
		 
		// the summation from i = start to ( start + alpha*N )   
		double sum = 0;
		for(int i=start; i<= start + length; i++){
			sum += buffer[i];
		}
				
		// finally divide with length and return with log10
		return Math.log10( sum / length );
	}
	
	
	/***
	 * apply the matrix with Karhunen-Loeve Transform 
	 * @param x - matrix which needs to apply KL transform
	 * @return the matrix with the result of Karhunen-Loeve Transform
	 */
	
	private Matrix k_lTransform(Matrix x)
	{
	 
	   x = x.transpose();
	     
	   int nExample = x.getColumnDimension();

	  //calculate mean
	   Matrix mean = getMean(x);
	  
	   double[][] oneD = new double[1][nExample];
	   for(int i = 0; i < nExample; i++)
	       oneD[0][i] = 1;
	   Matrix ones = new Matrix(oneD);
	  
	   //center the data
	   Matrix xm = x.minus(mean.times(ones)); 

	   //Calculate covariance matrix
	   Matrix cov  = xm.times(xm.transpose());
	  
	   /*
	   In the matlab code, the covariance matrix is divided with N (nExample). 
	   Now cov and cov/nExample have the same eigenvectors but different eigenvalues. 
	   In this code, the division doesn't make any difference as we are only 
	   considering the eigenvectors. 
	   But there are some cases, like in Kaiser-Guttman stopping rule 
	   where only the eigenvectors with eigenvalue > 1 are chosen, 
	   division might make a difference.
	   */
	   cov = cov.times(1.0/nExample);

	   //compute eigen vectors
	   Matrix eigenVectors = cov.eig().getV();

	  //compute pca
	   Matrix pca = eigenVectors.transpose().times(xm);
	   return pca;
	}
	
	/***
	 * calculate the mean of input matrix
	 * @param x - matrix of the 2d array  
	 * @return matrix of mean 
	 */
	
	private Matrix getMean(Matrix x) {
		int nExample = x.getColumnDimension();
		int nFeature = x.getRowDimension();
	  
		double[][] meanD = new double[nFeature][1];
		Matrix mean = new Matrix(meanD);
	  
		for(int i = 0; i < nFeature; i++)
		{
			double avg = 0.0;
			for(int j = 0; j < nExample; j++)
			{
				avg+=x.get( i,j);
			}
	   	 	mean.set(i, 0, avg/nExample);
   	 	}
		return mean;
	}
	
	
}


