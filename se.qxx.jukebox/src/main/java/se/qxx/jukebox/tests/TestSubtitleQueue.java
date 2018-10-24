package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.settings.Settings;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public class TestSubtitleQueue {

	public static void main(String[] args) throws IOException, JAXBException, DatabaseNotSupportedException {
		try {
			Settings.initialize();
			
			DB.getSubtitleQueue();
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	


}
