package se.qxx.jukebox.tests;

import com.google.inject.Injector;
import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.interfaces.IDatabase;

public class TestFindSeries {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Need to specify a series title");
		}
		try {
			Injector injector = Binder.setupBindings(args);
			IDatabase db = injector.getInstance(IDatabase.class);

			db.findSeries(args[0]);
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	


}
