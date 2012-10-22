package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.builders.FilenameBuilder;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;

public class TestMovieBuilder {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			Settings.readSettings();
			ImdbSettings.readSettings();		
			
			List<String> extensions = getExtensions();
			
			String filename = args[0];
			
			String filePath = FilenameUtils.getFullPathNoEndSeparator(filename);
			String singleFile = FilenameUtils.getName(filename);
			
			System.out.println(filePath);
			System.out.println(singleFile);
			
			Movie m = MovieBuilder.identifyMovie(filePath, singleFile);

			System.out.println(String.format("Movie identified by :: %s", m.getIdentifier()));
		}
		else {
			System.out.println("No arguments");
		}
	}
	
	private static List<String> getExtensions() {
		List<String> list = new ArrayList<String>();
		
		for (JukeboxListenerSettings.Catalogs.Catalog c : Settings.get().getCatalogs().getCatalog()) {
			for (JukeboxListenerSettings.Catalogs.Catalog.Extensions.Extension e : c.getExtensions().getExtension()) {
				if (!list.contains(e.getValue()))
					list.add(e.getValue());
			}
		}
		
		return list;
	}
}
