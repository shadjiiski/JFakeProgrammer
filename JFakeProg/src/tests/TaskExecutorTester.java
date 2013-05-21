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

import java.util.TooManyListenersException;

import org.jfakeprog.connection.ConnectionHandler;
import org.jfakeprog.tasks.AProgrammerTask;
import org.jfakeprog.tasks.EraseMemoryTask;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class TaskExecutorTester
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ConnectionHandler handler = new ConnectionHandler("COM17");
		System.out.println("Adding long");
		handler.addAProgrammerTask(new LongTask());
		System.out.println("Adding erase");
		handler.addAProgrammerTask(new EraseMemoryTask());
	}
	
	public static class LongTask extends AProgrammerTask
	{
		
		@Override
		public void startTask(RXTXPort port) throws TooManyListenersException
		{
			System.out.println("Long task starting");
			super.startTask(port);
			try
			{
				Thread.sleep(10000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			System.out.println("Long task stopping");
			taskComplete();
		}
		@Override
		public void serialEvent(SerialPortEvent ev)
		{
//			Do nothing - no events expected
		}
		
	}
}
