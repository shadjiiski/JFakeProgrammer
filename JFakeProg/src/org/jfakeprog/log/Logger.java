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
package org.jfakeprog.log;

/**
 * A interface defining the logger model. Implementations should make sure the {@link #log(String, LogLevel)}
 * method will output to the log only messages with associated level higher or equal to the level set for the logger 
 * @author Stanislav Hadjiiski
 * @see LogLevel
 * @see DefaultLogger
 */
public interface Logger
{
	/**
	 * Appends the message to the log if its level is higher or equal to the currently set log level
	 * @param message
	 * @param level the level of the message (e.g. {@link LogLevel#SUCCESS})
	 * @return whether the message was appended or not
	 */
	public boolean log(String message, LogLevel level);
	
	/**
	 * @return the currently selected level for this logger
	 */
	public LogLevel getLogLevel();
	
	/**
	 * Sets the level for this logger
	 * @param level
	 */
	public void setLogLevel(LogLevel level);
}
