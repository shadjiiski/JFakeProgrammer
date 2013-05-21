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
 * @author Stanislav Hadjiiski
 *
 */
public class DefaultLogger implements Logger
{
	private LogLevel level;
	
	public DefaultLogger()
	{
		this(LogLevel.ERRORS);
	}
	
	public DefaultLogger(LogLevel level)
	{
		this.level = level;
	}
	
	@Override
	public boolean log(String message, LogLevel level)
	{
		if(level.getLevel() < this.level.getLevel())
			return false;
		switch (level)
		{
			case DEBUG:
				System.out.print("[D]");
				break;
			case ERRORS:
				System.out.print("[E]");
				break;
			case SUCCESS:
				System.out.print("[S]");
				break;
			case NONE:
				System.out.print("[N]");
				break;
			default:
				break;
		}
		System.out.println(message);
		return true;
	}

	@Override
	public LogLevel getLogLevel()
	{
		return level;
	}

	@Override
	public void setLogLevel(LogLevel level)
	{
		this.level = level;
	}

}
