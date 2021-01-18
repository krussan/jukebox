package se.qxx.jukebox.tests;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.interfaces.IDatabase;

public class DeleteSeries {

	public static void main(String[] args) {
		if (args.length > 0) {
			
			try {
				Series s = null;
				IDatabase db = getDatabaseClass(args);
				
				if (StringUtils.isNumeric(args[0])) {
					int id = Integer.parseInt(args[0]);
					s = db.getSeries(id);
					
					if (s != null) 
						db.delete(s);
					else 
						System.out.println("Nothing found!");					
				}
				else {
					System.out.println("Please supply the series ID!");
				}
				
				
			} catch (Exception e) {
				System.out.println("failed to get information from database");
				System.out.println(e.toString());

			}
		}
		else {
			System.out.println("No arguments");
		}
	}

	private static IDatabase getDatabaseClass(String[] args) {
		Injector injector = Binder.setupBindings(args);
		IDatabase db = injector.getInstance(IDatabase.class);
		return db;
	}
	

}
