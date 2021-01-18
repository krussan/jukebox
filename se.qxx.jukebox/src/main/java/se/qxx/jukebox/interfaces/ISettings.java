package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.settings.*;

import java.io.IOException;

public interface ISettings {

	void initialize() throws IOException;

	void readSettings() throws IOException;

	ImdbTest getImdb();
	ParserTest getParser();
	SettingsTest getSettings();
	

}