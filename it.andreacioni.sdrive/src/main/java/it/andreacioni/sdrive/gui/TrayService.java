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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.andreacioni.commons.archive.CompressionLevel;
import it.andreacioni.commons.swing.JProgressDialog;
import it.andreacioni.commons.swing.ProgressCallback;
import it.andreacioni.commons.thread.StrongThread;
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

	private File previousExtractPath;

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
		MenuItem extractItem = new MenuItem("Extract...");
		MenuItem usernameItem = new MenuItem(sDrive.getAccountName());
		MenuItem aboutItem = new MenuItem("About");
		MenuItem exitItem = new MenuItem("Exit");

		usernameItem.setEnabled(false);

		extractItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDirectoryAndUnzip(e);
			}
		});

		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"sDrive is an open sourxce project hosted on GitHub.\nMore info here: https://github.com/andreacioni/sDrive",
						"About", JOptionPane.INFORMATION_MESSAGE);

			}
		});

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
		popupMenu.add(extractItem);
		popupMenu.add(prepareCompressionLevelMenu());
		popupMenu.addSeparator();
		popupMenu.add(usernameItem);
		popupMenu.add(aboutItem);
		popupMenu.add(exitItem);

		trayIcon.setImageAutoSize(true);
		trayIcon.setPopupMenu(popupMenu);

		try {
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			LOG.error("Failed to add traycon to system tray", e);
		}

	}

	private void selectDirectoryAndUnzip(ActionEvent e) {
		String dir = showDirectoryChooser();
		if (dir != null) {
			final JProgressDialog progressDialog = new JProgressDialog(uploadWindow, "Progress", "Starting...", 0, 0);
			final StrongThread thread = new StrongThread(new Runnable() {

				@Override
				public void run() {
					try {
						if (prepare(progressDialog)) {
							if (!sDrive.checkFirstStart()) {
								sDrive.extractFiles(dir, new ProgressCallback<String>() {
									@Override
									public void progressUpdate(String progress) {
										progressDialog.setText(progress);
									}
								});
								JOptionPane.showMessageDialog(progressDialog, "Uncompression done!", "Info",
										JOptionPane.INFORMATION_MESSAGE);
							} else
								LOG.error("No archive can be dowloaded!");
						} else
							LOG.error("No password supplied!");
					} catch (IOException e) {
						LOG.error("Extract failed", e);

					} finally {
						LOG.debug("Closing dialog");
						progressDialog.closeDialog();
					}

				}
			});

			thread.start();

			progressDialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent ev) {
					LOG.debug("Cancelling operation...");
					thread.stop();
					LOG.warn("Operation cancelled");
				}
			});
			progressDialog.showDialog();
		} else
			LOG.error("Not a valid directory");

	}

	private boolean prepare(JProgressDialog progressDialog) throws IOException {
		return new MasterPasswordManager(sDrive, true).prepareUpload(progressDialog);
	}

	@Override
	public void run() {
		try {
			if (sDrive.init()) {
				if (check()) {
					// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					preparePopupMenu();

				} else {
					LOG.error("SystemTray not supported");
				}
			}

		} catch (Exception e) {
			LOG.error("Exception on initialization", e);
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
		try {
			sDrive.setCompressionLevel(compLev);
		} catch (IOException e) {
			LOG.error("Failed setting compression level", e);
		}
	}

	private String showDirectoryChooser() {
		String dir = null;
		JFileChooser fileChooser;

		if (previousExtractPath == null)
			fileChooser = new JFileChooser();
		else
			fileChooser = new JFileChooser(previousExtractPath);

		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle("Select unzip directory");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int ret = fileChooser.showOpenDialog(uploadWindow);

		if (ret == JFileChooser.APPROVE_OPTION) {
			previousExtractPath = fileChooser.getSelectedFile();
			dir = previousExtractPath.getAbsolutePath();
			if (dir.isEmpty())
				dir = null;
		}

		return dir;
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
