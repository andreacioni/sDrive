package it.andreacioni.sdrive.archive;

import java.io.File;
import java.util.List;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class Zip4jArchiveService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	public File compress(List<File> toBeCompressed, String toFilePath, String key) throws Exception {
		LOG.info("Compressing files: {}, into: {}, with key", toBeCompressed, toFilePath, key);

		if (toBeCompressed != null && toBeCompressed.size() != 0 && toFilePath != null && !toFilePath.isEmpty()) {
			ZipFile zipFile = new ZipFile(toFilePath);

			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

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

		} else {
			throw new IllegalArgumentException();
		}
	}

	public boolean uncompress(String file, String toDir, String key) throws Exception {
		LOG.info("Uncompressing file: {}, into: {}, with key", file, toDir, key);
		ZipFile zipFile = new ZipFile(file);
		zipFile.extractAll(toDir);

		return true;
	}
}
