package it.andreacioni.sdrive.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.andreacioni.sdrive.cloud.GoogleDriveCloudService;

public class GoogleDriveServiceTest {

	private GoogleDriveCloudService service;

	@Before
	public void init() {
		service = new GoogleDriveCloudService();
		try {
			service.connect();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSearchFile() {
		try {
			assertTrue(service.fileExists("TODO.txt"));

			assertTrue(!service.fileExists("TODO"));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSearchDir() {
		try {
			assertTrue(service.directoryExists("Personale"));

			assertTrue(!service.directoryExists("Person"));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@After
	public void close() {
		try {
			service.disconnect();
		} catch (IOException e) {
			fail();
		}
	}

}
