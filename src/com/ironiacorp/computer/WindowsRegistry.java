/* 
 * Java native interface to the Windows Registry API.
 * 
 * Authored by Timothy Gerard Endres
 * <mailto:time@gjt.org>  <http://www.trustice.com>
 * 
 * Changed by Maso Gato
 * <mailto:masogato@users.sourceforge.net>
 * Changelog:
 * 20050315 : If 'ICE_JNIRegistry.DLL' DLL is not installed, then 
 * static code try to load it from the 'registry.jar' directory. 
 * 
 * This work has been placed into the public domain.
 * You may use this work in any way and for any purpose you wish.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
 * NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
 OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
 * CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
 * REDISTRIBUTION OF THIS SOFTWARE. 
 */

package com.ironiacorp.computer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.jinterop.dcom.common.IJIAuthInfo;
import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;
import org.jinterop.dcom.common.JIException;
import org.jinterop.winreg.IJIWinReg;
import org.jinterop.winreg.JIPolicyHandle;
import org.jinterop.winreg.JIWinRegFactory;

import com.ice.jni.registry.NoSuchKeyException;
import com.ice.jni.registry.RegistryException;
import com.ice.jni.registry.RegistryKey;

/**
 * The Registry class provides is used to load the native library DLL, as well
 * as a placeholder for the top level keys, error codes, and utility methods. <br>
 */
public class WindowsRegistry {
	/**
	 * The following statics are the top level keys. Without these, there is no
	 * way to get "into" the registry, since the RegOpenSubkey() call requires
	 * an existing key which contains the subkey.
	 */
	private RegistryKey HKEY_CLASSES_ROOT;
	private RegistryKey HKEY_CURRENT_USER;
	private RegistryKey HKEY_LOCAL_MACHINE;
	private RegistryKey HKEY_USERS;
	private RegistryKey HKEY_PERFORMANCE_DATA;
	private RegistryKey HKEY_CURRENT_CONFIG;
	private RegistryKey HKEY_DYN_DATA;

	/**
	 * Map names to the top level keys.
	 */
	private Map<String, RegistryKey> topLevelKeys = null;

	public WindowsRegistry() {
		loadLibrary();
		initializeTopKeys();
	}

	/**
	 * Loads the DLL needed for the native methods, creates the toplevel keys,
	 * fills the hashtable that maps various names to the toplevel keys.
	 */
	private void loadLibrary() {
		try {
			System.loadLibrary("ICE_JNIRegistry");
		} catch (UnsatisfiedLinkError e) {
			loadLibraryFromJarDirectory();

		} catch (SecurityException e) {
			System.err.println("ERROR You do not have permission to load the DLL named 'ICE_JNIRegistry.DLL'.\n\t" + e.getMessage());
		}
	}

	/**
	 * Try to load 'ICE_JNIRegistry.DLL' DLL from the 'registry.jar' directory.
	 */
	private void loadLibraryFromJarDirectory() {
		ClassLoader loader = RegistryKey.class.getClassLoader();
		URL urlJar = loader.getResource("com/ice/jni/registry/RegistryKey.class");

		try {
			URLConnection urlCon = urlJar.openConnection();
			if (!(urlCon instanceof JarURLConnection)) {
				// Try to load from the directory!
				System.err
						.println("ERROR You may have the REGISTRY api into JAR file.\n\t");
				return;
			}

			JarURLConnection jarCon = (JarURLConnection) urlCon;
			File jarFile = new File(jarCon.getJarFileURL().getFile());
			File directory = jarFile.getParentFile();
			URI directoryURI = new URI(directory.getAbsolutePath());
			// String directoryPath = URLDecoder.decode(directory.getAbsolutePath(), "UTF-8");
			// Runtime.getRuntime().load(directoryPath + File.separator + "ICE_JNIRegistry.DLL");
			Runtime.getRuntime().load(directoryURI + File.separator + "ICE_JNIRegistry.DLL");
		} catch (URISyntaxException ue) {
			System.err.println(ue.getMessage());
		} catch (IOException e) {
			System.err.println("ERROR You have not the DLL named 'ICE_JNIRegistry.DLL' into same directory than JAR file.\n\t" + e.getMessage());
		} catch (UnsatisfiedLinkError ule) {
			System.err.println("ERROR You have not installed the DLL named 'ICE_JNIRegistry.DLL'.\n\t" + ule.getMessage());
		}
	}

