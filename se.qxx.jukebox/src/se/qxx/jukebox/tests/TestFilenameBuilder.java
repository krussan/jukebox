package se.qxx.jukebox.tests;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.builders.FilenameBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;

public class TestFilenameBuilder {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			Settings.readSettings();
			ImdbSettings.readSettings();		
			
			List<String> extensions = Util.getExtensions();
			
			String filename = args[0];

			boolean hasSuffix = false;
			for (String ext : extensions) {
				if (StringUtils.endsWithIgnoreCase(filename, ext)) {
					hasSuffix = true;
					break;
				}
			}
			
			if (!hasSuffix)
				filename += ".dummy";
			
			String filePath = FilenameUtils.getPath(filename);
			String singleFile = FilenameUtils.getName(filename);
			
			System.out.println(filePath);
			System.out.println(singleFile);
			
			FilenameBuilder fb = new FilenameBuilder();
			Movie m = fb.extractMovie(filePath, singleFile);
			
			System.out.println(m.toString());
		}
		else {
			System.out.println("No arguments");
		}
	}
	

}
