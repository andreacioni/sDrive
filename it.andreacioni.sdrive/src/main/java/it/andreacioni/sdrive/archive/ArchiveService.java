package it.andreacioni.sdrive.archive;

import java.io.File;
import java.util.List;

public interface ArchiveService {

	public File compress(List<File> toBeCompressed, String toFilePath, String key) throws Exception;

	public boolean uncompress(String file, String toDir, String key) throws Exception;
}