	private void initializeTopKeys() {
		HKEY_CLASSES_ROOT = new RegistryKey(0x80000000, "HKEY_CLASSES_ROOT");
		HKEY_CURRENT_USER = new RegistryKey(0x80000001, "HKEY_CURRENT_USER");
		HKEY_LOCAL_MACHINE = new RegistryKey(0x80000002, "HKEY_LOCAL_MACHINE");
		HKEY_USERS = new RegistryKey(0x80000003, "HKEY_USERS");
		HKEY_PERFORMANCE_DATA = new RegistryKey(0x80000004, "HKEY_PERFORMANCE_DATA");
		HKEY_CURRENT_CONFIG = new RegistryKey(0x80000005, "HKEY_CURRENT_CONFIG");
		HKEY_DYN_DATA = new RegistryKey(0x80000006, "HKEY_DYN_DATA");

		topLevelKeys = new HashMap<String, RegistryKey>(16);
		topLevelKeys.put("HKCR", HKEY_CLASSES_ROOT);
		topLevelKeys.put("HKEY_CLASSES_ROOT", HKEY_CLASSES_ROOT);
		topLevelKeys.put("HKCU", HKEY_CURRENT_USER);
		topLevelKeys.put("HKEY_CURRENT_USER", HKEY_CURRENT_USER);
		topLevelKeys.put("HKLM", HKEY_LOCAL_MACHINE);
		topLevelKeys.put("HKEY_LOCAL_MACHINE", HKEY_LOCAL_MACHINE);
		topLevelKeys.put("HKU", HKEY_USERS);
		topLevelKeys.put("HKUS", HKEY_USERS);
		topLevelKeys.put("HKEY_USERS", HKEY_USERS);
		topLevelKeys.put("HKPD", HKEY_PERFORMANCE_DATA);
		topLevelKeys.put("HKEY_PERFORMANCE_DATA", HKEY_PERFORMANCE_DATA);
		topLevelKeys.put("HKCC", HKEY_CURRENT_CONFIG);
		topLevelKeys.put("HKEY_CURRENT_CONFIG", HKEY_CURRENT_CONFIG);
		topLevelKeys.put("HKDD", HKEY_DYN_DATA);
		topLevelKeys.put("HKEY_DYN_DATA", HKEY_DYN_DATA);
	}

	private void checkKey(RegistryKey topKey, String keyName, int access) {
		try {
			topKey.openSubKey(keyName, access);
		} catch (NoSuchKeyException e) {
			throw new IllegalArgumentException("Key '" + keyName + "' does not exist.", e);
		} catch (RegistryException e) {
			throw new IllegalArgumentException("Registry error", e);
		}
	}

	/**
	 * Get the description of a Registry error code.
	 * 
	 * @param errCode
	 *            The error code from a RegistryException
	 * @return The description of the error code.
	 */

	private void checkKey(String keyName) {
		if (keyName.startsWith("\\\\")) {
			throw new IllegalArgumentException("Remote registry access is not allowed");
		}

		int index = keyName.indexOf('\\');
		if (index >= 0 && index < 4) {
			throw new IllegalArgumentException("Invalid key '" + keyName + "', top level key name too short.");
		}

		// "topLevelKeyname\subKey\subKey\..."
		String topKeyName = keyName.substring(0, index);
		RegistryKey topKey = getTopLevelKey(topKeyName);
		if (topKey == null) {
			throw new IllegalArgumentException("Toplevel key '" + topKeyName + "' could not be resolved");
		}
	}

	
	
