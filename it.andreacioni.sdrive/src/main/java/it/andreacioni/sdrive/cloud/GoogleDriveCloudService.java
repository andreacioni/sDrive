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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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

	private static final String APP_DATA_FOLDER = "appDataFolder";

	private static final String ROOT_FOLDER = "root";

	private static final String MIME_TYPE_DIR = "application/vnd.google-apps.folder";

	private static final String SIMPLE_Q_FORMAT = "name = '%s' and trashed = false and '%s' in parents";

	private static final String DIRECTORY_Q_FORMAT = "mimeType='" + MIME_TYPE_DIR
			+ "' and name = '%s' and trashed = false and '%s' in parents";

	private static final String FILE_Q_FORMAT = "mimeType!='" + MIME_TYPE_DIR
			+ "' and name = '%s' and trashed = false and '%s' in parents";

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

	private Drive driveService;

	private boolean connected = false;

	private String accountName;

	public GoogleDriveCloudService() {
	}

	public boolean connect() throws IOException {
		boolean ret = false;
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(SDrive.DATA_STORE_DIR);

			driveService = getDriveService();

			accountName = getAccountName();

			ret = connected = true;
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return ret;
	}

	public String getAccountName() throws IOException {

		if (accountName == null) {
			try {
				return driveService.about().get().setFields("user").execute().getUser().getEmailAddress();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		} else {
			return accountName;
		}

	}

	public boolean isAccountConnected() throws IOException {
		return connected;
	}

	public File download(String path) throws IOException {
		File ret = null;
		com.google.api.services.drive.model.File file = getFile(path);
		if (file == null)
			throw new IOException("Invalid path passed");
		String fileId = file.getName();
		OutputStream outputStream = new FileOutputStream(new File(path));
		driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);

		return ret;
	}

	public boolean upload(File file) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean createFolder(String path, String folderName) throws IOException {
		boolean ret = true;

		com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
		fileMetadata.setName(folderName);
		fileMetadata.setMimeType(MIME_TYPE_DIR);

		driveService.files().create(fileMetadata).execute();

		return ret;
	}

	public boolean fileExists(String path) throws IOException {
		com.google.api.services.drive.model.File ret = getFile(path);
		return (ret != null) && !isDirectory(ret);
	}

	public boolean directoryExists(String path) throws IOException {
		com.google.api.services.drive.model.File ret = getFile(path);
		return (ret != null) && isDirectory(ret);
	}

	public com.google.api.services.drive.model.File getFile(String path) throws IOException {
		boolean flag = true;
		com.google.api.services.drive.model.File ret = null;
		String part[] = path.split("/");
		List<com.google.api.services.drive.model.File> list = listFilesInFolder(ROOT_FOLDER);

		for (int i = 0; (i < path.length()) && flag; i++) {
			Iterator<com.google.api.services.drive.model.File> iterator = list.iterator();
			ret = null;
			flag = false;
			while (iterator.hasNext() && !flag) {
				com.google.api.services.drive.model.File f = iterator.next();
				flag = f.getName().equals(part[i]);
				if (flag) {
					ret = f;
					list = listFilesInFolder(f.getId());
				}
			}
		}

		return ret;
	}

	public void disconnect() throws IOException {
		if (connected) {
			driveService = null;
			connected = false;
		}
	}

	private List<com.google.api.services.drive.model.File> listFilesInFolder(String rootFolder) throws IOException {
		String pageToken = null;
		FileList result = null;
		List<com.google.api.services.drive.model.File> fileList = new LinkedList<com.google.api.services.drive.model.File>();

		do {
			result = driveService.files().list().setQ(String.format(SIMPLE_Q_FORMAT, rootFolder)).setSpaces("drive")
					.setCorpora("user").setIncludeTeamDriveItems(false).setSupportsTeamDrives(false)
					.setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();

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
