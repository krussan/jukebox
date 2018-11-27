package se.qxx.jukebox.interfaces;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.imdb.Imdb;
import se.qxx.jukebox.settings.parser.Parser;

public interface ISettings {

	public void initialize() throws IOException, JAXBException;
	
	public void readSettings() throws IOException, JAXBException;
	
	public Imdb getImdb();
	public Parser getParser();
	public JukeboxListenerSettings getSettings();
	

}