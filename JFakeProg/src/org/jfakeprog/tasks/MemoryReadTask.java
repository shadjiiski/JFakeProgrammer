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
import org.jfakeprog.hex.IHEX8Record;
import org.jfakeprog.hex.util.HEXRecordSet;
import org.jfakeprog.log.DefaultLogger;
import org.jfakeprog.log.LogLevel;
import org.jfakeprog.log.Logger;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class MemoryReadTask extends AProgrammerTask
{
	private HEXRecordSet memory;
	private Logger logger;
	private ProgressDialog progress;
	private boolean success;
	
	private int currentIndex;
	private int size;
	
	public MemoryReadTask(Frame parent, Logger logger)
	{
		this(parent, logger, -1);
	}
	
	public MemoryReadTask(Frame parent, Logger logger, int memSize)
	{
		if(logger == null)
			logger = new DefaultLogger();
		this.logger = logger;
		this.size = memSize / 8;
		progress = new ProgressDialog(parent);
		progress.setIndeterminate(true);
		progress.setTitle("Прочитане на ROM на микроконтролер");
		progress.setProgressText("Изпращане на команда за четене");
	}
	
	private void updateProgress()
	{
		if(size <= 0)
		{
			if(currentIndex == 0)
				progress.setProgressText("Получаване на записи");
			return;
		}
		if(currentIndex == 0)
		{
			progress.setMin(0);
			progress.setMax(size);
			progress.setValue(0);
			progress.setProgressText("Получаване на записи");
			progress.setIndeterminate(false);
		}
		progress.setValue(currentIndex + 1);
		progress.setProgressText("Получаване на запис " + Math.min((currentIndex + 1), size) + "/" + size);
	}
	
	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		progress.setVisible(true);
		super.startTask(port);
		memory = new HEXRecordSet();
		
		OutputStream out = port.getOutputStream();
		InputStream in = port.getInputStream();
		
		byte[] buf = new byte[3];
		try
		{
			port.enableReceiveTimeout(300);
			port.enableReceiveThreshold(3);
			boolean statusOK = false;
			int retriesLeft = 5;
			while((!statusOK) && retriesLeft > 0)
			{
				out.write(ProtocolConstants.MARK_READ);
				out.write(ProtocolConstants.CMD_VERIFY.getBytes());
				logger.log("Командата за четене е изпратена", LogLevel.DEBUG);
				
				Thread.sleep(100); // waste some time to make sure all is read
				int read = in.read(buf);
				String received = new String(buf).toUpperCase();
				if(read <= 0)
					logger.log("След 300 ms не беше получен отговор. Повторен опит", LogLevel.DEBUG);
				else if(received.equalsIgnoreCase(ProtocolConstants.CMD_ACKNOWLEDGE))
				{
					statusOK = true;
					logger.log("Командата за четене е успешно получена", LogLevel.DEBUG);
					// event-based read of data
					port.notifyOnDataAvailable(true);
					port.disableReceiveTimeout();
					port.enableReceiveThreshold(14); // max of 8 data bytes and 6 meta bytes are send
					updateProgress();
				}
				else
				{
					logger.log("Командата за четене е изпратена, но не е разпозната", LogLevel.ERRORS);
					logger.log("Отговора е " + received, LogLevel.DEBUG);
				}
				retriesLeft--;
			}
			if(!statusOK)
			{
				logger.log("5 поредни неуспешни опита. Отказ от задачата", LogLevel.ERRORS);
				taskComplete();
				return;
			}
				
		} catch (IOException ex)
		{
			logger.log("Възникна грешка при четенето на паметта", LogLevel.ERRORS);
			taskComplete();
			ex.printStackTrace();
		} catch (InterruptedException e){}
	}
	
	/**
	 * When the task completes this function may be invoked to get information about whether the
	 * task completed successfully or not
	 * @return whether the task was successful
	 */
	public boolean isTaskSuccessfull()
	{
		return success;
	}
	
	public HEXRecordSet getMemory()
	{
		return memory;
	}
	
	@Override
	public void taskComplete()
	{
		port.notifyOnDataAvailable(false);
		super.taskComplete();
		progress.dispose();
	}
	
	@Override
	public void serialEvent(SerialPortEvent ev)
	{
		if(ev.getEventType() != SerialPortEvent.DATA_AVAILABLE)
			return;
		InputStream in = port.getInputStream();
		boolean eof = false;
		try
		{
			in.read(); //reads the record mark
			int calculatedChkSum = 0;
			int len = in.read();
			int buf = in.read();
			calculatedChkSum = len + buf;
			int address = buf << 8;
			buf = in.read();
			calculatedChkSum += buf;
			address += buf;
			int type = in.read();
			if(type == IHEX8Record.Type.EOF.getCode())
				eof = true;
			calculatedChkSum += type;
			byte[] data = new byte[len];
			if(!eof)
			{
				for(int i = 0; i < len; i++)
				{
					data[i] = (byte) in.read();
					calculatedChkSum += data[i];
				}
			}
			int chksum = in.read();
			logger.log("Получен запис. Проверяване на чексумите", LogLevel.DEBUG);
			calculatedChkSum = (0x100 - (0xFF & calculatedChkSum)) & 0xFF;
			if(chksum != calculatedChkSum)
			{
				logger.log("Чексумите не съвпадат. Повторно поискване", LogLevel.DEBUG);
				port.getOutputStream().write(ProtocolConstants.CMD_RESEND.getBytes()); // sending resend request
				return;
			}
			if(!eof)
			{
				logger.log("Полученият запис е правилен. Добавяне в паметта и поискване на следващия", LogLevel.DEBUG);
				memory.add(new DataRecord(address, data, (byte) chksum));
				currentIndex++;
				updateProgress();
				port.getOutputStream().write(ProtocolConstants.CMD_ACKNOWLEDGE.getBytes());
				port.getOutputStream().write(ProtocolConstants.CMD_NEXT.getBytes());
			}
			else
			{
				port.getOutputStream().write(ProtocolConstants.CMD_ACKNOWLEDGE.getBytes());
				logger.log("Полученият запис е правилен. Добавяне на запис за край на файла", LogLevel.DEBUG);
				memory.add(new EndOfFileRecord());
				success = true; // task successfully ended
				taskComplete();
			}
		} catch (IOException e)
		{
			logger.log("Възникна грешка при получаването на записи от програматора", LogLevel.ERRORS);
			e.printStackTrace();
		}
	}
}
