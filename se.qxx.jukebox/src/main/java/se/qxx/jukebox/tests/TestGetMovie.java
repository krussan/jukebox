package se.qxx.jukebox.tests;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.DB;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;

public class TestGetMovie {

	public static void main(String[] args) throws IOException, JAXBException {
		Settings.initialize();
		
		if (args.length > 0) {
			try {
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
