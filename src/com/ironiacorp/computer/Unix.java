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
import java.util.ArrayList;
import java.util.List;

public class Unix extends AbstractOperationalSystem
{
	public static final OperationalSystemType type = OperationalSystemType.Linux;
		
	public static final String DEFAULT_EXEC_EXTENSION = "";

	public static final String DEFAULT_LIBRARY_EXTENSION = ".so";

	public static final String DEFAULT_LIBRARY_PREFIX = "lib";
	
	/**
	 * Default path for GraphViz in Unix systems.
	 */
	public static final String[] DEFAULT_EXEC_PATH = {
		"/bin",
		"/sbin",
		"/usr/bin",
		"/usr/sbin",
		"/usr/local/bin",
		"/usr/local/sbin",
	};
	
	/**
	 * Default path for GraphViz in Unix systems.
	 */
	public static final String[] DEFAULT_LIBRARY_PATH = {
		"/lib",
		"/usr/lib",
		"/usr/local/lib",
	};

	@Override
	public String getFullLibraryName(String libName)
	{
		return DEFAULT_LIBRARY_PREFIX + libName + DEFAULT_LIBRARY_EXTENSION;	
	}
	// x86_64-linux-gnu
	@Override
	protected List<File> getSystemLibrarySearchPath()
	{
		ComputerArchitectureDetector archDetector = new ComputerArchitectureDetector();
		ComputerArchitecture arch = archDetector.detectCurrentArchitecture();
		String dataModel = System.getProperty("sun.arch.data.model");
		String currentSearchPath = System.getenv("LD_LIBRARY_PATH");
		List<String> rawResult = new ArrayList<String>();
		List<File> result = new ArrayList<File>();

		for (String path : DEFAULT_LIBRARY_PATH) {
			File dir = new File(path);
			if (isValidPath(dir)) {
				rawResult.add(path);
			}
		}
		
		if (currentSearchPath != null) {
			for (String path : currentSearchPath.split(File.pathSeparator)) {
				File dir = new File(path);
				if (isValidPath(dir)) {
					rawResult.add(path);
				}
			}
		}
		
		for (String dirname : rawResult) {
			// Plain path + data model (32 or 64)
			File dir = new File(dirname + dataModel);
			if (isValidPath(dir)) {
				result.add(dir);
			}
			
			// Plain path + os.arch and flavours
			dir = new File(dirname + File.separator + arch.toString() + "-linux-gnu");
			if (isValidPath(dir)) {
				result.add(dir);
			}
			for (String acronym : arch.acronyms) {
				dir = new File(dirname + File.separator + acronym + "-linux-gnu");
				if (isValidPath(dir)) {
					result.add(dir);
				}
			}
		}
		
		return result;
	}
	

	@Override
	public String getFullExecutableName(String execName)
	{
		return execName;
	}
	
	@Override
	protected List<File> getSystemExecutableSearchPath()
	{
		String currentSearchPath = System.getenv("PATH");
		List<File> result = new ArrayList<File>();
		for (String dirname : currentSearchPath.split(File.pathSeparator)) {
			File dir = new File(dirname);
			if (isValidPath(dir)) {
				result.add(dir);
			}
		}

		for (String path : DEFAULT_EXEC_PATH) {
			File dir = new File(path);
			if (isValidPath(dir)) {
				result.add(dir);
			}
		}

		return result;
	}
	
	@Override
	public OperationalSystemType getType() {
		return type;
	}
}
