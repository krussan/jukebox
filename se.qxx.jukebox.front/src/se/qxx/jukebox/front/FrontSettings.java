package se.qxx.jukebox.front;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FrontSettings {
	
	private Properties prop;
	private static FrontSettings _instance;
	
	private FrontSettings() {
		prop = new Properties();
		 
    	try {
            //load a properties file
    		prop.load(new FileInputStream("jukeboxFront.prop"));
 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }		
	}
	
	public int getPort() {
		return Integer.parseInt(prop.getProperty("jukeboxfront.port", "2156"));		
	}
	
	public static FrontSettings get() {
		if (_instance == null)
			_instance = new FrontSettings();
		
		return _instance;
	}
}
