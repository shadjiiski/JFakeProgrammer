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
public class EndOfFileRecord implements IHEX8Record
{
	private byte[] bytes;
	
	public EndOfFileRecord()
	{
//		:00000001FF
		bytes = new byte[]{(byte) ProtocolConstants.MARK_RECORD, 0, 0, 0, 1, (byte) 0xFF};
	}
	
	@Override
	public char getRecordMark()
	{
		return DEFAULT_RECORD_MARK;
	}

	@Override
	public int getRecordLength()
	{
		return 0;
	}

	@Override
	public int getLoadOffset()
	{
		return 0;
	}

	@Override
	public Type getRecordType()
	{
		return Type.EOF;
	}

	/**
	 * @return <b>null</b> as EOF records contain no data
	 */
	@Override
	public byte[] getRecordData()
	{
		return null;
	}

	/**
	 * @return 0xFF as this is always the EOF record checksum
	 */
	@Override
	public byte getRecordChecksum()
	{
		return (byte) 0xff;
	}

	/**
	 * @return ":00000001FF" will be always returned.
	 */
	@Override
	public String toHEXString()
	{
		return staticToHEXString();
	}
	
	public static String staticToHEXString()
	{
		return (ProtocolConstants.MARK_RECORD + "00000001FF");
	}

	@Override
	public byte[] toByteArray()
	{
		return bytes;
	}
}
