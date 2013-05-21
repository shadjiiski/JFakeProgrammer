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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.jfakeprog.hex.IHEX8Record;
import org.jfakeprog.hex.util.HEXFileParser;
import org.jfakeprog.hex.util.HEXRecordSet;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class HEXParserTester
{

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		File f = new File("D:/BUF/CommTest/TransRecvTest.hex");
		
		System.out.println("======= ORIGINAL =========\n");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String read = null;
		while((read = reader.readLine()) != null)
			System.out.println(read);
		reader.close();
		
		System.out.println("\n======= PARSED =========\n");
		HEXRecordSet list = new HEXFileParser(f).parse();
		for(IHEX8Record rec: list)
			System.out.println(rec.toHEXString());
		
		System.out.println("\n======= SHORTENED =========\n");
		list.shortenRecords(8);
		for(IHEX8Record rec: list)
			System.out.println(rec.toHEXString());
	}

}
