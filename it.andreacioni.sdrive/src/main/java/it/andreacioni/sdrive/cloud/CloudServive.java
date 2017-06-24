package it.andreacioni.sdrive.cloud;

import java.io.File;

public interface CloudServive {
	
	public String getAccountName();
	
	public boolean isAccountConnected();
	
	public File download();
	
	public boolean upload();
}
