package se.qxx.jukebox.tests;

import java.io.IOException;

import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.interfaces.IMkvSubtitleReader;

public class TestExtractMkv {

	public static void main(String[] args) {
		if (args.length > 1) {
			
			try {
				Injector injector = Binder.setupBindings(args);
				IMkvSubtitleReader subtitleReader = injector.getInstance(IMkvSubtitleReader.class);

				String filename = args[0];
				String outputPath = args[1];
				subtitleReader.extractSubs(filename, outputPath);
				
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
