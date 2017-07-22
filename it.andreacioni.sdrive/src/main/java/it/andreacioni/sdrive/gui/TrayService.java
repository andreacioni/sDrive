package it.andreacioni.sdrive.gui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.GraphicsEnvironment;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.andreacioni.commons.archive.CompressionLevel;
import it.andreacioni.commons.utils.ImageUtils;
import it.andreacioni.sdrive.ExitCodes;
import it.andreacioni.sdrive.SDrive;

/**
 * @author andreacioni
 *
 */
public class TrayService implements Runnable {

	/** Directory to store user credentials for this application. */
	public static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".sDrive");

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private UploadWindow uploadWindow;

	private SystemTray systemTray;

	private PopupMenu popupMenu;

	private final SDrive sDrive;

	public TrayService() {
		sDrive = new SDrive(DATA_STORE_DIR);
	}

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

	private void preparePopupMenu() throws IOException {
		popupMenu = new PopupMenu();

		TrayIcon trayIcon = ImageUtils.getScaledTrayIconImage(ImageUtils.createImage("icon.png"));

		MenuItem uploadItem = new MenuItem("Upload...");
		MenuItem aboutItem = new MenuItem("About");
		MenuItem exitItem = new MenuItem("Exit");

		uploadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				putOnTop();
			}
		});

		trayIcon.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				putOnTop();
			}
		});

		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(ExitCodes.NO_ERROR);
			}
		});

		popupMenu.add(uploadItem);
		popupMenu.add(prepareCompressionLevelMenu());
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
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					preparePopupMenu();
					// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					LOG.error("Exception on initialization", e);
				}

			} else {
				LOG.error("SystemTray not supported");
			}
		}
	}

	private Menu prepareCompressionLevelMenu() {
		final Menu compressionItemMenu = new Menu("Compression");
		CheckboxMenuItem lowCheck = new CheckboxMenuItem("Low");
		CheckboxMenuItem midCheck = new CheckboxMenuItem("Medium");
		CheckboxMenuItem highCheck = new CheckboxMenuItem("High");

		CompressionLevel currentLevel = sDrive.getCompressionLevel();

		switch (currentLevel) {
		case HIGH:
			highCheck.setState(true);
			break;
		case LOW:
			lowCheck.setState(true);
			break;
		case MEDIUM:
			midCheck.setState(true);
			break;
		default:
			LOG.warn("Invalid selection");
		}

		ItemListener l = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				CompressionLevel compLev = CompressionLevel
						.valueOf(((CheckboxMenuItem) e.getSource()).getLabel().toUpperCase());
				switch (compLev) {
				case HIGH:
					midCheck.setState(false);
					lowCheck.setState(false);
					break;
				case LOW:
					midCheck.setState(false);
					highCheck.setState(false);
					break;
				case MEDIUM:
					lowCheck.setState(false);
					highCheck.setState(false);
					break;
				default:
					LOG.warn("Invalid selection");
				}

				saveCompressionLevel(compLev);

			}
		};

		lowCheck.addItemListener(l);
		midCheck.addItemListener(l);
		highCheck.addItemListener(l);

		compressionItemMenu.add(lowCheck);
		compressionItemMenu.add(midCheck);
		compressionItemMenu.add(highCheck);

		return compressionItemMenu;
	}

	private void saveCompressionLevel(CompressionLevel compLev) {
		sDrive.setCompressionLevel(compLev);
	}

	/**
	 * This solution is the only that work on OSX. It was found here:
	 * https://stackoverflow.com/questions/309023/how-to-bring-a-window-to-the-front
	 */
	private void putOnTop() {
		uploadWindow.setVisible(true);
		int state = uploadWindow.getExtendedState();
		state &= ~JFrame.ICONIFIED;
		uploadWindow.setExtendedState(state);
		uploadWindow.setAlwaysOnTop(true);
		uploadWindow.toFront();
		uploadWindow.requestFocus();
		uploadWindow.setAlwaysOnTop(false);
	}

}
