package se.qxx.jukebox.front;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FrontSettings {
	
	private Properties prop;
	private static FrontSettings _instance;
	
	private FrontSettings(String propFile) {
		prop = new Properties();
		 
    	try {
            //load a properties file
    		prop.load(new FileInputStream(propFile));
 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }		
	}
	
	public int getPort() {
		return Integer.parseInt(prop.getProperty("jukeboxfront.port", "2156"));		
	}
	
	public String getServer() {
		return prop.getProperty("jukebox.server");
	}
	
	public int getServerPort() {
		return Integer.parseInt(prop.getProperty("jukebox.port", "2150"));
	}
	
	public String getLibVlcPath() {
		return prop.getProperty("libvlc.path");
	}
	
	public static FrontSettings get() {
		if (_instance == null)
			_instance = new FrontSettings();
		
		return _instance;
	}
}
