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

import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import gnu.io.RXTXPort;
import gnu.io.SerialPortEventListener;

/**
 * @author Stanislav Hadjiiski
 * 
 * Simple class to represent task for the programmer like memory erase, records store,
 * verification process, etc. {@link #startTask(RXTXPort)} should be overwritten to enable
 * port specific parameters such as notification on available data, receive timeout and threshold.
 * Events will be delivered by the {@link #serialEvent(gnu.io.SerialPortEvent)} method. Once
 * the task completes be sure to invoke {@link #taskComplete()} method in order to mark it as
 * complete and reset all task specific setting applied to the communication port
 */
public abstract class AProgrammerTask implements SerialPortEventListener
{
	protected RXTXPort port;
	private boolean complete;
	private List<TaskListener> listeners;
	
	public AProgrammerTask()
	{
		listeners = new ArrayList<TaskListener>();
	}
	
	/**
	 * Attaches this task as {@link SerialPortEventListener} to the port specified. Event
	 * processing should be done by overriding {@link #serialEvent(gnu.io.SerialPortEvent)}
	 * @throws TooManyListenersException if there is already a listener assigned to this port
	 */
	public void startTask(RXTXPort port) throws TooManyListenersException
	{
		fireTaskStarting();
		this.complete = false;
		this.port = port;
		this.port.addEventListener(this);
	}
	
	/**
	 * Should be invoked when the task is done. You may override it to disables all event notifications,
	 * and settings that you have previosly made
	 * as {@link SerialPortEventListener} 
	 */
	public void taskComplete()
	{
		fireTaskFinishing();
		this.complete = true;
		port.removeEventListener();
	}
	
	/**
	 * Will return true once the {@link #taskComplete()} method executes marking
	 * the completion of this task
	 * @return whether the task is complete or not
	 */
	public boolean isComplete()
	{
		return this.complete;
	}
	
	/**
	 * Adds the specified listener to the listener list.
	 * @param listener if <code>null</code> {@link NullPointerException} will be thrown
	 */
	public void addTaskListener(TaskListener listener)
	{
		if(listener == null)
			throw new NullPointerException("listener cannot be null");
		listeners.add(listener);
	}
	
	/**
	 * Removes a listener from the listener list
	 * @param listener
	 * @return whether the listener specified was removed
	 */
	public boolean removeTaskListener(TaskListener listener)
	{
		if(listener == null)
			return false;
		return listeners.remove(listener);
	}
	
	private void fireTaskStarting()
	{
		for(TaskListener l: listeners)
			l.taskStarting();
	}
	
	private void fireTaskFinishing()
	{
		for(TaskListener l: listeners)
			l.taskFinishing();
	}
}
