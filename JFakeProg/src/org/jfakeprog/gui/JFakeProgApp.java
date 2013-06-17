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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicToolBarUI;
import javax.swing.text.DefaultEditorKit;

import org.jfakeprog.connection.ConnectionHandler;
import org.jfakeprog.hex.IHEX8Record;
import org.jfakeprog.hex.util.HEXFileParser;
import org.jfakeprog.hex.util.HEXRecordSet;
import org.jfakeprog.log.LogLevel;
import org.jfakeprog.tasks.EraseMemoryTask;
import org.jfakeprog.tasks.MemoryReadTask;
import org.jfakeprog.tasks.PingPongTask;
import org.jfakeprog.tasks.ProgramTask;
import org.jfakeprog.tasks.SingleRecordDumpTask;
import org.jfakeprog.tasks.TaskListener;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class JFakeProgApp extends JFrame
{
	private static final long serialVersionUID = 3680371570177832025L;
	
	private enum MemorySize
	{
		LARGE(4), SMALL(2);
		private int size;
		
		MemorySize(int size)
		{
			this.size = size;
		}
		
		public int getMemSize()
		{
			return size;
		}
	}
	
	private MemorySize memorySize;
	
	// various actions for visual components
	private Action fileLoadAction;
	private Action fileSaveAction;
	private Action readAction;
	private Action programAction;
	private Action verifyAction;
	private Action blankCheckAction;
	private Action eraseAction;
	private Action lockAction;
	private Action debugSwitchAction;
	
	/**
	 * Used to simplify connections with the programmer and task scheduling
	 */
	private ConnectionHandler connectionHandler;
	
	/**
	 * When set, debugMessages will be displayed in the log. Also shows the debug toolbar
	 */
	private boolean debugMode;
	/**
	 * Toolbat with debug actions
	 */
	private JToolBar debugTools;

	private LoggerPane logger;
	
	/**
	 * contents of a hex file parsed to e {@link HEXRecordSet}. This is what is stored in the memory of the PC
	 */
	private HEXRecordSet hexRecords;
	
	/**
	 *  Create and display simple GUI for programmer control
	 */
	public JFakeProgApp()
	{
		super("JFake Programmer for AT89Cx051");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				exiting();
			}
		});
		
		memorySize = MemorySize.LARGE;
//		connectionHandler = new ConnectionHandler();
		connectionHandler = new ConnectionHandler("COM16");
