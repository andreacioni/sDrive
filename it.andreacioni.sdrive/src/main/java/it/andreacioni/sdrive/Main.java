package it.andreacioni.sdrive;

import javax.swing.SwingUtilities;

import it.andreacioni.sdrive.gui.TrayService;

public class Main {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new TrayService());
	}

}