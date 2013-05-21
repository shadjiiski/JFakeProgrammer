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

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class ProgressDialog extends Thread
{
	private Frame parent;
	private JDialog dialog;
	private JProgressBar progress;
	
	public ProgressDialog(Frame parent)
	{
		this.parent = parent;
		dialog = new JDialog(parent);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		progress = new JProgressBar();
		progress.setStringPainted(true);
		dialog.add(progress);
	}
	
	public void setMin(int min)
	{
		progress.setMinimum(min);
	}
	
	public void setMax(int max)
	{
		progress.setMaximum(max);
	}
	
	public void setValue(int val)
	{
		progress.setValue(val);
	}
	
	public void setIndeterminate(boolean indeterminate)
	{
		progress.setIndeterminate(indeterminate);
	}
	
	public void setProgressText(String text)
	{
		progress.setString(text);
	}
	
	public void setTitle(String title)
	{
		dialog.setTitle(title);
	}
	
	@Override
	public void run()
	{
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
	
	public void setVisible(boolean visible)
	{
		if(visible)
			show();
		else
			hide();
	}
	
	private void show()
	{
		start();
	}
	
	private void hide()
	{
		dialog.setVisible(false);
	}
	
	public void dispose()
	{
		dialog.dispose();
	}
}
