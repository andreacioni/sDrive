package it.andreacioni.sdrive.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import it.andreacioni.sdrive.archive.JavaUtilArchiveService;

public class JavaUtilArchiveServiceTest {

	private JavaUtilArchiveService archiveService;

	@Before
	public void init() {
		archiveService = new JavaUtilArchiveService();
	}

	@Test
	public void uncompressTest1() throws IOException {
		String toDir = "test";
		File uncompressed = new File(toDir);

		assertTrue(!uncompressed.exists());

		assertTrue(archiveService.uncompress(getClass().getResource("test_decompress_1.zip").getFile(), toDir, null));

		assertTrue(uncompressed.exists());
		assertTrue(uncompressed.isDirectory());
		assertTrue(uncompressed.list().length == 1);

		FileUtils.deleteDirectory(uncompressed);

		assertTrue(!uncompressed.exists());
	}

	@Test
	public void uncompressTest2() throws IOException {
		String toDir = "test";
		File uncompressed = new File(toDir);

		assertTrue(!uncompressed.exists());

		assertTrue(archiveService.uncompress(getClass().getResource("test_decompress_2.zip").getFile(), toDir, null));

		assertTrue(uncompressed.exists());
		assertTrue(uncompressed.isDirectory());
		assertTrue(uncompressed.list().length == 3);

		FileUtils.deleteDirectory(uncompressed);

		assertTrue(!uncompressed.exists());

	}

	@Test
	public void uncompressTest3() throws IOException {
		String toDir = "test";
		File uncompressed = new File(toDir);

		assertTrue(!uncompressed.exists());

		assertTrue(archiveService.uncompress(getClass().getResource("test_decompress_3.zip").getFile(), toDir, null));

		assertTrue(uncompressed.exists());
		assertTrue(uncompressed.isDirectory());
		assertTrue(uncompressed.list().length == 2);

		FileUtils.deleteDirectory(uncompressed);

		assertTrue(!uncompressed.exists());

	}

}
