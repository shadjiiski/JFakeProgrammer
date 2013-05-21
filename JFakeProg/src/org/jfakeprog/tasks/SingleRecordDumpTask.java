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
import org.jfakeprog.hex.DataRecord;
import org.jfakeprog.hex.EndOfFileRecord;
import org.jfakeprog.hex.IHEX8Record.Type;
import org.jfakeprog.log.DefaultLogger;
import org.jfakeprog.log.LogLevel;
import org.jfakeprog.log.Logger;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class SingleRecordDumpTask extends AProgrammerTask
{
	private Logger logger;
	private ProgressDialog progress;
	
	public SingleRecordDumpTask(Frame owner, Logger logger)
	{
		super();
		if(logger != null)
			this.logger = logger;
		else
			this.logger = new DefaultLogger();
		
		progress = new ProgressDialog(owner);
		progress.setTitle("Извличане на текущ запис");
		progress.setProgressText("Извличане на записа");
		progress.setIndeterminate(true);
	}
	
	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		super.startTask(port);
		port.enableReceiveTimeout(500);
		port.enableReceiveThreshold(14);
		
		OutputStream out = port.getOutputStream();
		InputStream in = port.getInputStream();
		
		boolean statusOK = false;
		int retries = 5;
		while((!statusOK) && retries > 0)
		{
			try
			{
				logger.log("Изпращане на команда за извличане на единичен запис", LogLevel.DEBUG);
				out.write(ProtocolConstants.MARK_DEBUG);
				out.write(ProtocolConstants.CMD_DEBUG_DUMP.getBytes());
				
				Thread.sleep(100); // waste some time
				in.read(); // the record mark
				int len = in.read();
				int calcCheck = len;
				int buf = in.read();
				int address = buf << 8;
				calcCheck += buf;
				buf = in.read();
				address += buf;
				calcCheck += buf;
				int type = in.read();
				calcCheck += type;
				byte[] data = new byte[len];
				if(type == Type.DATA.getCode())
				{
					for(int i = 0; i < len; i++)
					{
						data[i] = (byte) in.read();
						calcCheck += data[i];
					}
				}
				int chkSum = in.read();
				calcCheck = (0x100 - (0xFF & calcCheck)) & 0xFF;
				if(chkSum != calcCheck)
					logger.log("Чексумите не съвпадат. Повторно извикване.", LogLevel.DEBUG);
				else
				{
					statusOK = true;
					if(type == Type.DATA.getCode())
						logger.log("В паметта на микроконтролера е записано " + new DataRecord(address, data, (byte) chkSum).toHEXString(), LogLevel.SUCCESS);
					else
						logger.log("В паметта на микроконтролера е записано " + new EndOfFileRecord().toHEXString(), LogLevel.SUCCESS);
				}
				retries--;
				
			} catch (IOException e)
			{
				logger.log("Възникна грешка при извличането на записа", LogLevel.ERRORS);
				logger.log(e.getMessage(), LogLevel.DEBUG);
				e.printStackTrace();
			} catch (InterruptedException e){}
		}
		if(!statusOK)
			logger.log("Отказ след 5 последователни неуспешни опита", LogLevel.ERRORS);
		taskComplete();
	}
	
	@Override
	public void taskComplete()
	{
		super.taskComplete();
		progress.dispose();
	}
	
	@Override
	public void serialEvent(SerialPortEvent ev)
	{
//		Not used
	}

}
