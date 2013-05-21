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
package org.jfakeprog.hex;

import org.jfakeprog.connection.ProtocolConstants;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class DataRecord implements IHEX8Record
{
	private byte checkSum;
	private int loadOffset;
	private byte[] dataBytes;
	private byte[] recordBytes;
	
	public DataRecord(int loadOffset, byte[] dataBytes)
	{
		if(loadOffset < 0)
			throw new IllegalArgumentException("Offset must be >= 0");
		if(dataBytes.length > 255)
			throw new IllegalArgumentException("Cannot store more than 255 bytes in a single record");
		
		this.loadOffset = loadOffset;
		this.dataBytes = dataBytes;
		this.checkSum = calculateCheckSum();
		calculateRecordBytes();
	}
	
	public DataRecord(int loadOffset, byte[] dataBytes, byte checkSum)
	{
		if(loadOffset < 0)
			throw new IllegalArgumentException("Offset must be >= 0");
		if(dataBytes.length > 255)
			throw new IllegalArgumentException("Cannot store more than 255 bytes in a single record");
		
		this.loadOffset = loadOffset;
		this.dataBytes = dataBytes;
		this.checkSum = checkSum;
		calculateRecordBytes();
	}
	
	private void calculateRecordBytes()
	{
		recordBytes = new byte[6 + dataBytes.length];
		recordBytes[0] = (byte) ProtocolConstants.MARK_RECORD;
		recordBytes[1] = (byte) dataBytes.length;
		recordBytes[2] = (byte) ((loadOffset & 0xFF00) >> 8);
		recordBytes[3] = (byte) (loadOffset & 0xFF);
		recordBytes[4] = 0;
		int i = 5;
		for ( ; i < recordBytes.length - 1; i++)
			recordBytes[i] = dataBytes[i - 5];
		recordBytes[i] = checkSum;
	}
	
	/**
	 * The checksum is calculated as follows:
	 * <ol>
	 * <li> Add all bytes of the record, excluding the check sum byte and the record mark byte</li>
	 * <li> Strip all bits but the last 8</li>
	 * <li> Apply two's complement operation</li>
	 * <li> return the result</li>
	 * </ol>
	 * @return the checkSum
	 */
	private byte calculateCheckSum()
	{
		//data type is 00
		int checkSum = getRecordLength();
		int address = getLoadOffset();
		checkSum += (address & 0xFF00) >> 8;
		checkSum += (address & 0xFF);
		for (int i = 0; i < dataBytes.length; i++)
		{
			checkSum += dataBytes[i];
			if(dataBytes[i] < 0) // because bytes in java are always signed
				checkSum += 256;
		}
		checkSum = (0x100 - (checkSum & 0xFF)) & 0xFF;
		
		return (byte) checkSum;
	}
	
	@Override
	public char getRecordMark()
	{
		return DEFAULT_RECORD_MARK;
	}

	@Override
	public int getRecordLength()
	{
		return dataBytes.length;
	}

	@Override
	public int getLoadOffset()
	{
		return loadOffset;
	}

	@Override
	public Type getRecordType()
	{
		return Type.DATA;
	}

	@Override
	public byte[] getRecordData()
	{
		return dataBytes;
	}

	@Override
	public byte getRecordChecksum()
	{
		return checkSum;
	}

	@Override
	public String toHEXString()
	{
		StringBuffer result = new StringBuffer(String.format("%s%02x%04x00", ProtocolConstants.MARK_RECORD, getRecordLength(), getLoadOffset()));
		for (int i = 0; i < dataBytes.length; i++)
			result.append(String.format("%02x", dataBytes[i]));
		result.append(String.format("%02x", getRecordChecksum()));
		return result.toString().toUpperCase();
	}
	
	@Override
	public byte[] toByteArray()
	{
		return recordBytes;
	}

}
