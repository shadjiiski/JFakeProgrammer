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

/**
 * @author Stanislav Hadjiiski
 *
 */
public interface IHEX8Record
{
	public enum Type
	{
		/**
		 * Data records are marked with RECTYPE = '00'
		 */
		DATA(0x00, "DATA_RECORD"),
		
		/**
		 * End of file records are marked with RECTYPE = '01'
		 */
		EOF(0x01, "END_OF_FILE_RECORD");
		
		private String textRepresentation;
		private int code;
		
		Type(int code, String text)
		{
			this.code = code;
			this.textRepresentation = text;
		}
		
		@Override
		public String toString()
		{
			return textRepresentation;
		}
		
		public int getCode()
		{
			return code;
		}
	}
	/**
	 * Default record mark in Intel HEX-8bit
	 */
	public static final char DEFAULT_RECORD_MARK = ':';
	
	/**
	 * 
	 * @return the char symbol, used to mark records
	 */
	public char getRecordMark();
	
	/**
	 * 
	 * @return the number of bytes stored in the data part of this record
	 */
	public int getRecordLength();
	
	/**
	 * 
	 * @return the memory load address of the first of this record's data bytes
	 * should have 0 offset
	 */
	public int getLoadOffset();
	
	/**
	 * 
	 * @return the type of this record.
	 * @see Type
	 */
	public Type getRecordType();
	
	/**
	 * 
	 * @return byte array with the stored in this record data. <b>null</b> will be returned
	 * if this record contains no data
	 * @see #getRecordLength()
	 */
	public byte[] getRecordData();
	
	/**
	 * 
	 * @return the checksum of this record as a byte
	 */
	public byte getRecordChecksum();
	
	/**
	 * @return a String representing this record of the HEX file
	 */
	public String toHEXString();
	
	/**
	 * 
	 * @return the bytes of this record.
	 */
	public byte[] toByteArray();
}