//		connectionHandler.connect();
//		System.out.println("connected = " + connectionHandler.isConnected());
		
		initActions();
		getContentPane().add(createMainPanel());
		setJMenuBar(createJMenuBar());
		pack();
		
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * When called all global Action variables are initialized and ready to use
	 */
	private void initActions()
	{
		fileLoadAction   = new FileLoadAction();
		eraseAction      = new EraseAction();
		blankCheckAction = new BlankCheckAction();
		readAction       = new MCUReadAction();
		fileSaveAction   = new FileSaveAction();
		programAction    = new MCUProgramAction();
		verifyAction     = new VerifyAction();
		lockAction       = new LockAction();
		debugSwitchAction= new DebugCheckboxAction();
		
		((DebugCheckboxAction) debugSwitchAction).setSelected(debugMode);
		fileSaveAction.setEnabled(false);
		programAction.setEnabled(false);
		verifyAction.setEnabled(false);
//		TODO enable it once it is implemented in the ASM project
		lockAction.setEnabled(false);
	}
	
	/**
	 * Initializes various control elements such as file load buttons, programmer command buttons (erase,
	 * blank check, program, verify, read) etc.
	 * @return a {@link JPanel} instance, holding the GUI interface
	 */
	private JPanel createMainPanel()
	{
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JToolBar tools = new JToolBar(JToolBar.VERTICAL);
		tools.add(fileLoadAction);
		tools.addSeparator();
		tools.add(eraseAction);
		tools.add(blankCheckAction);
		tools.addSeparator();
		tools.add(readAction);
		tools.add(fileSaveAction);
		tools.addSeparator();
		tools.add(programAction);
		tools.add(verifyAction);
		tools.addSeparator();
		tools.add(lockAction);
		
		debugTools = new JToolBar();
		debugTools.setUI(new DocklessToolbarUI());
		debugTools.setOrientation(JToolBar.HORIZONTAL);
		debugTools.add(new DebugDiscoverAction());
		debugTools.add(new DebugPingPongAction());
		debugTools.add(new DebugMemDumpAction());
		debugTools.setVisible(debugMode);
		
		logger = new LoggerPane(LogLevel.ERRORS);
		logger.setPreferredSize(new Dimension(500, 300));
		logger.setEditable(false);
		
		mainPanel.add(tools, BorderLayout.WEST);
		mainPanel.add(debugTools, BorderLayout.SOUTH);
		mainPanel.add(new JScrollPane(logger), BorderLayout.CENTER);
		
		return mainPanel;
	}
	
	/**
	 * 
	 * @return a {@link JMenuBar} instance to be set
	 */
	private JMenuBar createJMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu file = new JMenu("File");
		file.setMnemonic('f');
		file.add(fileLoadAction);
		file.add(fileSaveAction);
		file.addSeparator();
		file.add(new ExitAction());
		
		JMenu prog = new JMenu("Programmer");
		prog.setMnemonic('p');
		
		prog.add(new JCheckBoxMenuItem(debugSwitchAction));
		prog.add(new SettingsAction());
		prog.addSeparator();
		prog.add(blankCheckAction);
		prog.add(verifyAction);
		prog.addSeparator();
		prog.add(eraseAction);
		prog.add(readAction);
		prog.add(programAction);
		prog.addSeparator();
		prog.add(lockAction);
		
		JMenu help = new JMenu("Help");
		help.setMnemonic('h');
		help.add(new AboutAction());
		help.add(new HelpAction());
		
		menuBar.add(file);
		menuBar.add(prog);
		menuBar.add(help);
		return menuBar;
	}
	
	/**
	 * Invoked before program exit
	 */
	private void exiting()
	{
		if(connectionHandler != null && connectionHandler.isConnected())
			connectionHandler.disconnect(); // free the port before exit
	}
	
	/**
	 * Just create new instance of the programmer GUI
	 * @param args
	 */
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				new JFakeProgApp();
			}
		});
	}
	
	private class DocklessToolbarUI extends BasicToolBarUI
	{
		@Override
		public boolean canDock(Component c, Point p)
		{
			return false;
		}

		@Override
		public void setFloating(boolean b, Point p)
		{
			super.setFloating(b, p);
			debugTools.setVisible(b);
			((DebugCheckboxAction)debugSwitchAction).setSelected(b);
		}

		@Override
		protected WindowListener createFrameListener()
		{
			return new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					setFloating(false, null);
				}
			};
		}
	}
	
	private class DebugCheckboxAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 7268422887042103165L;

		public DebugCheckboxAction()
		{
			super(TITLE_DEBUG, DESC_DEBUG, MNEMONIC_DEBUG, ICON_SMALL_DEBUG, null, ACCELERATOR_DEBUG);
			putValue(SELECTED_KEY, debugMode);
		}
		
		public void setSelected(boolean selected)
		{
			debugMode = selected;
			if(logger != null)
				logger.setLogLevel(selected ? LogLevel.DEBUG : LogLevel.ERRORS);
			putValue(SELECTED_KEY, selected);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			debugMode = !debugMode;
			debugTools.setVisible(debugMode);
			BasicToolBarUI ui = (BasicToolBarUI) debugTools.getUI();
			if(debugMode)
			{
				ui.setFloatingLocation(getLocationOnScreen().x, getLocationOnScreen().y);
				ui.setFloating(true, null);
			}
			else
				ui.setFloating(false, null);
		}
	}

	private class DebugDiscoverAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 7268422887042103165L;
		
		public DebugDiscoverAction()
		{
			super(TITLE_DISCOVER, (char)-1, null, ICON_DISCOVER);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			new Thread()
			{
				public void run()
				{
					if(connectionHandler.isConnected())
						connectionHandler.disconnect(); // disconnecting to free the port
					ProgressDialog dialog = new ProgressDialog(JFakeProgApp.this);
					dialog.setTitle("Откриване на порт");
					dialog.setProgressText("Търсене на JFake Programmer");
					dialog.setIndeterminate(true);
					dialog.setVisible(true);
					
					String oldPort = connectionHandler.getPrefferedPortName();
					connectionHandler.setPrefferedPortName(null);
					connectionHandler.connect();
					if(connectionHandler.isConnected())
						logger.log("Програматор JFakeProgrammer беше открит на порт " + connectionHandler.getPortName(), LogLevel.SUCCESS);
					else
						logger.log("Програматор JFake Programmer не беше открит", LogLevel.ERRORS);
					connectionHandler.setPrefferedPortName(oldPort);
					dialog.dispose();
				};
			}.start();
		}
	}

	private class DebugPingPongAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 7268422887042103165L;
		
		public DebugPingPongAction()
		{
			super(TITLE_PING, (char)-1, null, ICON_PING);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					connectionHandler.addAProgrammerTask(new PingPongTask(JFakeProgApp.this, logger));
				}
			});
		}
	}
	
	private class DebugMemDumpAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 7268422887042103165L;
		
		public DebugMemDumpAction()
		{
			super(TITLE_MEM_DUMP, (char)-1, null, ICON_MEM_DUMP);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					connectionHandler.addAProgrammerTask(new SingleRecordDumpTask(JFakeProgApp.this, logger));
				}
			});
		}
	}
	
	private class HelpAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 7268422887042103165L;
		
		public HelpAction()
		{
			super(TITLE_HELP, MNEMONIC_HELP, ICON_SMALL_HELP, (String)null);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// TODO Auto-generated method stub
			System.out.println("Help -> Help action");
		}
	}
	
	private class AboutAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 7268422887042103165L;
		private JScrollPane message; 
		private ImageIcon icon;
		
		public AboutAction()
		{
			super(TITLE_ABOUT, MNEMONIC_ABOUT, ICON_SMALL_ABOUT, (String)null);
			icon = new ImageIcon(getClass().getResource("icons/32/about.png"));
			final JEditorPane textPane = new JTextPane();
			textPane.setContentType("text/html");
			textPane.setText("<html>" +
					"<p style='text-align: justify; text-indent: 50px;'>" +
					"JFake Programmer е java приложение, осъществяващо връзка с програматор за 2KB и 4KB версии " +
					"на микроконтролерите AT89Cx051. Протоколът за комуникация не е напълно стандартен, затова приложението " +
					"е полезно само ако разполагате и с нашия хардуер. Приложението е безплатно, лицензирано под GPL version 3" +
					"лиценз. Иконките към приложението са предоставени от Калоян Генков, лицензирани под Creative Commons Attribution-ShareAlike лиценз." +
					"За работата си приложението разчита на библиотеката за серийна комуникация <a href='https://code.google.com/p/nrjavaserial/'>nrjavaserial</a>" +
					"(LGPL v2.1), която е един от портовете на библиотеката <a href='http://rxtx.qbang.org/'>RXTX</a> (LGPL)" +
					"</p>" +
					"<p style='text-align: justify; text-indent: 50px;'>" +
					"<b>Функции, поддържани от програмата:</b>" +
					"</p>" +
					"<ul>" +
					"<li>Изтриване паметта на микроконтролер</li>" +
					"<li>Прочитане паметта на микроконтролер и записване във файл</li>" +
					"<li>Програмиране на микроконтролер при наличие на IHEX8 файл (Intel HEX 8-bit; *.hex)</li>" +
					"<li>Проверка дали паметта на микроконтролер е изтрита</li>" +
					"<li>Проверка дали записаната в микроконтролера програма отговаря на тази от hex файл</li>" +
					"<li>Бъдеща поддръжка на заключване на паметта (възпрепятстване на четенето ѝ)</li>" +
					"</ul>" +
					"<p style='text-align: justify; text-indent: 50px;'>" +
					"<b>Авторски колектив</b>" +
					"</p>" +
					"<ul>" +
					"<li><b>Станислав Хаджийски</b> - софтуерна разработка на настолното приложение; разработка на кода, записан в програматора</li>" +
					"<li><b>Мартин Йорданов</b> - разработка на кода, записан в микроконтролера</li>" +
					"<li><b>Калоян Генков</b> - разработка на иконките, използвани в настолното приложение</li>" +
					"</ul>" +
					"<p style='text-align: justify; text-indent: 50px;'>" +
					"В настоящия момент (месец май, 2013 година) ние сме студенти от III курс, учим във физическия факултет на Софийски Университет " +
					"\"Св. Климент Охридски\" в специалности <b>физика</b> и <b>инженерна физика</b>, а програматорът е разработен като курсов проект за " +
					"предмета \"Информационни Технологии в Микроелектрониката\", воден от доц. д-р Стоян Русев и гл. ас. д-р Гичка Цуцуманова, катедра Физика на " +
					"твърдото тяло и микроелектрониката." +
					"</p>" +
					"<p style='text-align: justify; text-indent: 50px;'>" +
					"<b>Връзка с нас:</b>" +
					"</p>" +
					"<ul>" +
					"<li><a href='mailto:shadjiiski" + ((char)64) + "gmail.com'<b>Станислав Хаджйиски</b> - shadjiiski" + ((char)64) + "gmail.com</a></li>" +    //'at' masquerading
					"<li><a href='mailto:martin.r.yordanov" + ((char)64) + "gmail.com'<b>Мартин Йорданов</b> - martin.r.yordanov" + ((char)64) + "gmail.com</a></li>" +
					"<li><a href='mailto:kvgkvg" + ((char)64) + "gmail.com'><b>Калоян Генков</b> - kvgkvg" + ((char)64) + "gmail.com</a></li>" +
					"</ul>" +
					"</html>");
			textPane.addHyperlinkListener(new HyperlinkListener()
			{
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e)
				{
					if(e.getEventType() != EventType.ACTIVATED)
						return;
					if(!Desktop.isDesktopSupported())
					{
						System.err.println("Desktop is not supported");
						return;
					}
					Desktop desktop = Desktop.getDesktop();
					if(!desktop.isSupported(Desktop.Action.BROWSE))
					{
						System.err.println("Browse action is not supported");
						return;
					}
					try
					{
						desktop.browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException ex)
					{
						ex.printStackTrace();
					}
					
				}
			});
			textPane.setEditable(false);
			textPane.setPreferredSize(new Dimension(600, 400));
			message = new JScrollPane(textPane);
			final JPopupMenu popup = new JPopupMenu();
			
			Action copy = new DefaultEditorKit.CopyAction();
			copy.putValue(Action.NAME, "Копиране");
			textPane.getActionMap().put(DefaultEditorKit.copyAction, copy);
			popup.add(copy);
			textPane.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent e)
				{
					if(e.isPopupTrigger())
						popup.show(textPane, e.getX(), e.getY());
				}
			});
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JOptionPane.showMessageDialog(JFakeProgApp.this, message, "Относно JFake Programmer", JOptionPane.INFORMATION_MESSAGE, icon);
		}
	}
	
	private class ExitAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 6755333542362697917L;

		public ExitAction()
		{
			super(TITLE_EXIT, TITLE_EXIT, MNEMONIC_EXIT, ICON_SMALL_EXIT, null, ACCELERATOR_EXIT);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			exiting();
			System.exit(0);
		}
	}
	
	private class FileLoadAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 3478160128396411L;
		private JFileChooser fileChooser;

		public FileLoadAction()
		{
			super(TITLE_FILE_LOAD, DESC_FILE_LOAD, MNEMONIC_FILE_LOAD, ICON_FILE_LOAD, ACCELERATOR_LOAD);
			fileChooser = new JFileChooser(System.getProperty("user.dir"));
			fileChooser.setAcceptAllFileFilterUsed(true);
			HEXFileFilter filter = new HEXFileFilter();
			fileChooser.addChoosableFileFilter(filter);
			fileChooser.setFileFilter(filter);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int option = fileChooser.showOpenDialog(JFakeProgApp.this);
			if(option != JFileChooser.APPROVE_OPTION)
				return;
			File hexFile = fileChooser.getSelectedFile();
			try
			{
				hexRecords = new HEXFileParser(hexFile).parse();
				
//				There's a program in the memory so file save, program and verify should be enabled now
				fileSaveAction.setEnabled(true);
				programAction.setEnabled(true);
				verifyAction.setEnabled(true);
				
				logger.log("Файлът " + hexFile.getName() + " e зареден в паметта. Може да го запрограмирате.", LogLevel.SUCCESS);
			} catch (IOException ex)
			{
				ex.printStackTrace();
				logger.log("Възникна грешка при зареждане на " + hexFile.getName(), LogLevel.ERRORS);
				logger.log(ex.getMessage(), LogLevel.DEBUG);
			}
		}
	}
	
	private class FileSaveAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 1078867046919028755L;
		private JFileChooser fileChooser;
		public FileSaveAction()
		{
			super(TITLE_FILE_SAVE, DESC_FILE_SAVE, MNEMONIC_FILE_SAVE, ICON_FILE_SAVE, ACCELERATOR_SAVE);
			fileChooser = new JFileChooser(System.getProperty("user.dir"));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			FileFilter filter = new HEXFileFilter();
			fileChooser.addChoosableFileFilter(filter);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileFilter(filter);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			boolean success = false;
			while(!success)
			{
				int option = fileChooser.showSaveDialog(JFakeProgApp.this);
				if(option != JFileChooser.APPROVE_OPTION)
					return;
				File hexFile = fileChooser.getSelectedFile();
				// ensure there is a ".hex" suffix in the filename
				if(!hexFile.getName().toLowerCase().endsWith(".hex"))
					hexFile = new File(hexFile.getAbsolutePath() + ".hex");
				if(hexFile.exists())
				{
					option = JOptionPane.showConfirmDialog(JFakeProgApp.this, "Файлът " + hexFile.getName() + " вече съществува. Записване върху него?", "Внимание", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if(option != JOptionPane.YES_OPTION)
						continue; // show the file chooser dialog again
					//overwrite is confirmed
				}
				success = true;
				PrintWriter pw;
				try
				{
					pw = new PrintWriter(hexFile);
					for(IHEX8Record record: hexRecords)
						pw.println(record.toHEXString());
					pw.close();
					logger.log("Съдържанието на паметта беше записано в " + hexFile.getName(), LogLevel.SUCCESS);
				} catch (FileNotFoundException ex)
				{
					logger.log("Възникна грешка при записване на файла " + hexFile.getName(), LogLevel.ERRORS);
					logger.log(ex.getMessage(), LogLevel.DEBUG);
					ex.printStackTrace();
				}
			}
		}
	}
	
	private class VerifyAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 1903082924201854156L;

		public VerifyAction()
		{
			super(TITLE_VERIFY, DESC_VERIFY, MNEMONIC_VERIFY, ICON_VERIFY);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final MemoryReadTask task = new MemoryReadTask(JFakeProgApp.this, logger, memorySize.getMemSize() * 1024);
			task.addTaskListener(new TaskListener()
			{
				@Override
				public void taskStarting(){}
				
				@Override
				public void taskFinishing()
				{
					if(!task.isTaskSuccessfull())
					{
						logger.log("Възникна грешка при прочитането на паметта на микроконтролера", LogLevel.ERRORS);
						return;
					}
					logger.log("Паметта на микроконтролера е успешно прочетена. Сравняване с записаната в RAM", LogLevel.DEBUG);
					// data bytes of what is stored in RAM
					byte[] ramData = new byte[memorySize.getMemSize() * 1024];
					//initial erased memory
					for(int i = 0; i < ramData.length; i++)
							ramData[i] = (byte) 0xFF;
					for (IHEX8Record rec : hexRecords)
					{
						if(rec.getRecordType() == IHEX8Record.Type.EOF)
							continue;
						byte[] data = rec.getRecordData();
						for(int i = 0; i < data.length; i++)
							ramData[rec.getLoadOffset() + i] = data[i];
					}

					//check integrity
					HEXRecordSet memory = task.getMemory();
					int idx = 0;
					for (IHEX8Record rec : memory)
					{
						if(rec.getRecordType() == IHEX8Record.Type.EOF)
							continue;
						for(byte b: rec.getRecordData())
						{
							if(ramData[idx] != b)
							{
								logger.log(String.format("Паметта на микроконтролера съдържа на адрес 0x%04x стойност 0x%02x вместо 0x%02x", idx, b, ramData[idx]), LogLevel.DEBUG);
								logger.log("Програмата в ROM на микроконтролера не съвпада със заредената в RAM", LogLevel.ERRORS);
								return;
							}
							idx++;
						}
					}
					logger.log("Програмата в ROM на микроконтролера съвпада с заредената в RAM", LogLevel.SUCCESS);
				}
			});
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					connectionHandler.addAProgrammerTask(task);
				}
			});
		}
	}
	
	private class BlankCheckAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 3081942666257651199L;

		public BlankCheckAction()
		{
			super(TITLE_BLANK, DESC_BLANK, MNEMONIC_BLANK, ICON_BLANK);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final MemoryReadTask task = new MemoryReadTask(JFakeProgApp.this, logger, memorySize.getMemSize() * 1024);
			task.addTaskListener(new TaskListener()
			{
				@Override
				public void taskStarting(){}
				
				@Override
				public void taskFinishing()
				{
					if(!task.isTaskSuccessfull())
					{
						logger.log("Възникна грешка при четенето на паметта на микроконтролера", LogLevel.ERRORS);
						return;
					}
					logger.log("Паметта на микроконтролера е успешно прочетена. Проверка за изтрита памет.", LogLevel.DEBUG);
					
					HEXRecordSet memory = task.getMemory();
					for (IHEX8Record rec : memory)
					{
						if(rec.getRecordType() == IHEX8Record.Type.EOF)
							continue;
						byte[] data = rec.getRecordData(); // if memory is erased, all bytes should be 0xFF
						for (int i = 0; i < data.length; i++)
						{
							byte b = data[i];
							if(b != (byte)0xFF)
							{
								int address = i + rec.getLoadOffset();
								logger.log("Паметта на устройството не е в изтрито състояние", LogLevel.ERRORS);
								logger.log("Паметта на адрес 0x" + String.format("%04x", address) + " съдържа 0x" + String.format("%02x", b) + " вместо 0xFF", LogLevel.DEBUG);
								return;
							}
						}
					}
					logger.log("Паметта на устройството е в изтрито състояние", LogLevel.SUCCESS);
				}
			});
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					connectionHandler.addAProgrammerTask(task);
				}
			});
		}
	}

	private class MCUReadAction extends ProgrammerAction
	{
		private static final long serialVersionUID = -3286744909765024942L;

		public MCUReadAction()
		{
			super(TITLE_READ, DESC_READ, MNEMONIC_READ, ICON_READ);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final MemoryReadTask task = new MemoryReadTask(JFakeProgApp.this, logger, memorySize.getMemSize() * 1024);
			task.addTaskListener(new TaskListener()
			{
				@Override
				public void taskStarting() {}
				
				@Override
				public void taskFinishing()
				{
					if(task.isTaskSuccessfull())
					{
						hexRecords = task.getMemory();
						programAction.setEnabled(true);
						fileSaveAction.setEnabled(true);
						verifyAction.setEnabled(true);
						logger.log("Паметта на устройството е прочетена и записана в RAM", LogLevel.SUCCESS);
					}
					else
						logger.log("Паметта на устройството не беше прочетена успешно", LogLevel.ERRORS);
				}
			});
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					connectionHandler.addAProgrammerTask(task);
				}
			});
		}
	}
	
	private class MCUProgramAction extends ProgrammerAction
	{
		private static final long serialVersionUID = -3801761381874075607L;

		public MCUProgramAction()
		{
			super(TITLE_WRITE, DESC_WRITE, MNEMONIC_WRITE, ICON_WRITE);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					connectionHandler.addAProgrammerTask(new ProgramTask(JFakeProgApp.this, logger, hexRecords));
				}
			});
		}
	}
	
	private class EraseAction extends ProgrammerAction
	{
		private static final long serialVersionUID = 962321549799386071L;

		public EraseAction()
		{
			super(TITLE_ERASE, DESC_ERASE, MNEMONIC_ERASE, ICON_ERASE);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					connectionHandler.addAProgrammerTask(new EraseMemoryTask(logger, JFakeProgApp.this));
				}
			});
		}
	}
	
	private class LockAction extends ProgrammerAction
	{
		private static final long serialVersionUID = -7464521334406964953L;

		public LockAction()
		{
			super(TITLE_LOCK, DESC_LOCK, MNEMONIC_LOCK, ICON_LOCK);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// TODO Auto-generated method stub
			System.out.println("Lock Action");
		}
	}
	
	private class SettingsAction extends ProgrammerAction
	{
		private static final long serialVersionUID = -7915865148079267727L;
		private JPanel message;
		private JRadioButton debug, errors, success, none;
		private JComboBox<String> memorySelection;
		private String[] labels;
		public SettingsAction()
		{
			super("Settings", 's', "settings.png", "");
			
			labels = new String[]{"Запис", "Отказ"};
			message = new JPanel(new GridBagLayout());
			Insets insets = new Insets(4, 4, 4, 4);
			
			memorySelection = new JComboBox<String>(new String[]{"4 KB (AT89c4051)", "2 KB (AT89c2051)"});
			
			debug = new JRadioButton("Debug");
			errors = new JRadioButton("Грешки");
			success = new JRadioButton("Успешни");
			none = new JRadioButton("Минимално");
			
			ButtonGroup group = new ButtonGroup();
			group.add(debug);
			group.add(errors);
			group.add(success);
			group.add(none);
			
			message.add(new JLabel("<html><p style='width: 100px'>Памет (Устройство)</p>"), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
			message.add(memorySelection, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
			message.add(new JLabel("<html><p style='width: 100px'>Долно ограничение за нивото на съобщения в лога</p>"), new GridBagConstraints(0, 1, 1, 4, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
			message.add(debug,  new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
			message.add(errors, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
			message.add(success,new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
			message.add(none,   new GridBagConstraints(1, 4, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		}
		
		private void loadSettings()
		{
			memorySelection.setSelectedIndex(memorySize ==  MemorySize.LARGE ? 0 : 1);
			switch (logger.getLogLevel())
			{
				default:
				case DEBUG:
					debug.setSelected(true);
					break;
				case ERRORS:
					errors.setSelected(true);
					break;
				case SUCCESS:
					success.setSelected(true);
					break;
				case NONE:
					none.setSelected(true);
					break;
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			loadSettings();
			int option = JOptionPane.showOptionDialog(JFakeProgApp.this, message, "Настройки", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, labels, labels[0]);
			if(option != JOptionPane.OK_OPTION)
				return;
			
			if(memorySelection.getSelectedIndex() == 0)
				memorySize = MemorySize.LARGE;
			else
				memorySize = MemorySize.SMALL;
			if(debug.isSelected())
				logger.setLogLevel(LogLevel.DEBUG);
			else if(errors.isSelected())
				logger.setLogLevel(LogLevel.ERRORS);
			else if(success.isSelected())
				logger.setLogLevel(LogLevel.SUCCESS);
			else if(none.isSelected())
				logger.setLogLevel(LogLevel.NONE);
		}
		
	}
}
