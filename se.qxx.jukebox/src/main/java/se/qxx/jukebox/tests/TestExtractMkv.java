package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import com.google.inject.Injector;

import se.qxx.jukebox.Binder;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;
import se.qxx.jukebox.subtitles.IMkvSubtitleReader;
import se.qxx.jukebox.subtitles.MkvSubtitleReader;

public class TestExtractMkv {

	public static void main(String[] args) throws IOException, JAXBException {
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
