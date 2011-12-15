package se.qxx.jukebox.settings;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;


public class Settings {

	private static Settings _instance;
	private JukeboxListenerSettings _settings;
	
	public int serverPort = 45444;	
	
	private Settings() {
	}
	
	private static Settings getInstance() {
		if (_instance == null) {
			_instance = new Settings();
		}
		
		return _instance;
	}
	
	public static JukeboxListenerSettings get() {
		return getInstance()._settings;
	}
	
	public static void readSettings() throws IOException, JAXBException {
		getInstance().readSettingFile();
	}
	
	private void readSettingFile() throws IOException, JAXBException {
		JAXBContext c = JAXBContext.newInstance(JukeboxListenerSettings.class);
		Unmarshaller u = c.createUnmarshaller();
		
		JAXBElement<JukeboxListenerSettings> root = u.unmarshal(new StreamSource(new File("JukeboxSettings.xml")), JukeboxListenerSettings.class);
		_settings = root.getValue();
		
	}
}
