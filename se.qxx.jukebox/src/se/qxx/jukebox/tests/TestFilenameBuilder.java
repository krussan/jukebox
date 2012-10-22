package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.builders.FilenameBuilder;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;

public class TestFilenameBuilder {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			Settings.readSettings();
			ImdbSettings.readSettings();		
			
			List<String> extensions = getExtensions();
			
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
			fb.extractMovie(filePath, singleFile);
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