	/**
	 * Get a top level key by name using the top level key Hashtable.
	 * 
	 * @param keyName
	 *            The name of the top level key.
	 * @return The top level RegistryKey, or null if unknown keyName.
	 * 
	 * @see topLevelKeys
	 */
	private RegistryKey getTopLevelKey(String keyName) {
		return (RegistryKey) topLevelKeys.get(keyName);
	}

	private String getString(RegistryKey topKey, String keyName, String valueName) {
		try {
			RegistryKey subKey = topKey.openSubKey(keyName, RegistryKey.ACCESS_READ); 
			if (subKey == null) {
				throw new IllegalArgumentException();
			}

			String value = subKey.getStringValue(valueName);
			
			return value;
		} catch (RegistryException e) {
			throw new IllegalArgumentException("Error getting value '" + valueName + "'", e);
		}
	}
	
	public String getString(String key, String value)
	{
		return getStringUsingRegEdit(key, value);
	}
	
	
	private String getStringUsingICE(String keyName)
	{
		int index = keyName.indexOf('\\');

		String topKeyName = keyName.substring(0, index);
		if ((index + 1) >= keyName.length()) {
			keyName = null;
		} else {
			keyName = keyName.substring(index + 1);
		}

		RegistryKey topKey = getTopLevelKey(topKeyName);
		if (topKey == null) {
			throw new IllegalArgumentException("Toplevel key '" + topKeyName + "' could not be resolved");
		}

		int lastIndex = keyName.lastIndexOf("\\");
		String valueName = keyName.substring(lastIndex + 1);
		keyName = keyName.substring(0, lastIndex);
		
		return getString(topKey, keyName, valueName);
	}
	
	
    private String getStringUsingJInterop(String key)
    {
            String domain = "";
            String username = "";
            String password = "";
            String dir = null;

            // IJIWinReg winReg = JIWinRegFactory.getSingleTon().getWinreg(hostInfo, hostInfo.getHost(), true);
            IJIAuthInfo authInfo = new JIDefaultAuthInfoImpl(domain, username, password);
            try {
                    IJIWinReg registry = JIWinRegFactory.getSingleTon().getWinreg(authInfo, domain, true);
                    JIPolicyHandle policyHandle1 = registry.winreg_OpenHKLM();
                    JIPolicyHandle policyHandle2 = registry.winreg_OpenKey(policyHandle1, key, IJIWinReg.KEY_READ);
                    Object[] value = registry.winreg_QueryValue(policyHandle2, "InstallPath", 4096);
                    dir = (String) value[0];
                    registry.winreg_CloseKey(policyHandle2);
                    registry.winreg_CloseKey(policyHandle1);
            } catch (JIException e) {
            } catch (UnknownHostException JavaDoc) {
            }

            return dir;
    }
    

    /**
     * Read GraphViz installation path using RegEdit.exe
     * 
     * @return Directory where GraphViz has been installed (and null if not found).
     */
    private String getStringUsingRegEdit(String key, String value)
    {
            final String REGQUERY_UTIL = "reg query ";
            final String REGSTR_TOKEN = "REG_EXPAND_SZ";
            final String COMPUTER_WINDOWS_GRAPHVIZ_FOLDER = REGQUERY_UTIL + "\"" + key + "\"" + "/v " + value;

            InputStreamReader isr = null;
            BufferedReader reader = null;
            try {
               Process process = Runtime.getRuntime().exec(COMPUTER_WINDOWS_GRAPHVIZ_FOLDER);
                isr = new InputStreamReader(process.getInputStream(), "UTF-8");
                reader = new BufferedReader(isr);
                process.waitFor();
                String result = reader.readLine();
                if (result != null) {
                    int p = result.indexOf(REGSTR_TOKEN);
                    if (p != -1) {
                    	return result.substring(p + REGSTR_TOKEN.length()).trim();
                    }
                }
                return null;
            } catch (IOException e) {
            	return null;
            } catch (InterruptedException ie) {
            	return null;
            } finally {
            	if (reader != null) {
            		try {
            			reader.close();
            		} catch (IOException ioe) {}
            	}
            	if (isr != null) {
            		try {
            			isr.close();
            		} catch (IOException ioe) {}
            	}
            }
    }
}