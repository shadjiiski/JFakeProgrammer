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
package org.jfakeprog.connection;

/**
 * @author Stanislav Hadjiiski
 *
 */
public interface ProtocolConstants
{
	public static final char MARK_GENERAL =	'=';
	public static final char MARK_RECORD  =	':';
	public static final char MARK_ERASE   =	'-';
	public static final char MARK_READ    =	'+';
	public static final char MARK_DEBUG   =	'?';
	
	public static final String CMD_ACKNOWLEDGE=	"AKN";
	public static final String CMD_RESEND     = "RES";
	public static final String CMD_ERASE      = "DEL";
	public static final String CMD_UNKNOWN    = "UNK";
	public static final String CMD_VERIFY     = "VER";
	public static final String CMD_NEXT       = "NXT";

	public static final String CMD_DISCOVER   = "DSC";
	public static final String CMD_JFAKE_PROG = "JFP";
	public static final String CMD_MEMORY_4KB = "LRG";
	public static final String CMD_MEMORY_2KB = "SML";

	public static final String CMD_DEBUG_PING = "PIN";
	public static final String CMD_DEBUG_PONG = "PON";
	public static final String CMD_DEBUG_DUMP = "DMP";

	public static final String CMD_ERROR   = "ERR";
	public static final String CMD_SUCCESS = "SCS";
}
