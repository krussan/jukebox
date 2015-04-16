package se.qxx.jukebox.front;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class FrontSettings {
	
	private Properties prop;
	private static FrontSettings _instance;
	
	private FrontSettings() { 
		prop = new Properties();
		 
    	try {
            //load a properties file
    		  BufferedReader reader = new BufferedReader(
    		           new InputStreamReader(
    		                      new FileInputStream(this.getPropFile()), "UTF8"));

    		prop.load(reader);
 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }		
	}
	
	public String getPropFile() {
		return getString("", "props-file", "jukeboxFront.prop");
	}
	
	public int getPort() {
		return getInt("jukeboxfront.port", "front-port", 2156);
	}
	
	public String getServer() {
		return getString("jukebox.server", "jukebox-server-address", "127.0.0.1");
	}
	
	public int getServerPort() {
		return getInt("jukebox.port", "jukebox-server-port", 2150);
	}
	
	public String getT9ActiveKeymap() {
		return getString("T9.active.keymap", "keymap", "sv");
	}
	
	public String getT9key(int key) {
		return getString(String.format("T9.%s.%s", getT9ActiveKeymap(), key), "", "");
	}
	
	public String getLibVlcPath() {
		
		return prop.getProperty("libvlc.path");
	}
	
	public static FrontSettings get() {
		if (_instance == null)
			_instance = new FrontSettings();
		
		return _instance;
	}
	
	private int getInt(String propertyName, String argumentOption, int defaultValue) {
		String value = prop.getProperty(propertyName);
		
		if (Arguments.cmd().hasOption(argumentOption))
			value = Arguments.cmd().getOptionValue(argumentOption);
		
		if (!StringUtils.isNumeric(value) || StringUtils.isEmpty(value))
			return defaultValue;

		return Integer.parseInt(value);		
	}
	
	private String getString(String propertyName, String argumentOption, String defaultValue) {
		String value = StringUtils.EMPTY;
		
		if (Arguments.cmd().hasOption(argumentOption))
			value = Arguments.cmd().getOptionValue(argumentOption);
		else
			value = prop.getProperty(propertyName);
		
		if (StringUtils.isEmpty(value))
			return defaultValue;
		else
			return value;
	}
}
