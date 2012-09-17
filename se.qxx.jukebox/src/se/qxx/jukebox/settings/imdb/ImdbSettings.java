package se.qxx.jukebox.settings.imdb;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class ImdbSettings {
	private static ImdbSettings _instance;
	private Imdb _imdb;
	
	private ImdbSettings() {
	}
	
	private static ImdbSettings getInstance() {
		if (_instance == null) {
			_instance = new ImdbSettings();
		}
			
		return _instance;
	}
	
	public static Imdb get() {
		return getInstance()._imdb;
	}
	
	public static void readSettings() throws IOException, JAXBException {
		getInstance().readSettingFile();
	}
	
	private void readSettingFile() throws IOException, JAXBException {
		JAXBContext c = JAXBContext.newInstance(Imdb.class);
		Unmarshaller u = c.createUnmarshaller();
		
		JAXBElement<Imdb> root = u.unmarshal(new StreamSource(new File("imdb.xml")), Imdb.class);
		_imdb = root.getValue();
		
	}

}
