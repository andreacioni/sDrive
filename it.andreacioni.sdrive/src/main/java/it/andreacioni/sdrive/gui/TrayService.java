package it.andreacioni.sdrive.gui;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.andreacioni.sdrive.ExitCodes;
import it.andreacioni.sdrive.SDrive;
import it.andreacioni.sdrive.utils.ImageUtils;

public class TrayService implements Runnable {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private UploadWindow uploadWindow;

	private SystemTray systemTray;

	private PopupMenu popupMenu;

	private final SDrive sDrive = new SDrive();

	private boolean check() {
		boolean ret = false;

		if (SystemTray.isSupported()) {
			systemTray = SystemTray.getSystemTray();
			ret = true;
		}

		if (!GraphicsEnvironment.isHeadless()) {
			uploadWindow = new UploadWindow(sDrive);
			ret = true;
		} else {
			ret = false;
		}

		return ret;
	}

	private void preparePopupMenu() {
		popupMenu = new PopupMenu();

		TrayIcon trayIcon = new TrayIcon(ImageUtils.createImage("icon.gif", "tray icon"));
		MenuItem uploadItem = new MenuItem("Upload...");
		MenuItem aboutItem = new MenuItem("About");
		MenuItem exitItem = new MenuItem("Exit");

		uploadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!uploadWindow.isVisible())
					uploadWindow.setVisible(true);
				else
					uploadWindow.setVisible(false);
			}
		});

		trayIcon.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!uploadWindow.isVisible())
					uploadWindow.setVisible(true);
				else
					uploadWindow.setVisible(false);
			}
		});

		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(ExitCodes.NO_ERROR);
			}
		});

		popupMenu.add(uploadItem);
		popupMenu.addSeparator();
		popupMenu.add(aboutItem);
		popupMenu.add(exitItem);

		trayIcon.setImageAutoSize(true);
		trayIcon.setPopupMenu(popupMenu);

		try {
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		if (sDrive.init()) {
			if (check()) {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					LOG.error("Exception on initialization", e);
				}
				preparePopupMenu();
			} else {
				System.err.println("SystemTray not supported");
			}
		}
	}

}
