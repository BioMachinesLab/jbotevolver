package simulation.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassSearchUtils {


	public static List<String> searchFullNameInPath(String className) {
		ArrayList<String> classNames = new ArrayList<String>();
		ClassLoader classloader = className.getClass().getClassLoader();
		String classpath = System.getProperty("java.class.path");

		try {
			Method method = classloader.getClass().getMethod("getClassPath",
					(Class<?>) null);
			if (method != null) {
				classpath = (String) method.invoke(classloader, (Object) null);
			}
		} catch (Exception e) {
			// ignore
		}
		if (classpath == null) {
			classpath = System.getProperty("java.class.path");
		}

		StringTokenizer tokenizer = new StringTokenizer(classpath,
				File.pathSeparator);
		String token;
		File dir;
		String name;
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			dir = new File(token);
			if (dir.isDirectory()) {
				lookForNamesInDirectory(className, "", dir, classNames);
			}
			if (dir.isFile()) {
				name = dir.getName().toLowerCase();
				if (name.endsWith(".zip") || name.endsWith(".jar")) {
					lookForNamesInArchive(className, dir, classNames);
				}
			}
		}
		return classNames;
	}

	/**
	 * @param name
	 *            Name of to parent directories in java class notation (dot
	 *            separator)
	 * @param dir
	 *            Directory to be searched for classes.
	 */
	private static void lookForNamesInDirectory(String className, String name,
			File dir, ArrayList<String> classNames) {
		File[] files = dir.listFiles();
		try {
			// System.out.println(name+className);
			Class.forName(name + className);
			classNames.add(name + className);
		} catch (ClassNotFoundException e) {

		}

		String fileName;
		final int size = files.length;
		for (int i = 0; i < size; i++) {
			File file = files[i];
			fileName = file.getName();

			if (file.isDirectory()) {
				lookForNamesInDirectory(className, name + fileName + ".", file,
						classNames);
			}
		}

	}

	/**
	 * Search archive files for required resource.
	 * 
	 * @param archive
	 *            Jar or zip to be searched for classes or other resources.
	 */
	private static void lookForNamesInArchive(String className, File archive,
			ArrayList<String> classNames) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(archive);
		} catch (IOException e) {
			return;
		}
		Enumeration entries = jarFile.entries();
		JarEntry entry;
		String entryName;
		while (entries.hasMoreElements()) {
			entry = (JarEntry) entries.nextElement();
			entryName = entry.getName();
			if (entryName.toLowerCase().endsWith(".class")) {
				try {

					// convert name into java classloader notation
					entryName = entryName.substring(0, entryName.length() - 6);
					entryName = entryName.replace('/', '.');

					if (entryName.endsWith("." + className)
							|| entryName.equals(className)) {
						classNames.add(entryName);
					}

				} catch (Throwable e) {

				}
			}
		}
	}

}