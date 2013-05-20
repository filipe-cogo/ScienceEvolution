package com.ironiacorp.computer.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.ironiacorp.computer.OperationalSystemType;
import com.ironiacorp.computer.OperationalSystemDetector;
import com.ironiacorp.computer.environment.PathEnvironmentVariable;
import com.ironiacorp.computer.environment.PathJVMEnvironmentVariable;
import com.ironiacorp.computer.environment.PathSystemEnvironmentVariable;

public class NativeLibraryLoader
{
	public class GlobFileFinder extends SimpleFileVisitor<Path>
	{
		private final PathMatcher matcher;

		private Set<Path> paths = new LinkedHashSet<Path>();
		
		GlobFileFinder(String pattern) {
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		}

		// Compares the glob pattern against the file or directory name.
		void find(Path file) {
			Path name = file.getFileName();
			if (name != null && matcher.matches(name)) {
				FileInputStream fis = null;
				InputStreamReader isr = null;
				BufferedReader br = null;
				try {
					fis = new FileInputStream(name.toFile());
					isr = new InputStreamReader(fis, "UTF-8");
					br = new BufferedReader(isr);
					String line = null;
					while ((line = br.readLine()) != null) {
						if (line.startsWith("include")) {
							String pattern = line.replaceFirst("include ", "");
							GlobFileFinder finder = new GlobFileFinder(pattern);
							Files.walkFileTree(Paths.get("/etc"), finder);
							for (Path path : finder.paths) {
								paths.add(path);
							}
						} else {
							String[] pathNames = line.split(": \t,");
							for (String path : pathNames) {
								paths.add(Paths.get(path));
							}
						}
					
					}
				} catch (IOException e) {
				} finally {
					try {
						if (br != null) {
							br.close();
						}
					} catch (IOException e1) {
					}
					try {
						if (isr != null) {
							isr.close();
						}
					} catch (IOException e1) {
					}
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException e1) {
					}
				}
			}
		}

		// Invoke the pattern matching
		// method on each file.
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return FileVisitResult.CONTINUE;
		}

		// Invoke the pattern matching
		// method on each directory.
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			find(dir);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			return FileVisitResult.CONTINUE;
		}
	}
	
	private String[] getJavaLibraryLocationProperties()
	{
		String[] properties = {
				"java.class.path",
				"java.endorsed.dirs",
				"java.ext.dirs"
		};
		
		return properties;
	}
	
	private String[] parseLinuxLDConf()
	{
		return parseLinuxLDConf("/etc/ld.so.conf");
	}
	
	private String[] parseLinuxLDConf(String filename)
	{
		LinkedHashSet<String> directories = new LinkedHashSet<String>();
		try {
			GlobFileFinder finder = new GlobFileFinder("ld.so.conf");
			Files.walkFileTree(Paths.get("/etc"), finder);
			for (Path path : finder.paths) {
				directories.add(path.toString());
			}
		} catch (IOException e) {
		}
		
		return directories.toArray(new String[directories.size()]);
	}
		
	private String[] getLibraryPath()
	{
		List<String> path = new ArrayList<String>();
		OperationalSystemDetector osDetector = new OperationalSystemDetector();
		
		if (osDetector.detectCurrentOS().unixCompatible) {
			PathEnvironmentVariable libraryPathEnvVar = new PathSystemEnvironmentVariable("LD_LIBRARY_PATH");
			for (String s : libraryPathEnvVar.getValue()) {
				path.add(s);
			}
		}
		
		if (osDetector.detectCurrentOS() == OperationalSystemType.Linux) {
			path.add("/lib");
			path.add("/usr/lib");
			path.add("/usr/local/lib");
			
			for (String s : parseLinuxLDConf()) {
				path.add(s);
			}
		}
		
		if (osDetector.detectCurrentOS() == OperationalSystemType.Windows) {
			path.add(System.getenv("windir"));
			path.add(System.getenv("windir") + File.separator + "system");
			path.add(System.getenv("windir") + File.separator + "system32");
		}
		
		for (String property : getJavaLibraryLocationProperties()) {
			PathEnvironmentVariable libraryPathJava = new PathJVMEnvironmentVariable(property);
			for (String s : libraryPathJava.getValue()) {
				path.add(s);
			}
		}
		
		return path.toArray(new String[path.size()]);
	}
	
	/**
	 * Load the library (lib*.so).
	 */
	public void loadLibrary(String name)
	{
		try {
			Runtime.getRuntime().loadLibrary(name);
		} catch (UnsatisfiedLinkError ule) {
			findAndLoadLibrary(name);
		}
	}
	
	public void findAndLoadLibrary(String name)
	{
		String libname = System.mapLibraryName(name);
		
		// Try to find the library in the library path.
		for (String path : getLibraryPath()) {
			try {
				System.load(path + File.separator + libname);
				return;
			} catch (UnsatisfiedLinkError enf) {
			}
		}
		
		throw new UnsatisfiedLinkError("Library '" + name + "' not found");
	}
}
