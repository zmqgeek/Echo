package util;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Load the configurations of the analysis from the default location or a given one.
 * 
 * @author echo
 */
public class Config {
	// Keys of configuration items
	public static final String APPDIR = "appdir";
	public static final String ANDROIDJAR = "androidjar";
	// public static final String INFOFLOW = "soot-infoflow";
	// public static final String INFOFLOWANDROID = "soot-infoflow-android";
	public static final String RES = "res";
	public static final String NEWPROJECT = "2018-new-project";
	public static final String OUTPUT = "output";
	public static final String SDK = "SDK";
	public static final String ADB = "ADB";
	public static final String SYSACTS = "system-activities";

	// Default names of configuration file
	public static final String CONFIG = "config.properties";

	private File configFile;
	private Properties configs;

	private static Config singleton;

	/**
	 * Initialize the Config class with the location of config file. 
	 * If null is specified, find the configuration file with the default name in current directory.
	 */
	public static void init(String configFileLocation) {
		if(configFileLocation == null)
			singleton = new Config();
		else
			singleton = new Config(configFileLocation);
	}

	public static Config v() {
		if(singleton == null)
			throw new RuntimeException("Config has not been initialized yet.");
		return singleton;
	}

	private Config() {
		try {
			// try to find the config file in current directory
			initConfigFile(String.join(File.separator, ".", CONFIG));
		} catch (Exception e) {
			// if the config file is not found in current directory, 
			// then try to find in the outer directory
			initConfigFile(String.join(File.separator, ".", "..", CONFIG));
		}
	}

	private Config(String configFileLocation) {
		initConfigFile(configFileLocation);
	}

	private void initConfigFile(String configFileLocation) {
		File file = new File(configFileLocation);
		if(file.exists() && file.isFile())
			configFile = file;
		else {
			throw new RuntimeException("File " + configFileLocation + " does not exist or it is not an valid file.");
		}
		configs = new Properties();
		try {
			configs.load(new FileReader(configFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obtain the configuration value with given key. Return null if the given key is not specified.
	 */
	public String get(String key) {
		String value = configs.getProperty(key);
		if(value == null || value.isEmpty())
			throw new RuntimeException("Configuration " + key + " is not specified.");
		return value;
	}

	public void printAllConfigs() {
		configs.forEach((k, v) -> System.out.println(k + " : " + v));
	}

	// Test
	public static void main(String[] args) {
		Config configuration = new Config();
		configuration.printAllConfigs();
	}
}
