package se.qxx.jukebox.tests;

import java.io.IOException;

import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public class TestFindSeries {

	public static void main(String[] args) {
		try {
			Injector injector = Binder.setupBindings(args);
			IDatabase db = injector.getInstance(IDatabase.class);

			db.findSeries("The Walking Dead");
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	


}
