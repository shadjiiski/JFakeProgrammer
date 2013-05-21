/*******************************************************************************
 * JFakeProgrammer - Connection Interface and GUI for an AT89Cx051 programmer
 * Copyright (C) 2013  Stanislav Hadjiiski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 ******************************************************************************/
/**
 * 
 */
package org.jfakeprog.hex.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jfakeprog.hex.DataRecord;
import org.jfakeprog.hex.EndOfFileRecord;
import org.jfakeprog.hex.IHEX8Record;

/**
 * @author Stanislav Hadjiiski
 * 
 */
public class HEXFileParser
{
	private File file;

	/**
	 * Constructs a 8-bit HEX file parser by filename specified
	 * @param fileName
	 * @throws FileNotFoundException if file with such name does not exist
	 */
	public HEXFileParser(String fileName) throws FileNotFoundException
	{
		this(new File(fileName));
	}

	/**
	 * constructs a 8-bit HEX file parser by file object specified
	 * @param hexFile
	 * @throws FileNotFoundException if file with such name does not exist
	 */
	public HEXFileParser(File hexFile) throws FileNotFoundException
	{
		if (hexFile == null)
			throw new NullPointerException("hexFile cannot be null");
		if (!hexFile.exists())
			throw new FileNotFoundException("hexFile must exist");
		this.file = hexFile;
	}

	/**
	 * Parses the file specified in the constructor call. Result is given in a {@link HEXRecordSet}
	 * object. Every line of the input file is represented with single record in the set. Splitting
	 * large records may be done by invoking {@link HEXRecordSet#shortenRecords(int)}
	 * @return List of the records stored in the specified 8-bit HEX file
	 * @throws IOException
	 * @see HEXRecordSet
	 */
	public HEXRecordSet parse() throws IOException
	{
		HEXRecordSet records = new HEXRecordSet();
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				//skip the first byte - it's the mark record ':'
				int idx = 1;
				@SuppressWarnings("unused")
				int recLen = Integer.parseInt(line.substring(idx, idx + 2), 16);
				idx += 2;
				int loadOffset = Integer.parseInt(line.substring(idx, idx + 4), 16);
				idx += 4;
				int recType = Integer.parseInt(line.substring(idx, idx + 2), 16);
				idx += 2;
				
				if(recType == IHEX8Record.Type.EOF.getCode())
				{
					records.add(new EndOfFileRecord());
					continue;
				}
				//else is data record, so read the data
				
				byte[] data = new byte[(line.length() - 1) / 2 - 5];
				for (int i = 0; i < data.length; i++)
				{
					data[i] = (byte) Integer.parseInt(line.substring(idx, idx + 2), 16);
					idx += 2;
				}
				byte chekSum = (byte) Integer.parseInt(line.substring(idx, idx + 2), 16);
				idx += 2;
				if(idx != line.length())
				{
					reader.close();
					throw new HEXParserException("Wrong hex file format: read the check sum, but data record end was not reached.");
				}
				
				//if everything passed well
				records.add(new DataRecord(loadOffset, data, chekSum));
			}
			reader.close();
		} catch (FileNotFoundException e)
		{
			// this should not happen - file should exist
			e.printStackTrace();
		}
		return records;
	}
	
	private class HEXParserException extends RuntimeException
	{
		private static final long serialVersionUID = 4620925529782364871L;

		public HEXParserException(String error)
		{
			super(error);
		}
	}
}
