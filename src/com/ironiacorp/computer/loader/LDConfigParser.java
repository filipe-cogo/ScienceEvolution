package com.ironiacorp.computer.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ironiacorp.finder.FileFinder;

public class LDConfigParser
{
	public LDConfigParser()
	{
		parse();
	}
	
	public void parse()
	{
		File config = new File("/etc/ld.so.conf");
		if (! config.isFile()) {
			throw new IllegalArgumentException("No configuration file found for Linux dynamic linker.");
		}

		FileFinder finder = new FileFinder();
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			fis = new FileInputStream(config);
			isr = new InputStreamReader(fis, "UTF-8");
			reader = new BufferedReader(isr);
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("include ")) {
					String[] words = line.split("\\s");
					finder.find("/etc", words[1]);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Error found while parsing the configuration file.");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e1) {
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {
				}
			}
		}
	}
}
