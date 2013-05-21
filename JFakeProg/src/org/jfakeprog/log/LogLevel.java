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
 * Simple enumeration of possible log levels. Levels are ordered like this <br/>
 * DEBUG < ERRORS < MESSAGES < NONE <br/>
 * Only messages with associated log level above (or equal) to the selected for the {@link Logger}
 * will be displayed
 * @author Stanislav Hadjiiski
 */
public enum LogLevel
{
	DEBUG   (0, "<font color='#202020'><b>" + getTimestampPlaceholder() + "</b>" + spacer() + "<i>" + getMessagePlaceHolder() + "</i></font>"),
	ERRORS  (5, "<font color='red'><b>" + getTimestampPlaceholder() + "</b>" + spacer() + getMessagePlaceHolder() + "</font>"),
	SUCCESS(10, "<font color='green'><b>" + getTimestampPlaceholder() + "</b>" + spacer() + getMessagePlaceHolder() + "</font>"),
	NONE   (15, "<b>" + getTimestampPlaceholder() + "</b>" + spacer() + getMessagePlaceHolder());
	
	private int level;
	private String pattern;
	
	LogLevel(int level, String pattern)
	{
		this.level = level;
		this.pattern = pattern;
	}
	
	/**
	 * Level is used to determine whether to show a message in the log or not. If the log level associated
	 * to the message is greater or equal to the selected log level, the message will be displayed
	 * @return the weight of the selected {@link LogLevel}
	 */
	public int getLevel()
	{
		return level;
	}
	
	/**
	 * A pattern is defined for every log-level. It is basic HTML defining how a message of the {@link LogLevel}
	 * in question should be rendered. The pattern should include placeholders for timestamp and message 
	 * @return the pattern to be used
	 * @see #{@link #getTimestampPlaceholder()}
	 * @see #getMessagePlaceHolder()
	 */
	public String getPattern()
	{
		return pattern;
	}
	
	/**
	 * @return the placeholder string for timestamp in a log message pattern
	 */
	public static String getTimestampPlaceholder()
	{
		return "$TIMESTAMP$";
	}
	
	/**
	 * @return the placeholder string for message in a log message pattern
	 */
	public static String getMessagePlaceHolder()
	{
		return "$MESSAGE";
	}
	
	private static String spacer()
	{
		return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	}
}
