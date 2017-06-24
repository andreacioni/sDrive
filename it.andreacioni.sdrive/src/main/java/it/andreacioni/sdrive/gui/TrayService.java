package it.andreacioni.sdrive.gui;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import it.andreacioni.sdrive.utils.ImageUtils;

public class TrayService implements Runnable {

	private UploadWindow uploadWindow;
	
	private SystemTray systemTray;
	
	private PopupMenu popupMenu;
	
	private boolean check() {
		boolean ret = false;
		
		if(SystemTray.isSupported()) {
			systemTray = SystemTray.getSystemTray();
			ret = true;
		}
		
		if(!GraphicsEnvironment.isHeadless()) {
			uploadWindow = new UploadWindow();
			ret = true;
		} else {
			ret = false;
		}
		
		return ret;
	}
	
	private void preparePopupMenu() {
		popupMenu = new PopupMenu();
		
		TrayIcon trayIcon = new TrayIcon(ImageUtils.createImage("folder.gif", "tray icon"));
		MenuItem uploadItem = new MenuItem("Upload...");
		MenuItem aboutItem = new MenuItem("About");
		
		uploadItem.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				uploadWindow.setVisible(true);
			}
		});
		
		trayIcon.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(!uploadWindow.isVisible())
					uploadWindow.setVisible(true);
				else
					uploadWindow.setVisible(false);
			}
		});
		
		popupMenu.add(uploadItem);
		popupMenu.addSeparator();
		popupMenu.add(aboutItem);
		
		trayIcon.setImageAutoSize(true);
		trayIcon.setPopupMenu(popupMenu);
		
		try {
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		if(check()) {
			try {
	            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        } catch (Exception e) {
	        }
			preparePopupMenu();
		} else {
			System.err.println("SystemTray not supported");
		}
		
	}

	

}
