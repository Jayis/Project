package lib.comirva;

import java.io.IOException;
import java.util.Vector;

public interface Feature{
	public Vector process(AudioPreProcessor in) throws IllegalArgumentException, IOException;
	public int getWindowSize();
}