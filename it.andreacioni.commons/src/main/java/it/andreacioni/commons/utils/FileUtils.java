package it.andreacioni.commons.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FileUtils {

	public static File generateNonConflictFileName(File f) {
		File ret = null;
		String s;
		int i = 1;
		if (f == null)
			throw new IllegalArgumentException("Null file passed");

		if (!f.exists())
			ret = f;
		else {
			String format = "%s" + File.separatorChar + "%s(%d)%s";
			String basePath = getBasePath(f);
			String woExt = getBaseName(f);
			String ext = getExtension(f);

			if (!ext.isEmpty())
				ext = "." + ext;

			do {
				s = String.format(format, basePath, woExt, i, ext);
				i++;
			} while ((ret = new File(s)).exists());
		}

		return ret;
	}

	public static String getBasePath(File f) {
		return new File(f.getAbsolutePath()).getParent();
	}

	public static String getExtension(File f) {
		return getExtension(f.getAbsolutePath());
	}

	public static String getExtension(String path) {
		String parts[] = splitFilePath(path);
		if (parts == null || parts.length == 0)
			return null;
		else {
			if (parts[parts.length - 1].equals(".") || parts[parts.length - 1].indexOf('.') == -1)
				return "";
			else
				return parts[parts.length - 1].substring(parts[parts.length - 1].indexOf('.') + 1);
		}

	}

	public static String getBaseName(File f) {
		return getBaseName(f.getAbsolutePath());
	}

	public static String getBaseName(String path) {
		String parts[] = splitFilePath(path);
		if (parts == null || parts.length == 0)
			return null;
		else if (parts[parts.length - 1].indexOf('.') == -1)
			return parts[parts.length - 1];
		else
			return parts[parts.length - 1].substring(0, parts[parts.length - 1].indexOf('.'));

	}

	public static String[] splitFilePath(File f) {
		return splitFilePath(f.getAbsolutePath());
	}

	public static String[] splitFilePath(String path) {
		if (path == null)
			return null;
		else
			return Arrays.stream(path.split("[\\\\/]")).filter((s) -> !s.isEmpty()).toArray(String[]::new);
	}

	public static List<File> listFiles(File file, FileFilter filter, boolean b) {
		List<File> ret = new LinkedList<>();

		if (file == null)
			throw new NullPointerException();

		if (file.isDirectory()) {
			for (File f : file.listFiles(filter)) {
				if (filter == null || filter.accept(f)) {
					if (f.isDirectory() && b)
						ret.addAll(listFiles(f, filter, b));
					else
						ret.add(f);
				}
			}
		} else
			ret.add(file);

		return ret;
	}

	public static List<File> listFilesAndDirs(File file, FileFilter filter, boolean b) {
		List<File> ret = new LinkedList<>();

		if (file == null)
			throw new NullPointerException();

		ret.add(file);

		if (file.isDirectory()) {
			for (File f : file.listFiles(filter)) {
				if (filter == null || filter.accept(f)) {
					if (f.isDirectory() && b) {
						ret.add(f);
						ret.addAll(listFiles(f, filter, b));
					} else
						ret.add(f);
				}
			}
		}

		return ret;
	}
}
