package se.qxx.jukebox.tests;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.google.inject.Injector;

import se.qxx.jukebox.Binder;
import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.settings.Settings;

public class TestGetMovie {

	public static void main(String[] args) throws IOException, JAXBException {
		Injector injector = Binder.setupBindings(args);
		IDatabase db = injector.getInstance(IDatabase.class);

		if (args.length > 0) {
			try {
				Movie m = db.getMovie(
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
