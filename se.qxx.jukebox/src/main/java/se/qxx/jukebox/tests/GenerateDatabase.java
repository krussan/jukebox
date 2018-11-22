package se.qxx.jukebox.tests;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.core.Version;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.ProtoDBFactory;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;

public class GenerateDatabase {
	public static void main(String[] args) throws IOException, JAXBException, DatabaseNotSupportedException {
		Version v = new Version();
		
		//String filename = String.format("jukebox_clean_%s_%s.db", v.getMajor(), v.getMinor());
		//ProtoDB db = new ProtoDB(filename);
		if (args.length > 2) {
			String driver = args[0];
			String connectionString = args[1];
			
			ProtoDB db = ProtoDBFactory.getInstance(driver, connectionString);
			
			try {
				// generate main classes
				db.setupDatabase(Movie.getDefaultInstance());

				// generate support classes			
				db.setupDatabase(se.qxx.jukebox.domain.JukeboxDomain.Version.getDefaultInstance());
				
				db.setupDatabase(Series.getDefaultInstance());
				se.qxx.jukebox.domain.JukeboxDomain.Version ver = 
						se.qxx.jukebox.domain.JukeboxDomain.Version.newBuilder()
						.setMajor(v.getMajor())
						.setMinor(v.getMinor())
						.build();

				db.save(ver);
				
			} catch (ClassNotFoundException | SQLException
					| IDFieldNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}
}
