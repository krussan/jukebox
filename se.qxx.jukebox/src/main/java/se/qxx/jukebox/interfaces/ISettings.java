package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.settings.Imdb;
import se.qxx.jukebox.settings.Parser;
import se.qxx.jukebox.settings.SettingsTest;

import java.io.IOException;

public interface ISettings {

	void initialize() throws IOException;

	void readSettings() throws IOException;

	Imdb getImdb();
	Parser getParser();
	SettingsTest getSettings();
	

}