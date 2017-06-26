package it.andreacioni.sdrive.cloud;

import java.io.File;
import java.io.IOException;

public interface CloudServive {

	public boolean connect() throws IOException;

	public void disconnect() throws IOException;

	public String getAccountName() throws IOException;

	public boolean isAccountConnected() throws IOException;

	public File download(String path) throws IOException;

	public boolean upload(File file) throws IOException;

	public boolean fileExists(String path) throws IOException;

	public boolean directoryExists(String path) throws IOException;

	public boolean createFolder(String path, String folderName) throws IOException;
}
