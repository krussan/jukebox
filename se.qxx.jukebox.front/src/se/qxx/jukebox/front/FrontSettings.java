package se.qxx.jukebox.front;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class FrontSettings {
	
	private Properties prop;
	private static FrontSettings _instance;
	
	private FrontSettings() { 
		prop = new Properties();
		 
    	try {
            //load a properties file
    		prop.load(new FileInputStream(this.getPropFile()));
 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }		
	}
	
	public String getPropFile() {
		return getString("", "props", "jukeboxFront.prop");
	}
	
	public int getPort() {
		return getInt("jukeboxfront.port", "p", 2156);
	}
	
	public String getServer() {
		if (Arguments.cmd().hasOption("ip"))
			return Arguments.cmd().getOptionValue("ip");
		else
			return prop.getProperty("jukebox.server");
	}
	
	public int getServerPort() {
		return getInt("jukebox.port", "sp", 2150);
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
