package lib.comirva;

import java.io.IOException;
import java.util.Vector;

import lib.comirva.audio.FFT;
import lib.comirva.audio.Matrix;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
//import lib.comirva.audio.FFT;
//import lib.comirva.audio.Matrix;

public class SSD implements Feature
{
	//general fields
	  protected int windowSize;
	  protected int hopSize;
	  protected float sampleRate;
	  protected double baseFreq;

	  //fields concerning the mel filter banks
	  protected double minFreq;
	  protected double maxFreq;
	  protected int numberFilters;

	  //fields concerning the MFCCs settings
	  protected int numberCoefficients;
	  protected boolean useFirstCoefficient;

	  //implementation details
	  private double[] inputData;
	  private double[] buffer;
	  //constructors
	  
	  public SSD(float sampleRate) throws IllegalArgumentException
	  {
	    this(sampleRate, 512, 20.0, 16000.0, 40);
	  }
	  
	  public SSD(float sampleRate, int windowSize) throws IllegalArgumentException
	  {
	    this(sampleRate, windowSize, 20.0, 16000.0, 40);
	  }
	  
	  public SSD(float sampleRate, int windowSize, double minFreq, double maxFreq, int numberFilters) throws IllegalArgumentException
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

	    //check numberFilters
	    if(numberFilters < 2 || numberFilters > (windowSize/2) + 1)
	      throw new IllegalArgumentException("number filters must be at least 2 and smaller than the nyquist frequency");

	    //check minFreq/maxFreq
	    if(minFreq <= 0 || minFreq > maxFreq || maxFreq > 88200.0f)
	      throw new IllegalArgumentException("the min. frequency must be greater 0 smaller than the max. frequency, which must be smaller than 88200.0");;

	    this.sampleRate = sampleRate;
	    this.windowSize = windowSize;
	    this.hopSize = windowSize/2; //50% Overleap
	    this.baseFreq = sampleRate/windowSize;
	    this.minFreq = minFreq;
	    this.maxFreq = maxFreq;
	    this.numberFilters = numberFilters;

	    //create buffers
	    inputData = new double[windowSize];
	    buffer = new double[windowSize];
	  }

	  public Vector process(AudioPreProcessor in) throws IllegalArgumentException, IOException
	  {
	    //check in
	    if(in == null)
	      throw new IllegalArgumentException("the audio preprocessor must not be a null value");

	    //check for correct input format
	    if(in.getSampleRate() != sampleRate)
	        throw new IllegalArgumentException("sample rates of inputstream differs from sample rate of the sone processor");

	    //create vector holding the mfcc vector of each frame
	    Vector ssd = new Vector();

	    //read first frame
	    int samplesRead = in.append(inputData, hopSize, hopSize);

	    //process all other frames
	    while (samplesRead == hopSize)
	    {
	      //move data in window (overleap)
	      for (int i = hopSize, j = 0; i < windowSize; j++, i++)
	        inputData[j] = inputData[i];

	      //read new data
	      samplesRead = in.append(inputData, hopSize, hopSize);

	      //process the current window
	      double[] kk = processWindow(inputData, 0);
	      if((kk[0] != Double.NaN)&&(kk[1] != Double.NaN)&&(kk[2]!= Double.NaN)&&(kk[3]!=Double.NaN))
	    	  ssd.add(processWindow(inputData, 0));
	    }

	    return ssd;
	  }

	  public double[] processWindow(double[] window, int start) throws IllegalArgumentException{
		  double[] ssds = new double[4];
		  ssds[0] = new Mean().evaluate(window);
	      ssds[1] = new Variance().evaluate(window);
	      ssds[2] = new Skewness().evaluate(window);
	      ssds[3] = new Kurtosis().evaluate(window);
	     
	      return ssds;
	  }
	  public int getWindowSize(){
		  return windowSize;
	  }
}



