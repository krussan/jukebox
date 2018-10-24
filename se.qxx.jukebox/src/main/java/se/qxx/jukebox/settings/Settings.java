package se.qxx.jukebox.settings;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import se.qxx.jukebox.settings.imdb.Imdb;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;
import se.qxx.jukebox.webserver.StreamingWebServer;


public class Settings {

	private static Settings _instance;
	private JukeboxListenerSettings _settings;
	
	public int serverPort = 45444;	
	
	private Settings() {
	}
	
	public static void initialize() throws IOException, JAXBException {
		Settings.readSettings();
		ImdbSettings.readSettings();
		ParserSettings.readSettings();
		
		if (StreamingWebServer.isInitialized())
			StreamingWebServer.get().initializeMappings();
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
	
	public static Imdb imdb() {
		return ImdbSettings.get();
	}
	
	public static ParserSettings parser() {
		return ParserSettings.getInstance();
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
