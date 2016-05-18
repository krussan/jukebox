package se.qxx.jukebox.tests;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

public class GenerateDatabase {
	public static void main(String[] args) throws IOException, JAXBException {
		Version v = new Version();
		
		String filename = String.format("jukebox_clean_%s_%s.db", v.getMajor(), v.getMinor());
		ProtoDB db = new ProtoDB(filename);
		try {
			// generate main classes
			db.setupDatabase(Movie.getDefaultInstance());

			// generate support classes			
			db.setupDatabase(se.qxx.jukebox.domain.JukeboxDomain.Version.getDefaultInstance());
		} catch (ClassNotFoundException | SQLException
				| IDFieldNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
}
