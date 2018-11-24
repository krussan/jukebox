package se.qxx.jukebox.settings;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.settings.imdb.Imdb;
import se.qxx.jukebox.settings.parser.Parser;

@Singleton
public class Settings implements ISettings {
	private JukeboxListenerSettings settings;
	private IImdbSettings imdbSettings;
	private IParserSettings parserSettings;
	private IStreamingWebServer webServer;
	public int serverPort = 45444;	
	
	@Inject
	public Settings(IImdbSettings imdbSettings, IParserSettings parserSettings, IStreamingWebServer webServer) throws IOException, JAXBException {
		this.setImdbSettings(imdbSettings);
		this.setParserSettings(parserSettings);
		this.setWebServer(webServer);
		
		initialize();
	}
	
	public JukeboxListenerSettings getSettings() {
		return settings;
	}

	public void setSettings(JukeboxListenerSettings settings) {
		this.settings = settings;
	}

	public IStreamingWebServer getWebServer() {
		return webServer;
	}

	public void setWebServer(IStreamingWebServer webServer) {
		this.webServer = webServer;
	}

	public IParserSettings getParserSettings() {
		return parserSettings;
	}

	public void setParserSettings(IParserSettings parserSettings) {
		this.parserSettings = parserSettings;
	}

	public IImdbSettings getImdbSettings() {
		return imdbSettings;
	}

	public void setImdbSettings(IImdbSettings imdbSettings) {
		this.imdbSettings = imdbSettings;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.ISettings#initialize()
	 */
	@Override
	public void initialize() throws IOException, JAXBException {
		readSettings();
		
		if (this.getImdbSettings() != null)
			this.getImdbSettings().readSettings();
		
		if (this.getParserSettings() != null)
			this.getParserSettings().readSettings();

		if (this.getWebServer() != null)
			this.getWebServer().initializeMappings();
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.ISettings#imdb()
	 */
	@Override
	public Imdb getImdb() {
		return this.getImdbSettings().getImdb();
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.ISettings#parser()
	 */
	@Override
	public Parser getParser() {
		return this.getParserSettings().getSettings();
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.ISettings#readSettings()
	 */
	@Override
	public void readSettings() throws IOException, JAXBException {
		readSettingFile();
	}
	
	private void readSettingFile() throws IOException, JAXBException {
		JAXBContext c = JAXBContext.newInstance(JukeboxListenerSettings.class);
		Unmarshaller u = c.createUnmarshaller();
		
		JAXBElement<JukeboxListenerSettings> root = u.unmarshal(new StreamSource(new File("JukeboxSettings.xml")), JukeboxListenerSettings.class);
		this.setSettings(root.getValue());
		
	}
}
