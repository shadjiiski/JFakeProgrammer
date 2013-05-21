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

import gnu.io.RXTXPort;
import gnu.io.SerialPortEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.jfakeprog.connection.ConnectionHandler;
import org.jfakeprog.connection.ProtocolConstants;
import org.jfakeprog.hex.DataRecord;
import org.jfakeprog.hex.IHEX8Record;
import org.jfakeprog.hex.util.HEXFileParser;
import org.jfakeprog.hex.util.HEXRecordSet;
import org.jfakeprog.tasks.AProgrammerTask;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class DataTest extends AProgrammerTask
{
	private static final int TIMEOUT = 500; //ms
	private RXTXPort port;
	private boolean sent = false;
	
	public DataTest()
	{
		ConnectionHandler handler = new ConnectionHandler("COM23");
		handler.addAProgrammerTask(this);
	}
	
	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		this.port = port;
		super.startTask(port);
		port.enableReceiveTimeout(TIMEOUT);
		port.notifyOnDataAvailable(true);
		
		send();
	}
	
	private void send()
	{
		OutputStream out = port.getOutputStream();
		try
		{
			if(!sent) // sent the hex file
			{
				sent = true;
				HEXFileParser parser = new HEXFileParser("D:/BUF/CommTest/TransRecvTest.hex");
				HEXRecordSet parsed = parser.parse();
				parsed.shortenRecords(8);
				IHEX8Record record = parsed.get(0);
				System.out.println("Sending:\t" + record.toHEXString());
				out.write(record.toByteArray());
			}
			else
			{
				out.write(ProtocolConstants.MARK_DEBUG);
				out.write(ProtocolConstants.CMD_DEBUG_DUMP.getBytes());
			}
			out.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void serialEvent(SerialPortEvent ev)
	{
		if(ev.getEventType() != SerialPortEvent.DATA_AVAILABLE)
			return;
		InputStream in = port.getInputStream();
		int b;
		try
		{
			b = in.read();
			if(b == ProtocolConstants.MARK_RECORD)
			{
				int len = in.read();
				int offset = (in.read() << 8) + in.read();
				int datType = in.read();
				if(datType != 0)
				{
					System.err.println("Data type is not 0 but " + datType);
					return;
				}
				byte[] bytes = new byte[len];
				for(int i = 0; i < len; i++)
					bytes[i] = (byte) in.read();
				byte checkSum = (byte) in.read();
				if(new DataRecord(offset, bytes).getRecordChecksum() != checkSum)
					System.err.println("Calculated checksum does not match the received one");
				System.out.println("Received:\t" + new DataRecord(offset, bytes, checkSum).toHEXString());
				System.out.println("Exiting");
				System.exit(0);
			}
			else
			{
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				bytes.write(b);
				bytes.write(in.read());
				bytes.write(in.read());
				String cmd = new String(bytes.toByteArray());
				if(cmd.equalsIgnoreCase(ProtocolConstants.CMD_ACKNOWLEDGE))
				{
					System.out.println("Record aknowledged");
					send();
				}
				else if(cmd.equalsIgnoreCase(ProtocolConstants.CMD_NEXT))
					System.out.println("NEXT record issued. We are not sending it.");
				else if(cmd.equalsIgnoreCase(ProtocolConstants.CMD_RESEND))
				{
					System.err.println("RESEND signal received. Resending");
					sent = false;
					send();
				}
				else
					System.err.println("Unexpected command received: '" + cmd + "'. No response");
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		new DataTest();
	}
}
