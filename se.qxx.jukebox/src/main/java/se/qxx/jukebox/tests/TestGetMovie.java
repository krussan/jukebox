package se.qxx.jukebox.tests;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.ProtoDBFactory;
import se.qxx.protodb.SearchOptions;
import se.qxx.protodb.model.ProtoDBSearchOperator;

public class TestGetMovie {

	public static void main(String[] args) throws IOException, JAXBException {
		Settings.initialize();
		
		if (args.length > 0) {
			try {
				String driver = Settings.get().getDatabase().getDriver();
				String connectionString = Settings.get().getDatabase().getConnectionString();
				
				Movie m = DB.getMovie(
					Integer.parseInt(args[0]));
				
					
				if (m != null) {
					System.out.println(m);
				}
				else 
					System.out.println("Nothing found!");
				
			} catch (Exception e) {
				System.out.println("failed to get information from database");
				System.out.println(e.toString());

			}
		}
		else {
			System.out.println("No arguments");
		}
	}
	

}
