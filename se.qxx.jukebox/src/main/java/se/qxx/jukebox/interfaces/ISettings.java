package se.qxx.jukebox.interfaces;

import java.io.IOException;

import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Imdb;
import se.qxx.jukebox.settings.Parser;

interface ISettings {

	void initialize() throws IOException;
	
	void readSettings() throws IOException;
	
	Imdb getImdb();
	Parser getParser();
	JukeboxListenerSettings getSettings();
	

}