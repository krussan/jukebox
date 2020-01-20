package se.qxx.jukebox.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import se.qxx.jukebox.interfaces.ISettings;

import java.io.File;
import java.io.IOException;

@Singleton
public class Settings implements ISettings {
	private SettingsTest settings;
	private ImdbTest imdbSettings;
	private ParserTest parserSettings;

	@Inject
	public Settings() throws IOException {
		initialize();
	}
	
	public SettingsTest getSettings() {
		return settings;
	}

	public void setSettings(SettingsTest settings) {
		this.settings = settings;
	}

	public ParserTest getParserSettings() {
		return parserSettings;
	}

	public void setParserSettings(ParserTest parserSettings) {
		this.parserSettings = parserSettings;
	}

	public ImdbTest getImdb() {
		return imdbSettings;
	}

	public void setImdb(ImdbTest imdbSettings) {
		this.imdbSettings = imdbSettings;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.ISettings#initialize()
	 */
	@Override
	public void initialize() throws IOException {
		readSettings();
		/*
		if (this.getImdbSettings() != null)
			this.getImdbSettings().readSettings();
		
		if (this.getParserSettings() != null)
			this.getParserSettings().readSettings();

		 */
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.ISettings#parser()
	 */
	@Override
	public ParserTest getParser() {
		return this.getParserSettings();
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.settings.ISettings#readSettings()
	 */
	@Override
	public void readSettings() throws IOException {
		readSettingFile();
	}
	
	private void readSettingFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		this.setSettings(mapper.readValue(new File("settings.yaml"), SettingsTest.class));
		this.setImdb(mapper.readValue(new File("imdb.yaml"), ImdbTest.class));
		this.setParserSettings(mapper.readValue(new File("parser.yaml"), ParserTest.class));
	}
}
