package se.qxx.jukebox.tests;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.interfaces.IDatabase;

public class TestGetMovie {
	
	private IDatabase database;

	@Inject
	public TestGetMovie(IDatabase database) {
		this.database = database;
		
	}

	public static void main(String[] args) {
		Injector injector = Binder.setupBindings(args);
		TestGetMovie prog = injector.getInstance(TestGetMovie.class);
		
		if (args.length > 0) {
			prog.execute(Integer.parseInt(args[0]));
		}
		else {
			System.out.println("No arguments");
		}
	}
	
	public void execute(int movieId) {
		try {
			Movie m = this.database.getMovie(movieId);
			
				
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
	

}
