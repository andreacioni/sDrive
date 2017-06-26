package it.andreacioni.sdrive.cloud;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import it.andreacioni.sdrive.SDrive;
import it.andreacioni.sdrive.utils.ResourceUtils;

public class GoogleDriveCloudService implements CloudServive {

	/** Application name. */
	private static final String APPLICATION_NAME = "Secure Drive";

	private static final String ROOT_FOLDER = "root";

	private static final String MIME_TYPE_DIR = "application/vnd.google-apps.folder";

	private static final String SIMPLE_Q_FORMAT = "trashed = false and '%s' in parents";

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.sDrive
	 */
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_FILE);

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private Drive driveService;

	private boolean connected = false;

	private String accountName;

	public GoogleDriveCloudService() {
	}

	@Override
	public boolean connect() throws IOException {
		boolean ret = false;
		LOG.debug("Connecting to Google Drive");
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(SDrive.DATA_STORE_DIR);

			driveService = getDriveService();

			accountName = getAccountName();

			LOG.info("Account connected: " + accountName);

			ret = connected = true;
		} catch (Throwable t) {
			LOG.debug("Exception on Google Drive", t);
		}

		return ret;
	}

	@Override
	public String getAccountName() throws IOException {

		if (accountName == null) {
			try {
				return driveService.about().get().setFields("user").execute().getUser().getEmailAddress();
			} catch (IOException e) {
				LOG.debug("Exception on retrieving Google Drive account name", e);
			}

			return null;
		} else {
			return accountName;
		}

	}

	@Override
	public boolean isAccountConnected() throws IOException {
		return connected;
	}

	@Override
	public File download(String path, String toLocalPath) throws IOException {
		LOG.info("Downloading file: {}, to directory: {}", path, toLocalPath);
		File ret = null;

		if (toLocalPath != null) {
			File toFile = new File(toLocalPath);

			if (!toFile.createNewFile())
				LOG.warn("{} is already present, ovewriting it", toLocalPath);

			if (toFile.canWrite()) {
				com.google.api.services.drive.model.File file = getFile(path);
				if (file != null) {
					String fileId = file.getId();
					LOG.debug("File Id: {}", fileId);
					OutputStream outputStream = new FileOutputStream(toFile);

					driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);

					ret = toFile;
				} else
					LOG.error("Invalid file to download");
			} else {
				LOG.error("Cannot write to file");
			}

		} else
			LOG.error("Invalid path string");

		return ret;

	}

	@Override
	public boolean upload(File file, String destPath) throws IOException {
		LOG.info("Uploading file: {}", file.toString());
		boolean ret = false;
		com.google.api.services.drive.model.File parent = getDirectory(destPath);

		if (file != null && file.exists() && destPath != null) {
			com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();

			driveService.files().create(fileMetadata.setName(file.getName()).setParents(Arrays.asList(parent.getId())),
					new FileContent(null, file)).execute();

			ret = true;
		} else
			LOG.error("Invalid file to upload or destination path is not a valid directory");

		return ret;
	}

	@Override
	public boolean fileExists(String path) throws IOException {
		com.google.api.services.drive.model.File ret = getFile(path);
		return (ret != null);
	}

	@Override
	public boolean directoryExists(String path) throws IOException {
		com.google.api.services.drive.model.File ret = getDirectory(path);
		return (ret != null);
	}

	@Override
	public boolean createDirectory(String path, String folderName) throws IOException {
		LOG.info("Creating folder: {}, in path: {}", folderName, path);
		boolean ret = false;
		com.google.api.services.drive.model.File parentDir = getDirectory(path);
		if (parentDir != null) {
			com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();

			driveService.files().create(fileMetadata.setName(folderName).setParents(Arrays.asList(parentDir.getId()))
					.setMimeType(MIME_TYPE_DIR)).execute();

			ret = true;
		} else
			LOG.error("Parent directory not found or it is a file");

		return ret;
	}

	@Override
	public boolean deleteDirectory(String path) throws IOException {
		LOG.info("Deleting folder: {}", path);
		boolean ret = false;
		com.google.api.services.drive.model.File fDir = getDirectory(path);

		if (fDir != null) {
			driveService.files().delete(fDir.getId()).execute();

			ret = true;
		} else
			LOG.error("Directory not found or it is a file");

		return ret;
	}

	@Override
	public boolean deleteFile(String path) throws IOException {
		LOG.info("Deleting file: {}", path);
		boolean ret = false;
		com.google.api.services.drive.model.File fDir = getFile(path);

		if (fDir != null) {
			driveService.files().delete(fDir.getId()).execute();

			ret = true;
		} else
			LOG.error("Directory not found or it is a file");

		return ret;
	}

	@Override
	public void disconnect() throws IOException {
		if (connected) {
			driveService = null;
			connected = false;
		}
	}

	private com.google.api.services.drive.model.File get(String path) throws IOException {
		com.google.api.services.drive.model.File ret = null;

		if (!path.equals("/")) {
			String regex = Pattern.quote("/");
			String part[] = Arrays.stream(path.split(regex)).filter((s) -> !s.isEmpty()).toArray(String[]::new);

			LOG.debug("Splitted path: {},(regex: {})", Arrays.toString(part), regex);

			List<com.google.api.services.drive.model.File> list = listFilesInFolder(ROOT_FOLDER);
			Iterator<com.google.api.services.drive.model.File> iterator = list.iterator();
			int i = 0;

			LOG.debug("Path: {}, root: {}", path, list.toString());
			while (iterator.hasNext() && (i < part.length)) {

				LOG.debug("Searching for {} in: {}", part[i], list.toString());

				com.google.api.services.drive.model.File f = iterator.next();

				if (!f.getName().equals(part[i]))
					return null;
				else {
					list = listFilesInFolder(f.getId());
					iterator = list.iterator();
					ret = f;
					i++;
				}

			}
		} else
			ret = new com.google.api.services.drive.model.File().setId("root").setMimeType(MIME_TYPE_DIR);

		return ret;
	}

	private com.google.api.services.drive.model.File getFile(String path) throws IOException {
		com.google.api.services.drive.model.File ret = get(path);
		if (ret != null && !isDirectory(ret))
			return ret;
		else
			return null;
	}

	private com.google.api.services.drive.model.File getDirectory(String path) throws IOException {
		com.google.api.services.drive.model.File ret = get(path);
		if (ret != null && isDirectory(ret))
			return ret;
		else
			return null;
	}

	private List<com.google.api.services.drive.model.File> listFilesInFolder(String rootFolder) throws IOException {
		String pageToken = null;
		FileList result = null;
		List<com.google.api.services.drive.model.File> fileList = new LinkedList<com.google.api.services.drive.model.File>();

		do {
			result = driveService.files().list().setQ(String.format(SIMPLE_Q_FORMAT, rootFolder)).setSpaces("drive")
					.setCorpora("user").setIncludeTeamDriveItems(false).setSupportsTeamDrives(false)
					.setFields("nextPageToken, files(id, name, mimeType)").setPageToken(pageToken).execute();

			if (result == null)
				throw new IOException("Cannot retrieve result");

			fileList.addAll(result.getFiles());
			pageToken = result.getNextPageToken();
		} while (pageToken != null);

		return fileList;
	}

	private boolean isDirectory(com.google.api.services.drive.model.File file) {
		if (file == null)
			throw new IllegalArgumentException("Null file passed");
		else {
			return file.getMimeType().equals(MIME_TYPE_DIR);
		}
	}

	/**
	 * Build and return an authorized Drive client service.
	 *
	 * @return an authorized Drive client service
	 * @throws IOException
	 */
	private Drive getDriveService() throws IOException {
		Credential credential = authorize();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	}

	/**
	 * Creates an authorized Credential object.
	 *
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	private Credential authorize() throws IOException {
		// Load client secrets.
		InputStream in = ResourceUtils.asStream("client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

		System.out.println("Credentials stored in " + SDrive.DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

}
