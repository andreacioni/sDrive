package it.andreacioni.sdrive.archive;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ArchiveService {

	public File compress(List<File> toBeCompressed, String toFilePath, String key) throws IOException;

	public void uncompress(String file, String toDir, String key) throws IOException;
}
