package it.andreacioni.sdrive.archive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.andreacioni.sdrive.utils.FileUtils;

public class JavaUtilArchiveService {

	private static final int BUFFER = 2048;

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	public JavaUtilArchiveService() {
	}

	public File compress(String toBeCompressed[], String toFilePath, boolean mergeIfExists, String key)
			throws IOException {
		File ret = null;

		if (toBeCompressed != null)
			ret = compress(Arrays.asList(toBeCompressed), toFilePath, mergeIfExists, key);

		return ret;
	}

	public File compress(File[] toBeCompressed, String toFilePath, boolean mergeIfExists, String key)
			throws IOException {
		File ret = null;

		if (toBeCompressed != null && toBeCompressed.length != 0)
			ret = compress(Arrays.stream(toBeCompressed).map(File::toString).collect(Collectors.toList())
					.toArray(new String[] {}), toFilePath, mergeIfExists, key);

		return ret;
	}

	public File compress(List<String> toBeCompressed, String toFilePath, boolean mergeIfExists, String key)
			throws IOException {
		LOG.info("Compressing files: {}, into: {}, with key", toBeCompressed, toFilePath, key);
		File ret = null;

		if (toBeCompressed != null) {

			List<File> filesList = new LinkedList<>();

			for (String fString : toBeCompressed) {
				File f = new File(fString);

				filesList.addAll(FileUtils.listFilesAndDirs(f, (filterFile) -> {
					if (f.exists() && f.canRead()) {
						return true;
					} else {
						LOG.warn("Cannot insert {} into zip file", filterFile.getAbsolutePath());
						return false;
					}
				}, true));
			}

			for (File f : filesList) {
				ret = compress(f, toFilePath, mergeIfExists, key);
				mergeIfExists = true;
			}
		} else
			throw new IllegalArgumentException("Invalid parameter supplied");

		return ret;
	}

	private File compress(File toBeCompressed, String toFilePath, boolean mergeIfExists, String key)
			throws IOException {
		LOG.info("Compressing files: {}, into: {}, with key", toBeCompressed.getAbsolutePath(), toFilePath, key);
		File ret = null;

		File toFile = new File(toFilePath);

		if (toFile.exists() && !mergeIfExists) {
			toFile = FileUtils.generateNonConflictFileName(toFile);
			LOG.warn("Output file already exists, changing its name to: ", toFile.getName());
		}
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ZipOutputStream zipOutputStream = null;
		FileInputStream fin = null;
		BufferedInputStream bis = null;

		try {
			fos = new FileOutputStream(toFilePath);
			bos = new BufferedOutputStream(fos);
			zipOutputStream = new ZipOutputStream(bos);

			fin = new FileInputStream(toBeCompressed);
			bis = new BufferedInputStream(fin);

			compressZipEntry(toBeCompressed, zipOutputStream, bis);

			zipOutputStream.close();
			zipOutputStream = null;

			bos.close();
			bos = null;

			fos.close();
			fos = null;

			bis.close();
			bis = null;

			fin.close();
			fin = null;

		} catch (Exception e) {
			LOG.error("Exception cought on compressing file {}", toBeCompressed.toString());
		} finally {
			if (zipOutputStream != null) {
				zipOutputStream.close();
				zipOutputStream = null;
			}

			if (bos != null) {
				bos.close();
				bos = null;
			}

			if (fos != null) {
				fos.close();
				fos = null;
			}

			if (fin != null) {
				fin.close();
				fin = null;
			}

			if (bis != null) {
				bis.close();
				bis = null;
			}

		}

		return ret;
	}

	private void compressZipEntry(File toBeCompressed, ZipOutputStream zipOutputStream, BufferedInputStream bis)
			throws IOException {
		ZipEntry zEntry = new ZipEntry(toBeCompressed.toString());
		byte data[] = new byte[BUFFER];
		int count;

		zipOutputStream.putNextEntry(zEntry);
		while ((count = bis.read(data, 0, BUFFER)) != -1) {
			zipOutputStream.write(data, 0, count);
		}
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
							of = FileUtils.generateNonConflictFileName(of);
							LOG.warn("Output file already exists, changing its name to: ", of.getName());
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
