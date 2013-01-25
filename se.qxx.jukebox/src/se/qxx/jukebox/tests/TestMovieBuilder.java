package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.builders.FilenameBuilder;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;

public class TestMovieBuilder {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			Settings.initialize();	
			
			String filename = args[0];
			
			String filePath = FilenameUtils.getFullPathNoEndSeparator(filename);
			String singleFile = FilenameUtils.getName(filename);
			
			System.out.println(filePath);
			System.out.println(singleFile);
			
			Movie m = MovieBuilder.identifyMovie(filePath, singleFile);

			System.out.println(String.format("Movie identified by :: %s", m.getIdentifier()));
			
			System.out.println(m.toString());
		}
		else {
			System.out.println("No arguments");
		}
	}
}
