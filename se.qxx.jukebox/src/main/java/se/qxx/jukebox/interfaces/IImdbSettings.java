package se.qxx.jukebox.interfaces;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.settings.imdb.Imdb;

public interface IImdbSettings {

	void readSettings() throws IOException, JAXBException;
	Imdb getImdb();
}