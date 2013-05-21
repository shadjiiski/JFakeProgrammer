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
package org.jfakeprog.tasks;

import gnu.io.RXTXPort;
import gnu.io.SerialPortEvent;

import java.awt.Frame;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.jfakeprog.connection.ProtocolConstants;
import org.jfakeprog.gui.ProgressDialog;
import org.jfakeprog.log.DefaultLogger;
import org.jfakeprog.log.LogLevel;
import org.jfakeprog.log.Logger;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class EraseMemoryTask extends AProgrammerTask
{
	private Logger logger;
	private ProgressDialog progress;
	
	public EraseMemoryTask()
	{
		this(null, null);
	}
	
	public EraseMemoryTask(Logger logger, Frame frameParent)
	{
		super();
		if(logger == null)
			logger = new DefaultLogger();
		this.logger = logger;
		progress = new ProgressDialog(frameParent);
		progress.setTitle("Изтриване на памет");
		progress.setIndeterminate(true);
		progress.setProgressText("Изпращане на команда за изтриване");
	}
	
	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		progress.setVisible(true);
		super.startTask(port);
		
		OutputStream out = port.getOutputStream();
		InputStream in = port.getInputStream();
		
		
		try
		{
			port.enableReceiveThreshold(3);
			port.enableReceiveTimeout(500);
			byte[] buf = new byte[3];
			boolean statusOK = false;
			int retries = 5;
			String received;
			while((!statusOK) && retries > 0)
			{
				//send erase signal
				out.write(ProtocolConstants.MARK_ERASE);
				out.write(ProtocolConstants.CMD_ERASE.getBytes());
				logger.log("Командата за изтриване е изпратена", LogLevel.DEBUG);
				
				//give it some time to make sure it is read. May be just a time loss?
				Thread.sleep(100);
				
				//wait for acknowledge or unknown
				in.read(buf);
				received = new String(buf).toUpperCase();
				if(!received.equals(ProtocolConstants.CMD_ACKNOWLEDGE.toUpperCase()))
					logger.log("Командата за изтриване не е разбрана. Повтаряне", LogLevel.DEBUG);
				else
				{
					statusOK = true;
					logger.log("Командата за изтриване е приета", LogLevel.DEBUG);
				}
				retries--;
			}
			if(!statusOK)
			{
				logger.log("Прекратяване след 5 неуспешни опита", LogLevel.ERRORS);
				taskComplete();
				return;
			}
			progress.setProgressText("Установяване на резултата от изтриването (3 s timeout)");
			//wait for success, error or timeout
			port.enableReceiveTimeout(3000);
			int read = in.read(buf);
			received = new String(buf).toUpperCase();
			if(read <= 0)
				logger.log("Командата за изтриване е приета, но крайния резултат е неясен. Проверете", LogLevel.ERRORS);
			else if(received.equals(ProtocolConstants.CMD_SUCCESS))
				logger.log("Паметта на устройството е успешно изтрита", LogLevel.SUCCESS);
			else if(received.equals(ProtocolConstants.CMD_ERROR))
				logger.log("Командата за изтриване е приета, но възникна грешка при изтриването", LogLevel.ERRORS);
			else
				logger.log("Командата за изтриване е приета. Вместо статус съобщение се получи " + received, LogLevel.ERRORS);
		} catch (IOException e)
		{
			logger.log("Възникна грешка при комуникацията относно изтриване на ROM памет", LogLevel.ERRORS);
			logger.log(e.getMessage(), LogLevel.DEBUG);
			e.printStackTrace();
		} catch (InterruptedException e) {}
		finally
		{
			taskComplete();
		}
	}

	@Override
	public void taskComplete()
	{
		super.taskComplete();
		progress.dispose();
	}
	
	@Override
	public void serialEvent(SerialPortEvent e)
	{
//		Not used as the task is too simple
	}
}
