package it.andreacioni.sdrive.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.andreacioni.sdrive.cloud.GoogleDriveCloudService;

public class GoogleDriveServiceTest {

	private GoogleDriveCloudService service;

	private static final String TEST_NAME = "sDriveTest";
	private static final String TEST_FILE = TEST_NAME + ".txt";
	private static final String TEST_DIR = "/" + TEST_NAME;

	@Before
	public void init() {
		service = new GoogleDriveCloudService();
		try {
			service.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRoot() throws IOException {
		assertTrue(service.directoryExists("/"));
		assertTrue(!service.fileExists("/"));
	}

	@Test
	public void testDir() throws IOException {
		assertTrue(!service.directoryExists(TEST_DIR));
		assertTrue(!service.fileExists(TEST_DIR));

		assertTrue(service.createDirectory("/", TEST_NAME));

		assertTrue(service.directoryExists(TEST_DIR));
		assertTrue(!service.fileExists(TEST_DIR));

		assertTrue(service.deleteDirectory(TEST_DIR));

		assertTrue(!service.directoryExists(TEST_DIR));
		assertTrue(!service.fileExists(TEST_DIR));

	}

	@Test
	public void testFile() throws IOException {

		File f = new File(TEST_FILE);
		f.createNewFile();
		f.deleteOnExit();

		assertEquals(f.getName(), TEST_FILE);

		assertTrue(!service.directoryExists("/" + TEST_FILE));
		assertTrue(!service.fileExists("/" + TEST_FILE));

		assertTrue(service.upload(f, "/"));

		assertTrue(f.delete());

		f = service.download("/" + TEST_FILE, TEST_FILE);

		assertTrue(f != null && f.delete());

		assertTrue(!service.directoryExists("/" + TEST_FILE));
		assertTrue(service.fileExists("/" + TEST_FILE));

		assertTrue(service.deleteFile("/" + TEST_FILE));

		assertTrue(!service.directoryExists("/" + TEST_FILE));
		assertTrue(!service.fileExists("/" + TEST_FILE));

	}

	@After
	public void close() {
		try {
			service.deleteDirectory(TEST_DIR);
			service.deleteFile("/" + TEST_FILE);
			service.disconnect();
		} catch (IOException e) {
			fail();
		}
	}

}
