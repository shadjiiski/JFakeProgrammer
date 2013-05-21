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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.jfakeprog.connection.ConnectionHandler;
import org.jfakeprog.connection.ProtocolConstants;
import org.jfakeprog.tasks.AProgrammerTask;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class PingPongTest extends AProgrammerTask
{
	private static final int TIMEOUT = 500; //ms
	private RXTXPort port;
	
	public PingPongTest()
	{
		ConnectionHandler handler = new ConnectionHandler("COM19");
		handler.addAProgrammerTask(this);
	}
	
	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		this.port = port;
		super.startTask(port);
		port.enableReceiveTimeout(TIMEOUT);
		port.enableReceiveThreshold(3);
		port.notifyOnDataAvailable(true);
		
		sendPing();
	}
	
	private void sendPing()
	{
		OutputStream out = port.getOutputStream();
		try
		{
			System.out.println("ping?");
			out.write(ProtocolConstants.MARK_DEBUG);
			out.write(ProtocolConstants.CMD_DEBUG_PING.getBytes());
//			char[] buf = new char[]{0x90, 0x90, 0x90,
//									0x01, 0x02, 0x04, 0x08, 0x10,  0x20,  0x40, 0x80, 0xff};
//			for(int c: buf)
//			{
//				System.out.format("Send:\t%02x\n", c);
//				out.write(c);
//			}
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
//		waiting for PONG command
		InputStream in = port.getInputStream();
		byte[] pong = new byte[3];
		try
		{
			for (int i = 0; i < pong.length; i++)
				pong[i] = (byte) in.read();
			String resp = new String(pong); 
			if(resp.equalsIgnoreCase(ProtocolConstants.CMD_DEBUG_PONG))
				System.out.println("PONG!");
			else
				System.err.format("Waited for '%s' but got '%s'\n", ProtocolConstants.CMD_DEBUG_PONG, resp);
			Thread.sleep(TIMEOUT);
			sendPing();
//			System.out.format("Received:\t%02x\n", in.read());
				
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		new PingPongTest();
	}
}
