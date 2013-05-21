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

import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * @author Stanislav Hadjiiski
 *
 */
public abstract class ProgrammerAction extends AbstractAction
{
	private static final long serialVersionUID = 8087533192624211538L;
	
	public final static String TITLE_VERIFY    = "Verify Data";
	public final static String TITLE_BLANK     = "Check Blank";
	public final static String TITLE_FILE_LOAD = "Load File";
	public final static String TITLE_FILE_SAVE = "Save to File";
	public final static String TITLE_ERASE     = "Erase ROM";
	public final static String TITLE_READ      = "Read ROM";
	public final static String TITLE_WRITE     = "Program ROM";
	public final static String TITLE_LOCK      = "Lock ROM";
	public final static String TITLE_EXIT      = "Exit Application";
	public final static String TITLE_ABOUT     = "About";
	public final static String TITLE_HELP     = "Help";
	//Debug имена
	public final static String TITLE_DEBUG     = "Debug";
	public final static String TITLE_DISCOVER  = "Откриване на кой порт е закачен програматора";
	public final static String TITLE_MEM_DUMP  = "Извеждане в лога на текущия запис в паметта на програматора";
	public final static String TITLE_PING      = "Ping/Pong тест";
	
	public final static String DESC_VERIFY    = "Проверка за съвпадение на текущо заредената програма със запрограмираната в ROM";
	public final static String DESC_BLANK     = "Проверка за изтрит ROM на програмируемото устройство";
	public final static String DESC_FILE_LOAD = "Зареждане на Intel HEX 8-bit файл с програма в паметта";
	public final static String DESC_FILE_SAVE = "Записване на прочетената програма в Intel HEX 8-bit файл локално";
	public final static String DESC_ERASE     = "Изтриване ROM на програмируемото устройство";
	public final static String DESC_READ      = "Прочитане на програма от ROM на програмируемото устройство";
	public final static String DESC_WRITE     = "Записване на текущо заредената програма в ROM на програмируемото устройство";
	public final static String DESC_LOCK      = "Предпазване на ROM на програмируемото устройство от четене";
	//Debug описания
	public final static String DESC_DEBUG     = "При включване се появява Debug набора от инструменти, записват се повече съобщения в лога";
	public final static String DESC_DISCOVER  = "Открива на кой порт е закачен програматор JFakeProgrammer и записва в лога";
	public final static String DESC_PING      = "Изпраща PING команда към програматора. При безпроблемна комуникация трябва да получи PONG команда в отговор";
	public final static String DESC_MEM_DUMP  = "Изисква от програматора да изпрати текущо записания Data record от RAM паметта му";
	
	public static final char MNEMONIC_VERIFY    = 'v';
	public static final char MNEMONIC_BLANK     = 'b';
	public static final char MNEMONIC_FILE_LOAD = 'l';
	public static final char MNEMONIC_FILE_SAVE = 's';
	public static final char MNEMONIC_ERASE     = 'e';
	public static final char MNEMONIC_READ      = 'r';
	public static final char MNEMONIC_WRITE     = 'w';
	public static final char MNEMONIC_LOCK      = 'l';
	public static final char MNEMONIC_DEBUG     = 'd';
	public static final char MNEMONIC_EXIT      = 'x';
	public static final char MNEMONIC_ABOUT     = 'a';
	public static final char MNEMONIC_HELP      = 'h';

	public static final String ICON_VERIFY    = "verify-program.png";
	public static final String ICON_BLANK     = "verify-blank.png";
	public static final String ICON_FILE_LOAD = "file-read.png";
	public static final String ICON_FILE_SAVE = "file-save.png";
	public static final String ICON_ERASE     = "blank.png";
	public static final String ICON_READ      = "read.png";
	public static final String ICON_WRITE     = "program.png";
	public static final String ICON_LOCK      = "lock.png";
	public static final String ICON_HELP      = "help.png";
	
	//additional small icons
	public static final String ICON_SMALL_HELP  = "help.png";
	public static final String ICON_SMALL_DEBUG = "debug.png";
	public static final String ICON_SMALL_ABOUT = "about.png";
	public static final String ICON_SMALL_EXIT  = "close.png";
	
	//additional debug icons
	public static final String ICON_DISCOVER = "discover.png";
	public static final String ICON_PING     = "ping-pong.png";
	public static final String ICON_MEM_DUMP = "dump.png";

