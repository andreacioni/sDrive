package it.andreacioni.sdrive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.andreacioni.sdrive.archive.ArchiveService;
import it.andreacioni.sdrive.archive.Zip4jArchiveService;
import it.andreacioni.sdrive.cloud.CloudServive;
import it.andreacioni.sdrive.cloud.GoogleDriveCloudService;
import it.andreacioni.sdrive.utils.ResourceUtils;

public class SDrive {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	/** Directory to store user credentials for this application. */
	public static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".sDrive");

	public static final String ZIP_NAME = "sDrive.zip";

	public static final File LOCAL_ARCHIVE = new File(DATA_STORE_DIR, ZIP_NAME);

	public static final File LOCAL_TEMP_DIR = new File(DATA_STORE_DIR, "tempdir");

	public static final String LOCAL_README_FILE_NAME = "README.txt";

	public static final String REMOTE_SDRIVE_DIR = "sDrive";

	public static final String REMOTE_SDRIVE_PATH = "/" + REMOTE_SDRIVE_DIR;

	public static final String REMOTE_README_PATH = REMOTE_SDRIVE_PATH + "/" + LOCAL_README_FILE_NAME;

	public static final String REMOTE_ARCHIVE_PATH = REMOTE_SDRIVE_PATH + "/" + ZIP_NAME;

	private CloudServive cloudService;

	private ArchiveService archiveService;

	private String masterPassword;

	public SDrive() {
		cloudService = new GoogleDriveCloudService();
		archiveService = new Zip4jArchiveService();
	}

	public boolean init() {
		boolean ret = false;
		try {
			ret = cloudService.connect();
		} catch (IOException e) {
			LOG.error("Exception connecting to cloud service");
		}
		return ret;
	}

	public boolean checkFirstStart() throws IOException {
		return !cloudService.fileExists(REMOTE_ARCHIVE_PATH);
	}

	public synchronized void setPassword(String psw) {
		masterPassword = psw;
	}

	public synchronized boolean isPasswordLoaded() {
		return masterPassword != null && !masterPassword.isEmpty();
	}

	public synchronized void uploadFiles(List<File> files) throws IOException {
		if (files != null && files.size() != 0) {
			if (isPasswordLoaded()) {
				if (clearTempFileAndDirectory()) {
					if (LOCAL_TEMP_DIR.mkdir()) {
						boolean fisrtStart = checkFirstStart();
						if (!fisrtStart) {
							LOG.debug("Not first start, archive file is present on remote");
							if (downloadRemoteArchive()) {
								LOG.debug("Download done!");

								unzipArchiveToTempDir();
								LOG.debug("Unzip done!");
								copyToTempAndUpload(files, fisrtStart);
							} else {
								throw new IOException("Failed to download archive");
							}

						} else {
							LOG.warn("First start");
							copyToTempAndUpload(files, fisrtStart);
						}
					} else {
						throw new IOException("Cannot create temp folder");
					}
				} else {
					throw new IOException("Cannot delete local archive");
				}
			} else
				throw new NullPointerException("Master password not set yet");

		} else
			throw new IllegalArgumentException("Invalid file list");

	}

	private boolean copyToTempAndUpload(List<File> files, boolean firstStart) throws IOException {
		boolean ret = false;
		copyNewFileToTempDir(files);

		if (!firstStart) {
			LOG.debug("Removing old zip file");
			removeZipFileIfExists();
		}

		if (zipArchiveFromTempDir()) {
			LOG.debug("Zip done!");
			createRemoteDirIfNotExists();
			ret = uploadRemoteArchive();
		} else {
			LOG.error("Cannot compress that files");
		}

		return ret;
	}

	private void createRemoteDirIfNotExists() throws IOException {
		if (!cloudService.directoryExists(REMOTE_SDRIVE_PATH)) {
			cloudService.createDirectory("/", REMOTE_SDRIVE_DIR);
		}
	}

	private void copyNewFileToTempDir(List<File> files) throws IOException {
		for (File f : files) {
			if (f.exists()) {
				LOG.debug("Copying (recursive) file {} to temp dir", f.getName());
				if (f.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(f, LOCAL_TEMP_DIR);
				} else {
					FileUtils.copyFileToDirectory(f, LOCAL_TEMP_DIR);
				}
			} else
				throw new FileNotFoundException();

		}
	}

	private void unzipArchiveToTempDir() throws IOException {
		LOG.debug("Uncompressing dowloaded zip file");
		archiveService.uncompress(LOCAL_ARCHIVE.getAbsolutePath(), LOCAL_TEMP_DIR.getAbsolutePath(), masterPassword);
	}

	private boolean zipArchiveFromTempDir() throws IOException {
		LOG.debug("Compressing new zip file");
		return (archiveService.compress(Arrays.asList(LOCAL_TEMP_DIR.listFiles()), LOCAL_ARCHIVE.getAbsolutePath(),
				masterPassword) != null) && LOCAL_ARCHIVE.exists();
	}

	private boolean downloadRemoteArchive() throws IOException {
		LOG.debug("Downloading file from remote");
		return (cloudService.download(REMOTE_ARCHIVE_PATH, LOCAL_ARCHIVE.getAbsolutePath()) != null)
				&& LOCAL_ARCHIVE.exists();
	}

	private boolean uploadRemoteArchive() throws IOException {
		LOG.debug("Uploading file to remote");
		return (cloudService.upload(LOCAL_ARCHIVE, REMOTE_SDRIVE_PATH)) && (cloudService.fileExists(REMOTE_README_PATH)
				|| (cloudService.upload(ResourceUtils.asFile(LOCAL_README_FILE_NAME), REMOTE_SDRIVE_PATH)));
	}

	private boolean clearTempFileAndDirectory() {
		boolean ret = false;
		try {
			removeTempDirectory();
			removeZipFileIfExists();
			ret = true;
		} catch (IOException e) {
			LOG.error("Failed to delete temp files");
		}
		return ret;
	}

	private void removeTempDirectory() throws IOException {
		FileUtils.deleteDirectory(LOCAL_TEMP_DIR);
	}

	private void removeZipFileIfExists() throws IOException {
		if (LOCAL_ARCHIVE.exists() && !LOCAL_ARCHIVE.delete())
			throw new IOException("Failed remove archive file");
	}

}
