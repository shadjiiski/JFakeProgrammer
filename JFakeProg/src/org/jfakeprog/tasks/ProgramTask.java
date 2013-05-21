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
import org.jfakeprog.hex.IHEX8Record;
import org.jfakeprog.hex.IHEX8Record.Type;
import org.jfakeprog.hex.util.HEXRecordSet;
import org.jfakeprog.log.DefaultLogger;
import org.jfakeprog.log.LogLevel;
import org.jfakeprog.log.Logger;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class ProgramTask extends AProgrammerTask
{
	private ProgressDialog progress;
	private Logger logger;
	private HEXRecordSet program;
	private int currentIndex;
	
	public ProgramTask(Frame owner, Logger logger, HEXRecordSet program)
	{
		super();
		if(logger == null)
			logger = new DefaultLogger();
		this.logger = logger;
		this.program = program;
		this.currentIndex = 0;
		progress = new ProgressDialog(owner);
		progress.setIndeterminate(false);
		progress.setMin(0);
		progress.setMax(program.size());
		progress.setValue(0);
		progress.setProgressText("Начална подготовка");
	}
	
	private void updateProgress(boolean again)
	{
		progress.setValue(currentIndex);
		if(again)
			progress.setProgressText("Повторно изпращане на запис (" + (currentIndex + 1) + "/" + program.size() + ")");
		else
			progress.setProgressText("Изпращане на запис (" + (currentIndex + 1) + "/" + program.size() + ")");
	}
	
	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		progress.setVisible(true);
		super.startTask(port);
		port.enableReceiveThreshold(3);
//		TODO
//		port.enableReceiveTimeout(500);
		
		InputStream in = port.getInputStream();
		OutputStream out = port.getOutputStream();
		
		int retries, read;
		boolean statusOK;
		byte[] buf = new byte[3];
		
		boolean allProgrammed = true;
		for (IHEX8Record rec : program)
		{
			statusOK = false;
			retries = 5;
			while((!statusOK) && retries > 0)
			{
				updateProgress(retries < 5);
				try
				{
					out.write(rec.toByteArray());
					read = in.read(buf);
					if(read <= 0)
						logger.log("Командата за запис е изпратена, но няма отговор след 500 ms. Повторен опит", LogLevel.DEBUG);
					else
					{
						String recv = new String(buf);
						if(recv.equalsIgnoreCase(ProtocolConstants.CMD_ACKNOWLEDGE))
						{
							logger.log("Командата за запис е приета от програматора", LogLevel.DEBUG);
							if(rec.getRecordType() == Type.EOF)
							{
								statusOK = true;
								logger.log("Записът за край на файл е получен успешно", LogLevel.DEBUG);
							}
							else
							{
								logger.log("Очакване на заявка за следващия запис", LogLevel.DEBUG);
								read = in.read(buf);
								if(read <= 0)
									logger.log("Няма отговор след 500 ms. Повторно изпращане на реда", LogLevel.DEBUG);
								else
								{
									recv = new String(buf);
									if(recv.equalsIgnoreCase(ProtocolConstants.CMD_NEXT))
									{
										statusOK = true;
										currentIndex++;
										logger.log("Получена е заявка за следващия ред", LogLevel.DEBUG);
									}
									else
										logger.log("Вместо команда " + ProtocolConstants.CMD_NEXT + " се получи " + recv, LogLevel.DEBUG);
								}
							}
						}
					}
				} catch (IOException ex)
				{
					logger.log("Възникна грешка по време на комуникацията", LogLevel.ERRORS);
					logger.log(ex.getMessage(), LogLevel.DEBUG);
					ex.printStackTrace();
				}
				finally
				{
					retries--;
				}
			}
			if(!statusOK)
			{
				logger.log("Запис номер " + (currentIndex + 1) + " '" + rec.toHEXString() + "' не беше изпратен след 5 неуспешни опита. Прекратяване", LogLevel.ERRORS);
				allProgrammed = false;
				break;
			}
		}
		if(allProgrammed)
			logger.log("Програмирането на микроконтролера завърши успешно", LogLevel.SUCCESS);
		else
			logger.log("Програмирането на микроконтролера не беше успешно", LogLevel.ERRORS);
		taskComplete();
	}
	
	@Override
	public void serialEvent(SerialPortEvent ev)
	{
//		Not used
	}
	
	@Override
	public void taskComplete()
	{
		super.taskComplete();
		progress.dispose();
	}

}
