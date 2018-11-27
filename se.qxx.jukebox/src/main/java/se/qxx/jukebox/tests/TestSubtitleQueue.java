package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.settings.Settings;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;

public class TestSubtitleQueue {

	public static void main(String[] args) throws IOException, JAXBException, DatabaseNotSupportedException {
		try {
			Injector injector = Binder.setupBindings(args);
			IDatabase db = injector.getInstance(IDatabase.class);
			
			db.getSubtitleQueue();
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	


}
