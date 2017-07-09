package it.andreacioni.commons.cloud;

import java.io.File;
import java.io.IOException;

public interface CloudServive {

	public void setCredentialStoreDir(String path);

	public boolean connect() throws IOException;

	public void disconnect() throws IOException;

	public String getAccountName() throws IOException;

	public boolean isAccountConnected() throws IOException;

	public File download(String path, String toLocalPath) throws IOException;

	public boolean upload(File file, String destPath) throws IOException;

	public boolean fileExists(String path) throws IOException;

	public boolean directoryExists(String path) throws IOException;

	public boolean createDirectory(String path, String folderName) throws IOException;

	public boolean deleteDirectory(String path) throws IOException;

	public boolean deleteFile(String path) throws IOException;
}
