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
package org.jfakeprog.connection;

import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;

import org.jfakeprog.tasks.AProgrammerTask;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class ConnectionHandler
{
	/**
	 * Name of the serial port owner
	 */
	public static final String OWNER_NAME = "JFakeProgConnectionHandler";
	
	/**
	 * Maximum delay when trying to open the port
	 */
	public static final int DELAY_OPEN_PORT = 1000;
	
	private List<AProgrammerTask> tasks;
	private AProgrammerTask currentTask;

	/**
	 * Specifies which port should be used for connection with the JFake Programmer. Can be set to autofind by
	 * setting it's value to <code>null</code>
	 */
	private String portNameToUse;
	
	private RXTXPort port;
	private boolean connected;
	
	/**
	 * Default constructor. Just initializes the object
	 */
	public ConnectionHandler()
	{
		this(null);
	}
	
	/**
	 * Initializes the handler and tries to connect to the specified port
	 * @param port
	 */
	public ConnectionHandler(String port)
	{
		connected = false;
		tasks = new ArrayList<AProgrammerTask>();
		setPrefferedPortName(port);
		startTaskExecutor();
	}
	
	/**
	 * Overrides the default auto-find port action by specifying a port to use by name.
	 * To use the auto-find option again pass <code>null</code> as argument
	 * @param portToUse the name of the port (e.g. COM1 or /dev/ttyS0)
	 */
	public void setPrefferedPortName(String portToUse)
	{
		if(portToUse == null && this.portNameToUse == null)
			return;
		if(portToUse != null && this.portNameToUse != null && portToUse.toLowerCase().equals(this.portNameToUse.toLowerCase()))
			return; // nothing to change
		this.portNameToUse = portToUse;
		if(isConnected())
		{
			// reconnect if needed
			disconnect();
			connect(portToUse);
		}
	}
	
	/**
	 * @return the port that the application will try to connect to. <code>null</code> means auto select
	 */
	public String getPrefferedPortName()
	{
		return this.portNameToUse;
	}
	
	/**
	 * {@link IllegalStateException} will be thrown if invoked while there is no connected port
	 * @return the name of the connected port.
	 */
	public String getPortName()
	{
		if(!connected)
			throw new IllegalStateException("Cannot get port name - not connected");
		return port.getName();
	}

	/**
	 * @return whether the handler is currently connected to any port or not
	 */
	public boolean isConnected()
	{
		return connected;
	}
	
	/**
	 * Tries to connect to the programmer. Default behavior is to use the discover
	 * protocol to automatically find which port the device is connected to. Custom port
	 * may be forced by using the {@link #setPortName(String)} function. If you want
	 * to fall back to the auto-find option again, just invoke {@link #setPortName(String)}
	 * by passing <code>null</code> as argument. This method will also attempt to clear
	 * the input buffer after a successful connection
	 * 
	 * @return whether a connection was established or not
	 */
	public boolean connect()
	{
		if(connected)
		{
			System.err.println("Already connected");
			return true;
		}
		if(portNameToUse == null)
			connected = autoConnect();
		else
			connected = connect(portNameToUse);
		if(connected)
		{
			port.disableReceiveThreshold();
			port.enableReceiveTimeout(100);
			try
			{
				while(port.getInputStream().read() > 0) // read what is left in the buffer
					continue;
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return connected;
	}
	
	/**
	 * Uses the DISCOVER command to find which port is the JFakeProgrammer connected
	 * to and tries to connect to it
	 * @return whether the connection was a success
	 */
	private boolean autoConnect()
	{
		Set<String> ports = getAvailablePorts();
		for (String portName : ports)
		{
			RXTXPort port;
			try
			{
				CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
				String owner = portId.getCurrentOwner();
				if(owner != null && owner.length() > 0) // skip busy ports
					continue;
				port = portId.open(OWNER_NAME, DELAY_OPEN_PORT);
				port.setSerialPortParams(9600, RXTXPort.DATABITS_8, RXTXPort.STOPBITS_1, RXTXPort.PARITY_NONE);
				port.enableReceiveTimeout(500); //returns read after 500 ms even if no data is present
				port.enableReceiveThreshold(3); //returns read statement after three read bytes even if there are more to read
				int retries = 5;
				boolean statusOK = false;
				while((!statusOK) && retries > 0)
				{
					port.getOutputStream().write(ProtocolConstants.MARK_GENERAL);
					port.getOutputStream().write(ProtocolConstants.CMD_DISCOVER.getBytes());
					byte[] buf = new byte[3];
					port.getInputStream().read(buf);
					
					if(new String(buf).equalsIgnoreCase(ProtocolConstants.CMD_JFAKE_PROG)) // found it!
					{
						this.port = port;
						connected = true;
						return true;
					}
					else if((--retries) <= 0)// not this port. Close and try next
						port.close();
				}
			} catch (PortInUseException e)
			{
//				Skip exceptions for port in use. Should not be thrown as busy ports are skipped
//				but not all applications set the owner string
			} catch (NoSuchPortException | UnsupportedCommOperationException | IOException e)
			{
				e.printStackTrace();
			}
		}
		System.err.println("Could not find a JFake Proggramer device connected on any port");
		this.port = null;
		connected = false;
		return false;
	}
	
	/**
	 * Tries to connect to port <b>portName</b>
	 * @param portName
	 */
	private boolean connect(String portName)
	{
		CommPortIdentifier portId;
		try
		{
			portId = CommPortIdentifier.getPortIdentifier(portName);
			port = portId.open(OWNER_NAME, DELAY_OPEN_PORT);
			port.setSerialPortParams(9600, RXTXPort.DATABITS_8, RXTXPort.STOPBITS_1, RXTXPort.PARITY_NONE);
			connected = true;
			return true;
		} catch (NoSuchPortException e)
		{
			e.printStackTrace();
		} catch (PortInUseException e)
		{
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e)
		{
			System.err.println("Could not set serial port parameters. Reason:");
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Disconnects currently selected port if connected
	 */
	public void disconnect()
	{
		if(!connected)
		{
			System.err.println("Cannot disconnect: port is not connected");
			return;
		}
		connected = false;
		port.removeEventListener();
		port.close();
	}
	
	/**
	 * Ads the task specified to the task queue for processing. This method will
	 * connect to the programmer if no connection is already established
	 * @param task
	 */
	public void addAProgrammerTask(AProgrammerTask task)
	{
		if(task != null)
		{
			if(!isConnected())
				connect();
			synchronized (tasks)
			{
				tasks.add(task);
			}
		}
	}
	
	private synchronized void startTaskExecutor()
	{
		new ExecutorThread().start();
	}
	
	/**
	 * 
	 * @return a list of all available to the library ports' names
	 */
	public static Set<String> getAvailablePorts()
	{
		return NRSerialPort.getAvailableSerialPorts();
	}
	
	private class ExecutorThread extends Thread
	{
		@Override
		public void run()
		{
			while(true) // endless
			{
				synchronized (tasks)
				{
					if(tasks.size() == 0)
						currentTask = null;
					else
						currentTask = tasks.get(0);
				}
				if(currentTask != null)
				{
					
					try
					{
						currentTask.startTask(port);
						while(!currentTask.isComplete()) // wait for task completion
						{
							try
							{
								Thread.sleep(100); // give it some time
							} catch (InterruptedException e){}
						}
					} catch (TooManyListenersException e)
					{
						e.printStackTrace();
					}
					finally
					{
						synchronized (tasks)
						{
							tasks.remove(0);
						}
					}
				}
				
				try
				{
					Thread.sleep(100); // wait for new tasks a little more
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
