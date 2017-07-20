package it.andreacioni.commons.archive;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ArchiveService {

	public CompressionLevel getCompressionLevel();

	public void setCompressionLevel(CompressionLevel level);

	public File compress(List<File> toBeCompressed, String toFilePath, String key) throws IOException;

	public void uncompress(String file, String toDir, String key) throws IOException;
}
