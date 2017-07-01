package it.andreacioni.sdrive.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import it.andreacioni.sdrive.archive.Zip4jArchiveService;

public class Zip4jArchiveServiceTest {

	private Zip4jArchiveService archiveService;

	@Before
	public void init() {
		archiveService = new Zip4jArchiveService();
	}

	@Test
	public void testSimpleCompress() throws Exception {
		File dest = new File("dest.zip"), file = new File("testfile.txt");

		assertTrue(!dest.exists() && !file.exists());

		dest.deleteOnExit();
		file.deleteOnExit();

		assertTrue(file.createNewFile());

		assertTrue(archiveService.compress(Arrays.asList(file), dest.getAbsolutePath(), null) != null);

		assertTrue(dest.delete() & file.delete());
	}

	@Test
	public void testSimpleCompressAlreadyExists() throws Exception {
		File dest = new File("dest.zip"), f1 = new File("testfile.txt"), f2 = new File("testfile1.txt");

		assertTrue(!dest.exists() && !f1.exists() && !f2.exists());

		dest.deleteOnExit();
		f1.deleteOnExit();
		f2.deleteOnExit();

		assertTrue(f1.createNewFile() && f2.createNewFile());

		assertTrue(archiveService.compress(Arrays.asList(f1, f2), dest.getAbsolutePath(), null) != null);

		assertTrue(archiveService.compress(Arrays.asList(f1), dest.getAbsolutePath(), null) != null);

		assertTrue(dest.delete() & f1.delete() & f2.delete());
	}

	@Test
	public void testCompress() throws Exception {
		File dest = new File("test_compress.zip"), file = new File(getClass().getResource("test_compress").getFile());

		assertTrue(!dest.exists() && file.exists());

		dest.deleteOnExit();

		assertTrue(archiveService.compress(Arrays.asList(file), dest.getAbsolutePath(), null) != null);

		assertTrue(dest.delete());
	}

	@Test
	public void uncompressTest1() throws Exception {
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
	public void uncompressTest2() throws Exception {
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
	public void uncompressTest3() throws Exception {
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

	@Test
	public void uncompressTest4() throws IOException {

	}

}
