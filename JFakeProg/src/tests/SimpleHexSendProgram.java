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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import gnu.io.RXTXPort;
import gnu.io.SerialPortEvent;

import org.jfakeprog.connection.ConnectionHandler;
import org.jfakeprog.hex.IHEX8Record;
import org.jfakeprog.hex.util.HEXFileParser;
import org.jfakeprog.hex.util.HEXRecordSet;
import org.jfakeprog.tasks.AProgrammerTask;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class SimpleHexSendProgram extends AProgrammerTask
{
	private static final int TIMEOUT = 300; //ms
	private int currentIndex;
	private RXTXPort port;
	private HEXRecordSet records;
	private Timer timer;
	
	public SimpleHexSendProgram() throws Exception
	{
		super();
		records = new HEXFileParser("D:/BUF/CommTest/TransRecvTest.hex").parse();
		ConnectionHandler handler = new ConnectionHandler("COM18");
		handler.addAProgrammerTask(this);
		timer = new Timer(true);
	}
	

	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		this.port = port;
		super.startTask(port);
		port.enableReceiveTimeout(100);
		port.notifyOnDataAvailable(true);
		
		currentIndex = 0;
		sendCurrentRecord();
	}
	
	private void sendCurrentRecord()
	{
		OutputStream out = port.getOutputStream();
		IHEX8Record record = records.get(currentIndex);
		System.out.println("Sending:\t" + getHEXofBytes(record.toByteArray()));
		try
		{
			out.write(record.toByteArray());
			out.flush();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					System.err.println("Timeout reached but got no response. Resending.");
					sendCurrentRecord();
				}
			}, TIMEOUT);
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
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try
		{
			InputStream in = port.getInputStream();
			bytes.write(in.read()); // mark record
			int len = in.read();
			bytes.write(len);
			for(int i = 0; i < len + 4; i++)
				bytes.write(in.read()); //offset, type, data and checksum bytes
			
			System.out.println("Received:\t" + getHEXofBytes(bytes.toByteArray()));
			if(checkResponse(bytes.toByteArray()))
			{
				System.out.println("Record received OK. Sending next one");
				currentIndex++;
			}
			else
				System.err.println("Error exists in received bytes. Resending");
			
			timer.cancel(); // cancel timeout detection
			
			if(currentIndex < records.size())
				sendCurrentRecord();
			else
			{
				System.out.println("Everything sent. Closing");
				taskComplete();
				System.exit(0);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean checkResponse(byte[] response)
	{
		byte[] source = records.get(currentIndex).toByteArray();
		if(source.length != response.length)
			return false;
		for(int i = 0; i < source.length; i++)
		{
			if(source[i] != response[i])
				return false;
		}
		
		return true;
	}
	
	private String getHEXofBytes(byte[] bytes)
	{
		StringBuffer buf = new StringBuffer();
		for(byte b: bytes)
			buf.append(String.format("%02x", b));
		return buf.toString();
	}

	public static void main(String[] args) throws Exception
	{
		new SimpleHexSendProgram();
	}
}
