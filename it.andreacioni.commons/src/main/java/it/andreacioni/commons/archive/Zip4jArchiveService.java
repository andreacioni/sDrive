package it.andreacioni.commons.archive;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class Zip4jArchiveService implements ArchiveService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private CompressionLevel level = CompressionLevel.MEDIUM;

	@Override
	public CompressionLevel getCompressionLevel() {
		return level;
	}

	@Override
	public void setCompressionLevel(CompressionLevel level) {
		this.level = level;
	}

	@Override
	public File compress(List<File> toBeCompressed, String toFilePath, String key) throws IOException {
		LOG.info("Compressing files: {}, into: {}, with key", toBeCompressed, toFilePath, key);

		if (toBeCompressed != null && toBeCompressed.size() != 0 && toFilePath != null && !toFilePath.isEmpty()) {
			try {
				ZipFile zipFile = new ZipFile(toFilePath);

				ZipParameters parameters = new ZipParameters();
				parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
				parameters.setCompressionLevel(evaluateCompressionLevel());

				if (key != null && !key.isEmpty()) {
					Log.info("Key supplied, encryption enabled");
					parameters.setEncryptFiles(true);
					parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
					parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
					parameters.setPassword(key);
				} else
					Log.info("No key supplied, encryption disabled");

				for (File f : toBeCompressed) {
					if (f.isFile())
						zipFile.addFile(f, parameters);
					else
						zipFile.addFolder(f, parameters);
				}

				return zipFile.getFile();
			} catch (ZipException e) {
				throw new IOException(e);
			}

		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void uncompress(String file, String toDir, String key) throws IOException {
		LOG.info("Uncompressing file: {}, into: {}, with key", file, toDir, key);
		try {
			ZipFile zipFile = new ZipFile(file);
			if (key != null && !key.isEmpty()) {
				Log.info("Key supplied, encryption enabled");
				zipFile.setPassword(key);
			} else
				Log.info("No key supplied, encryption disabled");

			zipFile.extractAll(toDir);
		} catch (ZipException e) {
			throw new IOException(e);
		}

	}

	private int evaluateCompressionLevel() {
		switch (level) {
		case HIGH:
			return Zip4jConstants.DEFLATE_LEVEL_ULTRA;
		case MEDIUM:
			return Zip4jConstants.DEFLATE_LEVEL_NORMAL;
		case LOW:
			return Zip4jConstants.DEFLATE_LEVEL_FASTEST;
		default:
			throw new IllegalStateException("Invalid level proposed: " + level);
		}
	}
}
