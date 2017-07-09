package it.andreacioni.sdrive.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import it.andreacioni.commons.utils.FileUtils;

public class FileUtilsTest {

	private static final String WIN_FILE_PATH_1 = "C:\\abs\\aer\\342r.txt";
	private static final String WIN_FILE_PATH_2 = "C:/abs/aer/342r.txt";
	private static final String WIN_FILE_PATH_3 = "C:/abs/aer/342r.bc.txt";
	private static final String WIN_FILE_PATH_4 = "C://abs//aer//342r.txt";

	private static final String UNX_FILE_PATH_1 = "abs/aer/342r.txt";
	private static final String UNX_FILE_PATH_2 = "/abs/aer/342r.txt";
	private static final String UNX_FILE_PATH_3 = "/abs/aer/342r.bc.txt";

	@Test
	public void testNonConflictingFileNames1() throws IOException {
		File f1 = new File("abc"), f2 = new File("abc(1)");
		f1.deleteOnExit();
		f2.deleteOnExit();

		Assert.assertTrue(!f1.exists() && !f2.exists());

		Assert.assertTrue(f1.createNewFile());

		Assert.assertEquals(f2.getName(), FileUtils.generateNonConflictFileName(f1).getName());

		Assert.assertTrue(f1.exists() && !f2.exists());

		Assert.assertTrue(f2.createNewFile());

		Assert.assertTrue(f1.exists() && f2.exists());

		Assert.assertTrue(f1.delete() && f2.delete());
	}

	@Test
	public void testNonConflictingFileNames2() throws IOException {
		File f1 = new File("abc.txt"), f2 = new File("abc(1).txt");
		f1.deleteOnExit();
		f2.deleteOnExit();

		Assert.assertTrue(!f1.exists() && !f2.exists());

		Assert.assertTrue(f1.createNewFile());

		Assert.assertEquals(f2.getName(), FileUtils.generateNonConflictFileName(f1).getName());

		Assert.assertTrue(f1.exists() && !f2.exists());

		Assert.assertTrue(f2.createNewFile());

		Assert.assertTrue(f1.exists() && f2.exists());

		Assert.assertTrue(f1.delete() && f2.delete());
	}

	@Test
	public void testNonConflictingFileNames3() throws IOException {
		File f1 = new File("abc.txt.bak"), f2 = new File("abc(1).txt.bak");
		f1.deleteOnExit();
		f2.deleteOnExit();

		Assert.assertTrue(!f1.exists() && !f2.exists());

		Assert.assertTrue(f1.createNewFile());

		Assert.assertEquals(f2.getName(), FileUtils.generateNonConflictFileName(f1).getName());

		Assert.assertTrue(f1.exists() && !f2.exists());

		Assert.assertTrue(f2.createNewFile());

		Assert.assertTrue(f1.exists() && f2.exists());

		Assert.assertTrue(f1.delete() && f2.delete());
	}

	@Test
	public void testWindows() {
		Assert.assertEquals("342r", FileUtils.getBaseName(WIN_FILE_PATH_1));
		Assert.assertEquals("txt", FileUtils.getExtension(WIN_FILE_PATH_1));

		Assert.assertEquals("342r", FileUtils.getBaseName(WIN_FILE_PATH_2));
		Assert.assertEquals("txt", FileUtils.getExtension(WIN_FILE_PATH_2));

		Assert.assertEquals("342r", FileUtils.getBaseName(WIN_FILE_PATH_3));
		Assert.assertEquals("bc.txt", FileUtils.getExtension(WIN_FILE_PATH_3));

		Assert.assertEquals("342r", FileUtils.getBaseName(WIN_FILE_PATH_4));
		Assert.assertEquals("txt", FileUtils.getExtension(WIN_FILE_PATH_4));
	}

	@Test
	public void testUnix() {
		Assert.assertEquals("342r", FileUtils.getBaseName(UNX_FILE_PATH_1));
		Assert.assertEquals("txt", FileUtils.getExtension(UNX_FILE_PATH_1));

		Assert.assertEquals("342r", FileUtils.getBaseName(UNX_FILE_PATH_2));
		Assert.assertEquals("txt", FileUtils.getExtension(UNX_FILE_PATH_2));

		Assert.assertEquals("342r", FileUtils.getBaseName(UNX_FILE_PATH_3));
		Assert.assertEquals("bc.txt", FileUtils.getExtension(UNX_FILE_PATH_3));
	}

