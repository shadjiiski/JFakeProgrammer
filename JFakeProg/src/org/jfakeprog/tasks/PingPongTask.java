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
public class PingPongTask extends AProgrammerTask
{
	private ProgressDialog dialog;
	private Logger logger;
	
	public PingPongTask(Frame owner, Logger logger)
	{
		super();
		if(logger == null)
			logger = new DefaultLogger();
		this.logger = logger;
		dialog = new ProgressDialog(owner);
		dialog.setIndeterminate(true);
		dialog.setTitle("Ping/Pong тест");
		dialog.setProgressText("Изпращане на Ping команда (300 ms timeout)");
	}
	
	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		dialog.setVisible(true);
		super.startTask(port);
		port.enableReceiveTimeout(300); // 300 ms
		port.enableReceiveThreshold(3); //3 bytes
		
		OutputStream out = port.getOutputStream();
		InputStream in = port.getInputStream();
		
		try
		{
			int retries = 5;
			boolean statusOK = false;
			while((!statusOK) && retries > 0)
			{
				logger.log("Изпращане на Ping команда", LogLevel.DEBUG);
				out.write(ProtocolConstants.MARK_DEBUG);
				out.write(ProtocolConstants.CMD_DEBUG_PING.getBytes());
				
				byte[] buf = new byte[3];
				int read = in.read(buf);
				String received = new String(buf);
				if(read <= 0)
					logger.log("Няма отговор след 300 ms. Повторен опит", LogLevel.DEBUG);
				else if(received.equalsIgnoreCase(ProtocolConstants.CMD_DEBUG_PONG))
				{
					logger.log("Получен отговор Pong. Край на задачата", LogLevel.SUCCESS);
					statusOK = true;
				}
				else
					logger.log("Вместо Pong беше получено " + received + ". Повторен опит", LogLevel.DEBUG);
				retries--;
			}
			if(!statusOK)
				logger.log("Прекратяване след 5 неуспешни опита", LogLevel.ERRORS);
			
		} catch (IOException e)
		{
			logger.log("Възникна грешка по време на комуникацията: " + e.getMessage(), LogLevel.DEBUG);
			e.printStackTrace();
		}
		finally
		{
			taskComplete();
		}
	}
	
	@Override
	public void taskComplete()
	{
		super.taskComplete();
		dialog.dispose();
	}
	
	@Override
	public void serialEvent(SerialPortEvent ev)
	{
//		Not used, task is too simple
	}

}
