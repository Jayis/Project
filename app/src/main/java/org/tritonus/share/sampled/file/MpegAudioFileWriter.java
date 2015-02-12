/*
 *	MpegAudioFileWriter.java
 */

/*
 *  Copyright (c) 2000 by Florian Bomers <florian@bome.com>
 *
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */


package	org.tritonus.share.sampled.file;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import lib.sound.sampled.AudioFileFormat;
import lib.sound.sampled.AudioFormat;
import lib.sound.sampled.AudioSystem;

import org.tritonus.sampled.AudioFileTypes;
import org.tritonus.sampled.Encodings;

/**
 * Class for writing mpeg files
 *
 * @author Florian Bomers
 */
public class MpegAudioFileWriter extends TAudioFileWriter {

	private static final AudioFileFormat.Type[]	FILE_TYPES =
	    {
		    AudioFileTypes.getType("MPEG", "mpeg"),
	        // workaround for the fixed extension problem in AudioFileFormat.Type
	        // see org.tritonus.sampled.AudioFileTypes.java
		    AudioFileTypes.getType("MP3", "mp3")
	    };

	private static final int ALL=AudioSystem.NOT_SPECIFIED;
	public static AudioFormat.Encoding MPEG1L3=Encodings.getEncoding("MPEG1L3");

	private static final AudioFormat[]	AUDIO_FORMATS =
	    {
	        new AudioFormat(MPEG1L3, 44100, 16, 1, ALL, ALL, false),
	        new AudioFormat(MPEG1L3, 44100, 16, 1, ALL, ALL, true),
	        new AudioFormat(MPEG1L3, 44100, 16, 2, ALL, ALL, false),
	        new AudioFormat(MPEG1L3, 44100, 16, 2, ALL, ALL, true),
	    };

	public MpegAudioFileWriter() {
		super(Arrays.asList(FILE_TYPES),
		      Arrays.asList(AUDIO_FORMATS));
	}


	protected AudioOutputStream getAudioOutputStream(
	    AudioFormat audioFormat,
	    long lLengthInBytes,
	    AudioFileFormat.Type fileType,
	    File file)
	throws	IOException {
		TDataOutputStream	dataOutputStream = new SeekableTDOS(file);
		return new HeaderLessAudioOutputStream(audioFormat,
		                                       lLengthInBytes,
		                                       dataOutputStream);
	}

	protected AudioOutputStream getAudioOutputStream(
	    AudioFormat audioFormat,
	    long lLengthInBytes,
	    AudioFileFormat.Type fileType,
	    OutputStream outputStream)
	throws	IOException {
		TDataOutputStream	dataOutputStream = new NonSeekableTDOS(outputStream);
		return new HeaderLessAudioOutputStream(audioFormat,
		                                       lLengthInBytes,
		                                       dataOutputStream);
	}

}

/*** MpegAudioFileWriter.java ***/
