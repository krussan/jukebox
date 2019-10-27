package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.settings.Imdb;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Parser;

import java.io.IOException;

public interface ISettings {

	void initialize() throws IOException;

	void readSettings() throws IOException;

	Imdb getImdb();
	Parser getParser();
	JukeboxListenerSettings getSettings();
	

}