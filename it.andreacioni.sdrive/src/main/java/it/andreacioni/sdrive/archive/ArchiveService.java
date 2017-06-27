package it.andreacioni.sdrive.archive;

import java.io.File;
import java.util.List;

public interface ArchiveService {

	public List<File> uncompress(File file, String key);

	public File compress(String key, List<File> files);
}
