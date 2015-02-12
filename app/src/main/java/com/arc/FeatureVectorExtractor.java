package com.arc;


import java.io.File;
import java.io.IOException;

import lib.comirva.AudioFeatureExtractor;
import lib.comirva.TimbreDistributionExtractor;
import lib.comirva.audio.AudioFeature;
import lib.comirva.audio.PointList;

/*
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
*/


import lib.sound.sampled.UnsupportedAudioFileException;
import lib.sound.sampled.AudioInputStream;
import lib.sound.sampled.AudioSystem;


public interface FeatureVectorExtractor{
    public void calculate(String filename) throws IOException,UnsupportedAudioFileException;
    public double[] getFeature();
}
