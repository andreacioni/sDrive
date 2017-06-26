package it.andreacioni.sdrive;

import java.io.File;

import it.andreacioni.sdrive.cloud.CloudServive;
import it.andreacioni.sdrive.cloud.GoogleDriveCloudService;
import it.andreacioni.sdrive.gui.TrayService;

public class SDrive {

	/** Directory to store user credentials for this application. */
	public static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".sDrive");

	private CloudServive cloudService;

	private final TrayService trayService;

	public SDrive() {
		cloudService = new GoogleDriveCloudService();
		trayService = new TrayService();
	}

	public void start() {
		/*
		 * if (cloudService.connect()) {
		 * System.out.println("Account connected: " +
		 * cloudService.getAccountName());
		 * SwingUtilities.invokeLater(trayService); }
		 */
	}

}
