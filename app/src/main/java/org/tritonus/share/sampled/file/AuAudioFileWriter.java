/*
 *	AuAudioFileWriter.java
 */

/*
 *  Copyright (c) 1999,2000 by Florian Bomers <florian@bome.com>
 *  Copyright (c) 1999 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
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

import org.tritonus.share.sampled.file.AuAudioOutputStream;

/**
 * AudioFileWriter for Sun/Next AU files.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class AuAudioFileWriter extends TAudioFileWriter {

	private static final AudioFileFormat.Type[] FILE_TYPES =
	    {
	        AuTool.AU
	    };

	private static final int ALL=AudioSystem.NOT_SPECIFIED;

	// IMPORTANT: this array depends on the AudioFormat.match() algorithm which takes
	//            AudioSystem.NOT_SPECIFIED into account !
	private static final AudioFormat[]	AUDIO_FORMATS =
	    {
	        // IDEA: allow other number of channels that 1 and 2 ?
	        new AudioFormat(AuTool.PCM, ALL, 8, ALL, ALL, ALL, true),
	        new AudioFormat(AuTool.PCM, ALL, 8, ALL, ALL, ALL, false),

	        new AudioFormat(AuTool.ULAW, ALL, 8, ALL, ALL, ALL, false),
	        new AudioFormat(AuTool.ULAW, ALL, 8, ALL, ALL, ALL, true),

	        new AudioFormat(AuTool.ALAW, ALL, 8, ALL, ALL, ALL, false),
	        new AudioFormat(AuTool.ALAW, ALL, 8, ALL, ALL, ALL, true),

	        new AudioFormat(AuTool.PCM, ALL, 16, ALL, ALL, ALL, true),

	        new AudioFormat(AuTool.PCM, ALL, 24, ALL, ALL, ALL, true),

	        new AudioFormat(AuTool.PCM, ALL, 32, ALL, ALL, ALL, true),
	    };

	public AuAudioFileWriter() {
		super(Arrays.asList(FILE_TYPES),
		      Arrays.asList(AUDIO_FORMATS));
	}


	protected boolean isAudioFormatSupportedImpl(AudioFormat format,
	        AudioFileFormat.Type fileType) {
		return AuTool.getFormatCode(format)!=AuTool.SND_FORMAT_UNSPECIFIED;
	}

	protected AudioOutputStream getAudioOutputStream(
	    AudioFormat audioFormat,
	    long lLengthInBytes,
	    AudioFileFormat.Type fileType,
	    File file)
	throws	IOException {
		// TODO: (generalized) check if either seek is possible
		//       or length is not required in header
		TDataOutputStream	dataOutputStream = new SeekableTDOS(file);
		return new AuAudioOutputStream(audioFormat,
		                               lLengthInBytes,
		                               dataOutputStream);
	}

	protected AudioOutputStream getAudioOutputStream(
	    AudioFormat audioFormat,
	    long lLengthInBytes,
	    AudioFileFormat.Type fileType,
	    OutputStream outputStream)
	throws	IOException {
		// it should be thrown an exception if it is tried to write
		// to a stream but lLengthInFrames is AudioSystem.NOT_SPECIFIED
		TDataOutputStream	dataOutputStream = new NonSeekableTDOS(outputStream);
		return new AuAudioOutputStream(audioFormat,
		                               lLengthInBytes,
		                               dataOutputStream);
	}

}

/*** AuAudioFileWriter.java ***/