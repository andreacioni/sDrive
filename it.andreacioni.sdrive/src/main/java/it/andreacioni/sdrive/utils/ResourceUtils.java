package it.andreacioni.sdrive.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceUtils {

	// Obtain URL of a resource in RES_FOLDER
	public static URL asUrl(String resPath) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader.getResource(resPath);
	}

	// Obtain URL of a resource in RES_FOLDER
	public static InputStream asStream(String resPath) throws IOException {
		return asUrl(resPath).openStream();
	}

}
