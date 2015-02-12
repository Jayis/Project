/*
 *	MpegFileFormatType.java
 */

/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
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


import lib.sound.sampled.AudioFileFormat;


/**	FileFormatTypes used by the MPEG audio decoder.
 *
 *	@author Matthias Pfisterer
 */
 
public class MpegFileFormatType
	extends	AudioFileFormat.Type
{
	public static final AudioFileFormat.Type	MPEG = new MpegFileFormatType("MPEG", "mpeg");
	//$$fb 2000-08-15: added mp3 special file format for the correct extension
	public static final AudioFileFormat.Type	MP3 = new MpegFileFormatType("MP3", "mp3");



	public MpegFileFormatType(String strName,
				  String strExtension)
	{
		super(strName, strExtension);
	}
}



/*** MpegFileFormatType.java ***/

