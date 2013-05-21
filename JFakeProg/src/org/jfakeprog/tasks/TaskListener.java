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

/**
 * @author Stanislav Hadjiiski
 *
 */
public interface TaskListener
{
	/**
	 * Will be invoked once the {@link AProgrammerTask#startTask(gnu.io.RXTXPort)} is executed
	 */
	public void taskStarting();
	
	/**
	 * Will be invoked once the {@link AProgrammerTask#taskComplete()} is executed
	 */
	public void taskFinishing();
}
