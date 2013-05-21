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
package org.jfakeprog.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

import org.jfakeprog.log.LogLevel;
import org.jfakeprog.log.Logger;

/**
 * A {@link Logger} implementation
 * @author Stanislav Hadjiiski
 */
public class LoggerPane extends JTextPane implements Logger
{
	private static final long serialVersionUID = -7079644566279261880L;

	private LogLevel logLevel;
	private Calendar calendar;
	
	public LoggerPane()
	{
		this(LogLevel.ERRORS);
	}
	
	public LoggerPane(LogLevel level)
	{
		super();
		this.logLevel = level;
		calendar = Calendar.getInstance();
		setContentType("text/html");
		final JPopupMenu menu = new JPopupMenu();
		Action copy = getActionMap().get(DefaultEditorKit.copyAction);
		copy.putValue(Action.NAME, "Копиране");
		Action clear = new AbstractAction("Изчистване")
		{
			private static final long serialVersionUID = -6643983496031385283L;
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setText("");
			}
		};
		menu.add(copy);
		menu.add(clear);
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())
					menu.show(LoggerPane.this, e.getX(), e.getY());
			}
		});
	}
	
	@Override
	public boolean log(String msg, LogLevel significance)
	{
		//ignore non-significant messages
		if(significance.getLevel() < logLevel.getLevel())
			return false;
		calendar.setTimeInMillis(System.currentTimeMillis());
		String timestamp = String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
		//replace timestamp and message placeholders with appropriate values
		msg = significance.getPattern().replace(LogLevel.getTimestampPlaceholder(), timestamp).replace(LogLevel.getMessagePlaceHolder(), msg);
		StringReader reader = new StringReader(msg);
		Document doc = getStyledDocument();
		try
		{
			//"read" the new message at the end of the document
			getEditorKit().read(reader, doc, doc.getLength());
			setCaretPosition(doc.getLength());
		} catch (IOException | BadLocationException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			reader.close();
		}
		return true;
	}
	
	@Override
	public LogLevel getLogLevel()
	{
		return logLevel;
	}
	
	@Override
	public void setLogLevel(LogLevel level)
	{
		logLevel = level;
	}
}
