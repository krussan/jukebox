package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.settings.Imdb;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Parser;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public interface ISettings {

	void initialize() throws IOException, JAXBException;

	void readSettings() throws IOException, JAXBException;

	Imdb getImdb();
	Parser getParser();
	JukeboxListenerSettings getSettings();
	

}