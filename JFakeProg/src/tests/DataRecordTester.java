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
package tests;

import org.jfakeprog.hex.DataRecord;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class DataRecordTester
{

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		String raw = ":03000000020023D8";
		int idx = 1;
		int recLen = Integer.parseInt(raw.substring(idx, idx + 2), 16);
		idx += 2;
		int loadOffset = Integer.parseInt(raw.substring(idx, idx + 4), 16);
		idx += 4;
		int recType = Integer.parseInt(raw.substring(idx, idx + 2), 16);
		idx += 2;
		byte[] data = new byte[(raw.length() - 1) / 2 - 5];
		for (int i = 0; i < data.length; i++)
		{
			data[i] = (byte) Integer.parseInt(raw.substring(idx, idx + 2), 16);
			idx += 2;
		}
		byte chekSum = (byte) Integer.parseInt(raw.substring(idx, idx + 2), 16);
		idx += 2;
		
		DataRecord rec1 = new DataRecord(loadOffset, data);
		DataRecord rec2 = new DataRecord(loadOffset, data, chekSum);
		
		System.out.println(String.format("rec1 checksum = %02x", rec1.getRecordChecksum()));
		System.out.println(String.format("rec2 checksum = %02x", rec2.getRecordChecksum()));
		System.out.println("rec1\t\t" + rec1.toHEXString().toUpperCase());
		System.out.println("rec2\t\t" + rec2.toHEXString().toUpperCase());
		System.out.println("original\t" + raw);
	}

}
