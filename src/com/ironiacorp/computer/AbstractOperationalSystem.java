/*
 * Copyright (C) 2011 Marco Aurélio Graciotto Silva <magsilva@ironiacorp.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ironiacorp.computer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.ironiacorp.io.Filesystem;

public abstract class AbstractOperationalSystem implements OperationalSystem 
{
	protected List<File> extraExecutableSearchPaths;
	
	protected List<File> bogusExecutableSearchPaths;


	protected List<File> extraLibrarySearchPaths;
	
	protected List<File> bogusLibrarySearchPaths;

	
	public AbstractOperationalSystem()
	{
		extraExecutableSearchPaths = new LinkedList<File>();
		bogusExecutableSearchPaths = new LinkedList<File>();
		
		extraLibrarySearchPaths = new LinkedList<File>();
		bogusLibrarySearchPaths = new LinkedList<File>();
	}
	
	protected abstract List<File> getSystemExecutableSearchPath();

	
	@Override
	public void addExecutableSearchPath(File dir)
	{
		if (isValidPath(dir)) {
			extraExecutableSearchPaths.add(dir);
		}
	}

	@Override
	public void removeExecutableSearchPath(File dir)
	{
		if (isValidPath(dir)) {
			bogusExecutableSearchPaths.add(dir);
		}
	}
	
	@Override
	public boolean isExecutable(File exec)
	{
		if (exec == null || ! exec.exists() || ! exec.isFile() || ! exec.canExecute()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public File findExecutable(String exec)
	{
		String fullname = getFullExecutableName(exec);
    	for (File dir : getExecutableSearchPath()) {
    		File file = new File(dir, fullname);
   			if (isExecutable(file)) {
   				return file;
   			}
    	}
    	
    	return null;
	}

	@Override
	public List<File> getExecutableSearchPath()
	{
		List<File> searchPath = getSystemExecutableSearchPath();
		searchPath.addAll(extraExecutableSearchPaths);
		searchPath.removeAll(bogusExecutableSearchPaths);
		
		return searchPath;
	}

	
	@Override
	public ProcessBuilder exec(File execFile)
	{
		return exec(execFile, null);
	}

	@Override
	public ProcessBuilder exec(File execFile, List<String> parameters)
	{
		
		if (! isExecutable(execFile)) {
			throw new IllegalArgumentException("Invalid executable file");
		}
		
		ProcessBuilder pb;
		if (parameters != null) {
			parameters.add(0, execFile.getAbsolutePath());
			pb = new ProcessBuilder(parameters.toArray(new String[parameters.size()]));
		} else {
			pb = new ProcessBuilder(execFile.getAbsolutePath());
		}
		
		return pb;
	}
	
	protected boolean isValidPath(File dir)
	{
		return (dir != null && dir.exists() && dir.isDirectory());
	}
	
	@Override
	public void addLibrarySearchPath(File dir)
	{
		if (isValidPath(dir)) {
			extraLibrarySearchPaths.add(dir);
		}
	}

	@Override
	public void removeLibrarySearchPath(File dir)
	{
		if (isValidPath(dir)) {
			bogusLibrarySearchPaths.add(dir);
		}
	}
	
	protected abstract List<File> getSystemLibrarySearchPath();

	@Override
	public List<File> getLibrarySearchPath()
	{
		List<File> searchPath = getSystemLibrarySearchPath();
		searchPath.addAll(extraLibrarySearchPaths);
		searchPath.removeAll(bogusLibrarySearchPaths);
		
		return searchPath;
	}
	
	@Override
	public boolean isLoadable(File libFile)
	{
		if (libFile == null || ! libFile.exists() || ! libFile.isFile()) {
			return false;
		}
		return true;
	}
	

	@Override
	public void loadLibrary(File library)
	{
		if (! isLoadable(library)) {
			throw new IllegalArgumentException("Invalid file");
		}
		try {
			Runtime runtime = Runtime.getRuntime();
			runtime.load(library.getAbsolutePath());
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public File findLibrary(String libName)
	{
		String fullname = getFullLibraryName(libName);
		Filesystem fs = new Filesystem();
    	for (File dir : getLibrarySearchPath()) {
    		List<File> files = fs.find(dir, 0, Pattern.compile(fullname + "(\\.(\\d+))?"));
    		for (File file : files) {
	    		if (isLoadable(file)) {
	    			return file;
	   			}
    		}
    	}
    	
    	return null;
	}
	
	public String getNickname()
	{
		return getType().nickname;
	}
	
	public String getNickname(ComputerArchitecture arch)
	{
		return getType().nickname + arch.width;
	}
}
