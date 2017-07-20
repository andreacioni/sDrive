package it.andreacioni.commons.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesConfiguration {
	private Properties properties;
	private File propertiesFile;

	public PropertiesConfiguration() {
	}

	public void load(String propertiesPath) throws FileNotFoundException, IOException {
		propertiesFile = new File(propertiesPath);
		postLoad();
	}

	public void load(File propertiesFile) throws FileNotFoundException, IOException {
		this.propertiesFile = propertiesFile;
		postLoad();
	}

	public void setValue(String key, String value) {
		properties.setProperty(key, value);
	}

	public String getValue(String key, String defaults) {
		String ret = properties.getProperty(key);
		if (ret == null)
			ret = defaults;
		return ret;
	}

	public synchronized void store() throws FileNotFoundException, IOException {
		if (properties != null) {
			properties.store(new FileOutputStream(propertiesFile), null);
		}
	}

	private void postLoad() throws FileNotFoundException, IOException {
		propertiesFile.createNewFile();
		properties = new Properties();
		properties.load(new FileInputStream(propertiesFile));
	}
}