	@Test
	public void testUnixPath() {
		String expected[] = new String[] { "abs", "aer", "342r.txt" };

		String parts[] = FileUtils.splitFilePath(UNX_FILE_PATH_1);

		assertArrayEquals(expected, parts);

		parts = FileUtils.splitFilePath(UNX_FILE_PATH_2);
		expected = new String[] { "abs", "aer", "342r.txt" };

		assertArrayEquals(expected, parts);

		parts = FileUtils.splitFilePath(UNX_FILE_PATH_3);
		expected = new String[] { "abs", "aer", "342r.bc.txt" };

		assertArrayEquals(expected, parts);
	}

	@Test
	public void testWindowsPath() {
		String expected[] = new String[] { "C:", "abs", "aer", "342r.txt" };

		String parts[] = FileUtils.splitFilePath(WIN_FILE_PATH_1);

		assertArrayEquals(expected, parts);

		parts = FileUtils.splitFilePath(WIN_FILE_PATH_2);
		expected = new String[] { "C:", "abs", "aer", "342r.txt" };

		assertArrayEquals(expected, parts);

		parts = FileUtils.splitFilePath(WIN_FILE_PATH_3);
		expected = new String[] { "C:", "abs", "aer", "342r.bc.txt" };

		assertArrayEquals(expected, parts);
	}

	@Test
	public void testFileCreation() {
		File f = new File("*");
		assertTrue(!f.exists());
		f = new File("./*");
		assertTrue(!f.exists());
	}

	@Test
	public void listFiles1() throws IOException {
		File rootDir = new File("test"), dir = new File("test/testNested"), file1 = new File("test/file1"),
				file2 = new File("test/file2"), file3 = new File("test/testNested/file3");

		rootDir.deleteOnExit();
		dir.deleteOnExit();
		file1.deleteOnExit();
		file2.deleteOnExit();
		file3.deleteOnExit();

		assertTrue(!rootDir.exists() && !dir.exists() && !file1.exists() && !file2.exists() && !file3.exists());

		assertTrue(rootDir.mkdir() && rootDir.exists() && rootDir.isDirectory());

		assertTrue(dir.mkdir() && dir.exists() && dir.isDirectory());

		assertTrue(file1.createNewFile() && file1.exists() && file1.isFile());

		assertTrue(file2.createNewFile() && file2.exists() && file2.isFile());

		assertTrue(file3.createNewFile() && file3.exists() && file3.isFile());

		List<File> files = FileUtils.listFiles(rootDir, null, true);

		assertEquals(3, files.size());

		assertTrue(files.contains(file1));
		assertTrue(files.contains(file2));
		assertTrue(files.contains(file3));

		assertTrue(!files.contains(rootDir));
		assertTrue(!files.contains(dir));

		assertTrue(file1.delete());
		assertTrue(file2.delete());
		assertTrue(file3.delete());
		assertTrue(dir.delete());
		assertTrue(rootDir.delete());
	}

	@Test
	public void listFiles2() throws IOException {
		File file = new File("test");

		file.deleteOnExit();

		assertTrue(!file.exists());

		assertTrue(file.createNewFile());

		List<File> files = FileUtils.listFiles(file, null, true);

		assertTrue(files.size() == 1 && files.contains(file));

		assertTrue(file.delete());
	}

	@Test
	public void listFilesAndDirs1() throws IOException {
		File rootDir = new File("test"), dir = new File("test/testNested"), file1 = new File("test/file1"),
				file2 = new File("test/file2"), file3 = new File("test/testNested/file3");

		rootDir.deleteOnExit();
		dir.deleteOnExit();
		file1.deleteOnExit();
		file2.deleteOnExit();
		file3.deleteOnExit();

		assertTrue(!rootDir.exists() && !dir.exists() && !file1.exists() && !file2.exists() && !file3.exists());

		assertTrue(rootDir.mkdir() && rootDir.exists() && rootDir.isDirectory());

		assertTrue(dir.mkdir() && dir.exists() && dir.isDirectory());

		assertTrue(file1.createNewFile() && file1.exists() && file1.isFile());

		assertTrue(file2.createNewFile() && file2.exists() && file2.isFile());

		assertTrue(file3.createNewFile() && file3.exists() && file3.isFile());

		List<File> files = FileUtils.listFilesAndDirs(rootDir, null, true);

		assertEquals(5, files.size());

		assertTrue(files.contains(file1));
		assertTrue(files.contains(file2));
		assertTrue(files.contains(file3));

		assertTrue(files.contains(rootDir));
		assertTrue(files.contains(dir));

		assertTrue(file1.delete());
		assertTrue(file2.delete());
		assertTrue(file3.delete());
		assertTrue(dir.delete());
		assertTrue(rootDir.delete());
	}
}
