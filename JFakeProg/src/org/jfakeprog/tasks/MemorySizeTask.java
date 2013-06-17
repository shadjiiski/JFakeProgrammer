/**
 * 
 */
package org.jfakeprog.tasks;

import gnu.io.RXTXPort;
import gnu.io.SerialPortEvent;

import java.awt.Frame;
import java.io.IOException;
import java.util.TooManyListenersException;

import org.jfakeprog.connection.ProtocolConstants;
import org.jfakeprog.gui.JFakeProgApp.MemorySize;
import org.jfakeprog.gui.ProgressDialog;
import org.jfakeprog.log.DefaultLogger;
import org.jfakeprog.log.LogLevel;
import org.jfakeprog.log.Logger;


/**
 * @author Stanislav Hadjiiski
 *
 */
public class MemorySizeTask extends AProgrammerTask
{
	private ProgressDialog progress;
	private Logger logger;
	private MemorySize size;
	public MemorySizeTask(Frame owner, Logger logger, MemorySize size)
	{
		this.size = size;
		if(logger == null)
			logger = new DefaultLogger();
		this.logger = logger;
		progress = new ProgressDialog(owner);
		progress.setIndeterminate(true);
		progress.setTitle("Изпращане размер на чипа");
		progress.setProgressText("Изпращане размера на чипа");
	}
	
	@Override
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		progress.setVisible(true);
		super.startTask(port);
		port.enableReceiveThreshold(3);
		port.enableReceiveTimeout(300);
		byte[] buf = new byte[3];
		int retries = 5;
		boolean statusOK = false;
		while((!statusOK) && retries > 0)
		{
			try
			{
				port.getOutputStream().write(ProtocolConstants.MARK_GENERAL);
				if(size == MemorySize.LARGE)
					port.getOutputStream().write(ProtocolConstants.CMD_MEMORY_4KB.getBytes());
				else
					port.getOutputStream().write(ProtocolConstants.CMD_MEMORY_2KB.getBytes());
				int read = port.getInputStream().read(buf);
				if(read < 0)
					logger.log("Командата е изпратена, но няма отговор. Повторно изпращане", LogLevel.DEBUG);
				else
				{
					String recv = new String(buf);
					if(recv.equalsIgnoreCase(ProtocolConstants.CMD_ACKNOWLEDGE))
						statusOK = true;
					else
						logger.log("Получи се грешен отговор: '" + recv + "'. Повторен опит", LogLevel.DEBUG);
				}
			} catch (IOException e)
			{
				logger.log("Възникна грешка при изпращането на размера на чипа", LogLevel.ERRORS);
				logger.log(e.getMessage(), LogLevel.DEBUG);
				e.printStackTrace();
			}
			finally
			{
				retries--;
			}
		}
		if(statusOK)
			logger.log("Размерът на паметта беше изпратен успешно (" + size.getMemSize() + "KB)", LogLevel.SUCCESS);
		else
			logger.log("Прекратяване след 5 поредни неуспешни опита. Размерът на паметта не беше изпратен.", LogLevel.ERRORS);
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
		//Not used, task too simple
	}

}
