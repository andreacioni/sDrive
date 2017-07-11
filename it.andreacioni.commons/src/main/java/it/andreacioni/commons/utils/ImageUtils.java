package it.andreacioni.commons.utils;

import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageUtils {
	// Obtain the image URL
	public static Image createImage(String path) {
		URL imageURL = ResourceUtils.asUrl(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL)).getImage();
		}
	}

	public static BufferedImage getBufferedImage(String path) throws IOException {
		File imageURL = new File(ResourceUtils.asUrl(path).getFile());

		return ImageIO.read(imageURL);
	}

	public static TrayIcon getScaledTrayIconImage(String path) throws IOException {
		BufferedImage trayIconImage = getBufferedImage(path);
		int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
		return new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));
	}
}
