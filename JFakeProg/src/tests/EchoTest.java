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
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * @author Stanislav Hadjiiski
 * 
 */
public class EchoTest implements SerialPortEventListener
{
	private OutputStream out;
	private InputStream in;
	private String dataToSend;
	private boolean wait = false;
	private int receivedCount;

	public EchoTest()
	{
		SerialPort port = null;
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> commIds = CommPortIdentifier.getPortIdentifiers();
		while (commIds.hasMoreElements())
		{
			CommPortIdentifier commId = commIds.nextElement();
			if(commId.getName().equalsIgnoreCase("com16"))
//			if(commId.getName().equalsIgnoreCase("com16") || commId.getName().equalsIgnoreCase("com17"))
			{
				try
				{
					System.out.println("Trying to connect to " + commId.getName());
					port = (SerialPort) commId.open("FakeEchoTest", 1000);
					System.out.println("Connected");
					break;
				} catch (PortInUseException e)
				{
					System.out.println("Connection failed");
				}
			}
		}
		if(port == null)
			System.exit(1);
		
		try
		{
			port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
//			port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);
			port.notifyOnOutputEmpty(true);
			port.notifyOnDataAvailable(true);
			port.addEventListener(this);
			in = port.getInputStream();
			out = port.getOutputStream();
			dataToSend = "hello";
//			sendData();
		} catch (UnsupportedCommOperationException e)
		{
			e.printStackTrace();
		} catch (TooManyListenersException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void readData()
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		int b = -1;
		try
		{
			while((b = in.read()) != -1 && b != '\n')
				bytes.write(b);
			String msg = new String(bytes.toByteArray());
			System.out.println("received(" + (receivedCount++) + "):\t" + msg);
			dataToSend = msg;
			Thread.sleep(300);
			sendData();
		} catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendData()
	{
		if(wait)
			return;
		byte[] bytes = dataToSend.getBytes();
		try
		{
			out.write(bytes, 0, bytes.length);
			out.write('\n');
			wait = true;
			System.out.println("sent:\t" + dataToSend);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			dataToSend = null;
		}
	}
	
	@Override
	public void serialEvent(SerialPortEvent ev)
	{
		if(ev.getEventType() == SerialPortEvent.DATA_AVAILABLE)
			readData();
		if(ev.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY)
		{
			wait = false;
			if(dataToSend != null)
				sendData();
		}
	}
	
	public static void main(String[] args)
	{
		new EchoTest();
	}
}
