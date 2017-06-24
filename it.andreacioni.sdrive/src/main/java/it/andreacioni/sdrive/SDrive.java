package it.andreacioni.sdrive;

import java.awt.SystemTray;

import javax.swing.SwingUtilities;

import it.andreacioni.sdrive.gui.TrayService;

public class SDrive {
	
	private final TrayService trayService;
	
	public SDrive() {
		trayService = new TrayService();
	}
	
	public void start() {
		SwingUtilities.invokeLater(trayService);
	}
	
	
}
