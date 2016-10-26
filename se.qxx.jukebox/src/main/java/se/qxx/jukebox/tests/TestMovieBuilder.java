package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.settings.Settings;

public class TestMovieBuilder {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			Settings.initialize();	
			
			String filename = args[0];
			
			String filePath = FilenameUtils.getFullPathNoEndSeparator(filename);
			String singleFile = FilenameUtils.getName(filename);
			
			System.out.println(filePath);
			System.out.println(singleFile);
			
			MovieOrSeries mos = MovieBuilder.identify(filePath, singleFile);

			System.out.println(String.format("Movie identified by :: %s", mos.getIdentifier()));
			
			if (mos.isSeries())
				System.out.println(mos.getSeries().toString());
			else
				System.out.println(mos.getMovie().toString());
			
		}
		else {
			System.out.println("No arguments");
		}
	}
}