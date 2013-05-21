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

import java.util.ArrayList;

import org.jfakeprog.hex.DataRecord;
import org.jfakeprog.hex.IHEX8Record;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class HEXRecordSet extends ArrayList<IHEX8Record>
{
	private static final long serialVersionUID = 2077728059879893943L;
	
	/**
	 * Splits every record that holds more than <code>dataBytes</code> bytes of data in
	 * shorter records
	 * @param dataBytes maximum data bytes per record
	 */
	public void shortenRecords(int dataBytes)
	{
		for(int i = 0; i < size(); i++)
		{
			IHEX8Record record = get(i);
			// shorten only data records
			if(record.getRecordType() != IHEX8Record.Type.DATA || record.getRecordLength() <= dataBytes)
				continue;
			
			int leftOffset = record.getLoadOffset();
			int RightOffset = leftOffset + dataBytes;
			
			byte[] leftData = new byte[dataBytes];
			byte[] rightData = new byte[record.getRecordLength() - dataBytes];
			int idx = 0;
			for(; idx < dataBytes; idx++)
				leftData[idx] = record.getRecordData()[idx];
			for(; idx < record.getRecordLength(); idx++)
				rightData[idx - dataBytes] = record.getRecordData()[idx];
			
			remove(i);
			add(i, new DataRecord(RightOffset, rightData));
			add(i, new DataRecord(leftOffset, leftData));
		}
	}
}
