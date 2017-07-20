package it.andreacioni.sdrive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.andreacioni.commons.archive.ArchiveService;
import it.andreacioni.commons.archive.CompressionLevel;
import it.andreacioni.commons.archive.Zip4jArchiveService;
import it.andreacioni.commons.cloud.CloudServive;
import it.andreacioni.commons.cloud.GoogleDriveCloudService;
import it.andreacioni.commons.swing.ProgressCallback;
import it.andreacioni.commons.utils.ResourceUtils;

public class SDrive {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private static final String APPLICATION_NAME = "Secure Drive";

	public static final String ZIP_NAME = "sDrive.zip";

	public static final String LOCAL_README_FILE_NAME = "README.txt";

	public static final String REMOTE_SDRIVE_DIR = "sDrive";

	public static final String REMOTE_SDRIVE_PATH = "/" + REMOTE_SDRIVE_DIR;

	public static final String REMOTE_README_PATH = REMOTE_SDRIVE_PATH + "/" + LOCAL_README_FILE_NAME;

	public static final String REMOTE_ARCHIVE_PATH = REMOTE_SDRIVE_PATH + "/" + ZIP_NAME;

	private final File DATA_STORE_DIR;

	public final File LOCAL_ARCHIVE;

	public final File LOCAL_TEMP_DIR;

	private final File PROPERTIES_FILE;

	private SDrivePropertiesKey properties;

	private CloudServive cloudService;

	private ArchiveService archiveService;

	private String masterPassword;

	private String accountName;

	public SDrive(File dataStoreDir) {
		if (!dataStoreDir.isDirectory())
			throw new IllegalArgumentException("Not a valid directory");

		DATA_STORE_DIR = dataStoreDir;
		LOCAL_ARCHIVE = new File(DATA_STORE_DIR, ZIP_NAME);
		LOCAL_TEMP_DIR = new File(DATA_STORE_DIR, "tempdir");
		PROPERTIES_FILE = new File(DATA_STORE_DIR, "sdrive.properties");

		properties = new SDrivePropertiesKey();
		cloudService = new GoogleDriveCloudService(dataStoreDir.getAbsolutePath(), APPLICATION_NAME);
		archiveService = new Zip4jArchiveService();
	}

	public boolean init() {
		boolean ret = false;
		try {
			ret = cloudService.connect();
			accountName = cloudService.getAccountName();
			properties.load(PROPERTIES_FILE);
			setCompressionLevel(CompressionLevel.valueOf(
					properties.getValue(SDrivePropertiesKey.COMPRESSION_LEVEL_KEY, CompressionLevel.MEDIUM.name())));
		} catch (IOException e) {
			LOG.error("Exception connecting to cloud service");
		}
		return ret;
	}

	public String getAccountName() {
		return accountName;
	}

	public boolean checkFirstStart() throws IOException {
		return !cloudService.fileExists(REMOTE_ARCHIVE_PATH);
	}

	public CompressionLevel getCompressionLevel() {
		return archiveService.getCompressionLevel();
	}

	public void setCompressionLevel(CompressionLevel level) {
		LOG.info("Setting compression level to: {}", level);
		archiveService.setCompressionLevel(level);
		properties.setValue(SDrivePropertiesKey.COMPRESSION_LEVEL_KEY, level.toString());
		try {
			properties.store();
		} catch (IOException e) {
			LOG.error("Storing properties fail", e);
		}
	}

	public synchronized void setPassword(String psw) {
		masterPassword = psw;
	}

	public synchronized boolean isPasswordLoaded() {
		return masterPassword != null && !masterPassword.isEmpty();
	}

	public synchronized void uploadFiles(List<File> files) throws IOException {
		uploadFiles(files, null);
	}

	public synchronized void uploadFiles(List<File> files, ProgressCallback<String> progressCallback)
			throws IOException {
		if (files != null && files.size() != 0) {
			updateProgress("Checking password loaded...", progressCallback);
			if (isPasswordLoaded()) {
				updateProgress("Clearing temp directory...", progressCallback);
				if (clearTempFileAndDirectory()) {
					updateProgress("Creating temp directory...", progressCallback);
					if (LOCAL_TEMP_DIR.mkdir()) {
						boolean fisrtStart = checkFirstStart();
						if (!fisrtStart) {
							LOG.debug("Not first start, archive file is present on remote");
							updateProgress("Downloading remote archive...", progressCallback);
							if (downloadRemoteArchive()) {
								LOG.debug("Download done!");

								updateProgress("Uncompressing remote archive locally...", progressCallback);
								if (unzipArchiveToTempDir()) {
									LOG.debug("Unzip done!");
									copyToTempAndUpload(files, fisrtStart, progressCallback);
								} else {
									setPassword(null);
									throw new IOException("Failed to unzip archive. Wrong password?");
								}
							} else {
								throw new IOException("Failed to download archive");
							}

						} else {
							LOG.warn("First start");
							copyToTempAndUpload(files, fisrtStart, progressCallback);
						}
					} else {
						throw new IOException("Cannot create temp folder");
					}
				} else {
					throw new IOException("Cannot delete local archive");
				}
			} else
				throw new RuntimeException("Master password not set yet");

		} else
			throw new IllegalArgumentException("Invalid file list");

	}

	private void updateProgress(String newState, ProgressCallback<String> progressCallback) {
		if (progressCallback != null)
			progressCallback.progressUpdate(newState);
	}

	private boolean copyToTempAndUpload(List<File> files, boolean firstStart, ProgressCallback<String> progressCallback)
			throws IOException {
		boolean ret = false;
		copyNewFileToTempDir(files);

		if (!firstStart) {
			LOG.debug("Removing old zip file");
			updateProgress("Removing old zip file...", progressCallback);
			removeZipFileIfExists();
		}

		updateProgress("Zipping new archive...", progressCallback);
		if (zipArchiveFromTempDir()) {
			LOG.debug("Zip done!");
			createRemoteDirIfNotExists();
			updateProgress("Uploading...", progressCallback);
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

	private boolean unzipArchiveToTempDir() {
		LOG.debug("Uncompressing dowloaded zip file");
		try {
			archiveService.uncompress(LOCAL_ARCHIVE.getAbsolutePath(), LOCAL_TEMP_DIR.getAbsolutePath(),
					masterPassword);
			return true;
		} catch (IOException e) {
			LOG.error("", e);
			return false;
		}
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
