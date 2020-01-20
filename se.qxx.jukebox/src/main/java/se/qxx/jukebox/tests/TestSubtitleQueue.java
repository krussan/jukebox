package se.qxx.jukebox.tests;

import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.interfaces.IDatabase;

public class TestSubtitleQueue {

	public static void main(String[] args) {
		try {
			Injector injector = Binder.setupBindings(args);
			IDatabase db = injector.getInstance(IDatabase.class);
			
			db.getSubtitleQueue();
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	


}
