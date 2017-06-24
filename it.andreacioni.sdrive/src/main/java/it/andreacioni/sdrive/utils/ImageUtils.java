package it.andreacioni.sdrive.utils;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public class ImageUtils {
	 //Obtain the image URL
    public static Image createImage(String path, String description) {
        URL imageURL = ImageUtils.class.getResource(path);
        
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
