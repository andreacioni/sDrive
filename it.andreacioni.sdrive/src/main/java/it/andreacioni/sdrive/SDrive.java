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

	private SDriveProperties properties;

	private CloudServive cloudService;

	private ArchiveService archiveService;

	private String masterPassword;

	private String accountName;

	public SDrive(File dataStoreDir) {
		if (!dataStoreDir.isDirectory() && !dataStoreDir.mkdirs())
			throw new IllegalArgumentException("Not a valid directory");

		DATA_STORE_DIR = dataStoreDir;
		LOCAL_ARCHIVE = new File(DATA_STORE_DIR, ZIP_NAME);
		LOCAL_TEMP_DIR = new File(DATA_STORE_DIR, "tempdir");
		PROPERTIES_FILE = new File(DATA_STORE_DIR, "sdrive.properties");

		properties = new SDriveProperties();
		cloudService = new GoogleDriveCloudService(dataStoreDir.getAbsolutePath(), APPLICATION_NAME);
		archiveService = new Zip4jArchiveService();
	}

	public boolean init() throws IOException {
		boolean ret = false;
		try {
			ret = cloudService.connect();
			accountName = cloudService.getAccountName();
			properties.load(PROPERTIES_FILE);
			setCompressionLevel(CompressionLevel.valueOf(
					properties.getValue(SDriveProperties.COMPRESSION_LEVEL_KEY, CompressionLevel.MEDIUM.name())));
		} catch (IOException e) {
			throw new IOException("Exception connecting to cloud service");
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

	public void setCompressionLevel(CompressionLevel level) throws IOException {
		LOG.info("Setting compression level to: {}", level);
		archiveService.setCompressionLevel(level);
		properties.setValue(SDriveProperties.COMPRESSION_LEVEL_KEY, level.toString());
		try {
			properties.store();
		} catch (IOException e) {
			throw new IOException("Storing properties fail", e);
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
		LOG.info("Uploadign files: {}", files);
		if (files != null && files.size() != 0) {
			boolean firstStart = downloadAndUnzip(LOCAL_TEMP_DIR, progressCallback);
			copyToTempAndUpload(files, firstStart, progressCallback);
		} else
			throw new IllegalArgumentException("Invalid file list");

	}

	public synchronized void extractFiles(String toDir, ProgressCallback<String> progressCallback) throws IOException {
		if (!checkFirstStart()) {
			File f = new File(toDir);
			if (f.isDirectory()) {
				f = it.andreacioni.commons.utils.FileUtils.generateNonConflictDirectoryName(new File(f, "sDrive"));
				LOG.debug("Extracting files to {}", f.getAbsolutePath());
				if (f.mkdir()) {
					try {
						downloadAndUnzip(f, progressCallback);
					} catch (IOException e) {
						f.delete();
						throw e;
					}
				} else
					throw new IOException("Cannot create directory: " + f.getName());
			} else
				throw new IllegalArgumentException("Not a valid directory");
		} else
			throw new IllegalStateException("No archive can be dowloaded from cloud");
	}

	private boolean unzipToDir(String directoryPath) {
		LOG.debug("Uncompressing dowloaded zip file to: {}", directoryPath);
		File f = new File(directoryPath);
		if (f.isDirectory()) {
			try {
				archiveService.uncompress(LOCAL_ARCHIVE.getAbsolutePath(), f.getAbsolutePath(), masterPassword);
				return true;
			} catch (IOException e) {
				return false;
			}
		} else
			throw new IllegalArgumentException("Not a valid directory");
	}

	private boolean downloadAndUnzip(File directory, ProgressCallback<String> progressCallback) throws IOException {
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
							if (unzipToDir(directory.getAbsolutePath())) {
								LOG.debug("Unzip done!");
							} else {
								setPassword(null);
								throw new IOException("Failed to unzip archive. Wrong password?");
							}
						} else {
							throw new IOException("Failed to download archive");
						}

					} else {
						LOG.warn("First start");
					}

					return fisrtStart;
				} else {
					throw new IOException("Cannot create temp folder");
				}
			} else {
				throw new IOException("Cannot delete local archive");
			}
		} else
			throw new RuntimeException("Master password not set yet");
	}

	private void updateProgress(String newState, ProgressCallback<String> progressCallback) {
		if (progressCallback != null)
			progressCallback.progressUpdate(newState);
	}

	private void copyToTempAndUpload(List<File> files, boolean firstStart, ProgressCallback<String> progressCallback)
			throws IOException {
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
			if (!uploadRemoteArchive())
				throw new IOException("File not uploaded correctly");

		} else {
			throw new IOException("Cannot compress that files");
		}
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

	private boolean clearTempFileAndDirectory() throws IOException {
		boolean ret = false;
		try {
			removeTempDirectory();
			removeZipFileIfExists();
			ret = true;
		} catch (IOException e) {
			throw new IOException("Failed to delete temp files");
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
