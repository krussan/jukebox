package se.qxx.jukebox;

import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.core.Main;
import se.qxx.jukebox.core.Starter;

public class Jukebox {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting up ...");
		
		Injector injector = Binder.setupBindings(args);
		Starter starter = injector.getInstance(Starter.class);
		Main main = injector.getInstance(Main.class);

		System.out.println("Binder initialized ...");
		if (starter.checkStart())
			main.run();
	}


}
