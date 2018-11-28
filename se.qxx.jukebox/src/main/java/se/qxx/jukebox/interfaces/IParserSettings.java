package se.qxx.jukebox.interfaces;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.settings.parser.Parser;

public interface IParserSettings {

	void readSettings() throws IOException, JAXBException;

	Parser getSettings();

}