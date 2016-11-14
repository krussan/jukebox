package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.builders.ParserMovie;
import se.qxx.jukebox.settings.Settings;

public class TestParserBuilder {
	public static void main(String[] args) throws IOException, JAXBException {
		
		Settings.initialize();
		
		if (args.length > 0) {
			ParserBuilder b = new ParserBuilder();
			ParserMovie pm = b.extractMovieParser("", args[0]);
				
			System.out.println(pm.toString());
		}
	}
	
}
