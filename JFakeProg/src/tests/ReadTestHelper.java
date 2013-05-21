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

import gnu.io.CommPortIdentifier;
import gnu.io.RXTXPort;

import java.io.InputStream;
import java.io.OutputStream;

import org.jfakeprog.connection.ProtocolConstants;
import org.jfakeprog.hex.DataRecord;
import org.jfakeprog.hex.EndOfFileRecord;
import org.jfakeprog.hex.IHEX8Record;
import org.jfakeprog.hex.IHEX8Record.Type;
import org.jfakeprog.hex.util.HEXFileParser;
import org.jfakeprog.hex.util.HEXRecordSet;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class ReadTestHelper
{

	public static void main(String[] args) throws Exception
	{
		byte[] data = new byte[4 * 1024];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) 0xFF;
		HEXRecordSet program = new HEXFileParser("D:/BUF/CommTest/all.hex").parse();
		for (IHEX8Record rec : program)
		{
			if(rec.getRecordType() != Type.EOF)
			{
				for(int i = 0; i < rec.getRecordData().length; i++)
					data[rec.getLoadOffset() + i] = rec.getRecordData()[i];
			}
		}
		HEXRecordSet newProgram = new HEXRecordSet();
		for(int i = 0; i < data.length; i += 8)
		{
			byte[] buf = new byte[8];
			for(int j = 0; j < 8; j++)
				buf[j] = data[i + j];
			newProgram.add(new DataRecord(i, buf));
		}
		newProgram.add(new EndOfFileRecord());
		
		RXTXPort port = CommPortIdentifier.getPortIdentifier("COM17").open("ReadTestHelper", 1000);
		port.setSerialPortParams(9600, RXTXPort.DATABITS_8, RXTXPort.STOPBITS_1, RXTXPort.PARITY_NONE);
		port.enableReceiveThreshold(4);
		InputStream in = port.getInputStream();
		OutputStream out = port.getOutputStream();
		System.out.println("Port opened, waiting");
		byte[] buf = new byte[4];
		in.read(buf); // the read signal
		System.out.println(new String(buf));
		out.write(ProtocolConstants.CMD_ACKNOWLEDGE.getBytes());
		//acknowledged, now send the data
		port.enableReceiveThreshold(3);
		byte[] input = new byte[3];
		for(int i = 0; i < newProgram.size();)
		{
			IHEX8Record rec = newProgram.get(i);
//			System.out.println("Sending: " + rec.toHEXString());
			out.write(rec.toByteArray());
			in.read(input); // Acknowledge
			String recv = new String(input);
			if(recv.equalsIgnoreCase("RES"))
				continue;
			if(rec.getRecordType() != Type.EOF)
			{
				in.read(input); // next
				if(new String(input).equalsIgnoreCase("NXT"))
					i++;
			}
			else if(recv.equalsIgnoreCase("AKN"))
				i++;
		}
		port.close();
		System.out.println("Exiting");
		System.exit(0);
	}

}
