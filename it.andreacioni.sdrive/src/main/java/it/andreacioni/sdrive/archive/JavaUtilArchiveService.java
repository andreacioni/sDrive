package it.andreacioni.sdrive.archive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaUtilArchiveService {

	private static final int BUFFER = 2048;

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	public JavaUtilArchiveService() {
	}

	public boolean uncompress(String file, String toDir, String key) throws IOException {
		LOG.info("Uncompressing file: {}, into: {}, with key", file, toDir, key);
		boolean ret = false;
		BufferedInputStream is = null;
		FileOutputStream fos = null;
		BufferedOutputStream dest = null;
		ZipFile inZipFile = null;

		if (file != null) {
			File inFile = new File(file), outFile = new File(toDir);

			if (inFile.exists() && inFile.canRead() && inFile.isFile()) {

				if (outFile.exists() && outFile.isFile())
					throw new IOException("Cannot write uncompressed content to file");
				else if (!outFile.exists() && !outFile.mkdirs())
					throw new IOException("Failed to create directories hierarchy");

				try {
					inZipFile = new ZipFile(inFile);
					Enumeration<? extends ZipEntry> entries = inZipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry e = entries.nextElement();
						File of = new File(outFile.getAbsolutePath() + File.separator + e.getName());

						LOG.debug("Uncompressing ZipEntry: {}", e.toString());
						if (of.exists()) {
							LOG.warn("Output file already exists");
						}

						if (e.isDirectory()) {
							if (!of.mkdir())
								throw new IOException("Cannot create folder: " + e.getName());
						} else {
							is = new BufferedInputStream(inZipFile.getInputStream(e));

							int count;
							byte data[] = new byte[BUFFER];
							fos = new FileOutputStream(of);
							dest = new BufferedOutputStream(fos, BUFFER);
							while ((count = is.read(data, 0, BUFFER)) != -1) {
								dest.write(data, 0, count);
							}

							dest.flush();
							dest.close();
							fos.flush();
							fos.close();
							is.close();

							dest = null;
							fos = null;
							is = null;
						}

						LOG.debug("Uncompressed ZipEntry: {}", e.toString());
					}

					ret = true;

				} catch (Exception e) {
					LOG.error("Exception while uncompressing ZIP archive", e);
				} finally {

					if (inZipFile != null) {
						inZipFile.close();
						inZipFile = null;
					}

					if (dest != null) {
						dest.close();
						dest = null;
					}

					if (is != null) {
						is.close();
						is = null;
					}

					if (fos != null) {
						fos.close();
						fos = null;
					}
				}

			} else
				LOG.error("{} cannot be read", file);
		} else
			LOG.error("Invalid file passed");

		return ret;
	}

}