	public static final KeyStroke ACCELERATOR_LOAD  = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke ACCELERATOR_SAVE  = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke ACCELERATOR_EXIT  = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke ACCELERATOR_DEBUG = KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK);

	/**
	 * Constructs a {@link AbstractAction} withe NAME set to <code>title</code>, SMALL_ICON and LARGE_ICON_KEY
	 * determined by <code>icon</code>, MNEMONIC_KEY set to <code>mnemonic</code> and ACCELERATOR_KEY set to
	 * <code>accelerator</code>
	 * @param title
	 * @param tooltip
	 * @param mnemonic
	 * @param icon
	 * @param accelerator
	 */
	public ProgrammerAction(String title, String tooltip, char mnemonic, String icon, KeyStroke accelerator)
	{
		this(title, tooltip, mnemonic, icon, icon, accelerator);
	}

	/**
	 * Constructs a {@link AbstractAction} withe NAME set to <code>title</code>, SMALL_ICON and LARGE_ICON_KEY
	 * determined by <code>icon</code>, MNEMONIC_KEY set to <code>mnemonic</code> and no ACCELERATOR_KEY
	 * @param title
	 * @param tooltip
	 * @param mnemonic
	 * @param icon
	 */
	public ProgrammerAction(String title, String tooltip, char mnemonic, String icon)
	{
		this(title, tooltip, mnemonic, icon, icon, null);
	}
	
	/**
	 * Constructs a {@link AbstractAction} with NAME and SHORT_DESCRIPTION set to <code>title</code>, SMALL_ICON and
	 * LARGE_ICON_KEY determined by <code>icon</code> with MNEMONIC_KEY set to <code>mnemonic</code> and the specified
	 * {@link KeyStroke} as accelerator
	 * 
	 * @param title label and tooltip text
	 * @param mnemonic
	 * @param icon small and large icon. There must be an image file with this filename under the icons/16/ and icons/32/
	 * foldersCan be <code>null</code>
	 * @param accelerator the accelerato {@link KeyStroke} e.g. Ctrl + S
	 */
	public ProgrammerAction(String title, char mnemonic, String icon, KeyStroke accelerator)
	{
		this(title, title, mnemonic, icon, icon, accelerator);
	}
	
	/**
	 * Constructs a {@link AbstractAction} with NAME and SHORT_DESCRIPTION set to <code>title</code>, SMALL_ICON and
	 * LARGE_ICON_KEY determined by <code>icon</code> with MNEMONIC_KEY set to <code>mnemonic</code> and no accelerator
	 * 
	 * @param title label and tooltip text
	 * @param mnemonic
	 * @param icon small and large icon. There must be an image file with this filename under the icons/16/ and icons/32/
	 * foldersCan be <code>null</code>
	 */
	public ProgrammerAction(String title, char mnemonic, String icon)
	{
		this(title, title, mnemonic, icon, icon, null);
	}
	
	/**
	 * Constructs a {@link AbstractAction} with NAME and SHORT_DESCRIPTION set to <code>title</code>, SMALL_ICON and
	 * LARGE_ICON_KEY determined by <code>smallIcon</code> and <code>largeIcon</code> with MNEMONIC_KEY set to 
	 * <code>mnemonic</code> and no accelerator
	 * 
	 * @param title label and tooltip text
	 * @param mnemonic
	 * @param smallIcon name of image located under the icons/16/ folder. Can be <code>null</code>
	 * @param largeIcon name of image located under the icons/32/ folder. Can be <code>null</code>
	 */
	public ProgrammerAction(String title, char mnemonic, String smallIcon, String largeIcon)
	{
		this(title, title, mnemonic, smallIcon, largeIcon, null);
	}
	/**
	 * Constructs an {@link AbstractAction} with NAME, SHORT_DESCRIPTION, SMALL_ICON, LARGE_ICON_KEY, MNEMONIC_KEY and
	 * ACCELERATOR_KEY set to the passed values (if not null or zero sized strings)
	 * 
	 * @param title is the action name. Will be used as label for buttons and menu items
	 * @param tooltip is the action short description. Will be used for tooltip texts
	 * @param mnemonic is the mnenmonic char for this action
	 * @param smallIcon name of image, located under the icons/16/ folder. Will be used for menu item icons
	 * @param largeIcon name of image, located under the icons/32/ folder. Will be used for button icons
	 * @param accelerator is the accelerator {@link KeyStroke} e.g. Ctrl + S
	 */
	public ProgrammerAction(String title, String tooltip, char mnemonic, String smallIcon, String largeIcon, KeyStroke accelerator)
	{
		super();
		if(title != null && title.length() > 0)
			putValue(NAME, title);
		if(tooltip != null && tooltip.length() > 0)
			putValue(SHORT_DESCRIPTION, tooltip);
		if(smallIcon != null && smallIcon.length() > 0)
			putValue(SMALL_ICON,     new ImageIcon(getClass().getResource("icons/16/" + smallIcon)));
		if(largeIcon != null && largeIcon.length() > 0)
			putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("icons/32/" + largeIcon)));
		if(mnemonic > 0)
			putValue(MNEMONIC_KEY, KeyEvent.getExtendedKeyCodeForChar(mnemonic));
		if(accelerator != null)
			putValue(ACCELERATOR_KEY, accelerator);
	}
}
