package it.andreacioni.sdrive.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceUtils {

	// Obtain URL of a resource in RES_FOLDER
	public static URL asUrl(String resPath) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader.getResource(resPath);
	}

	public static String asFilePath(String resPath) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader.getResource(resPath).getFile();
	}

	public static File asFile(String resPath) {
		return new File(asFilePath(resPath));
	}

	// Obtain URL of a resource in RES_FOLDER
	public static InputStream asStream(String resPath) throws IOException {
		return asUrl(resPath).openStream();
	}

}
