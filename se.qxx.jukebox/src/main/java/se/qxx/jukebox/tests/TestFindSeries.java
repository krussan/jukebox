package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.settings.Settings;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public class TestFindSeries {

	public static void main(String[] args) throws IOException, JAXBException, DatabaseNotSupportedException {
		try {
			Settings.initialize();
			
			DB.findSeries("The Walking Dead");
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	


}
