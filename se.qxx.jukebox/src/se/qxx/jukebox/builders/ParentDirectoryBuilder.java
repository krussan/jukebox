package se.qxx.jukebox.builders;

import java.io.File;

import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.Settings;

public class ParentDirectoryBuilder extends FilenameBuilder {

	@Override
	public Movie extractMovie(String filepath, String filename) {
		File path = new File(filepath);
		
		// check if path exist and if that is under one of the base directory
		if (path.exists() && !isBasePath(filepath)) {
			Movie m = super.extractMovie(path.getParent(), path.getName());
			if (m != null) 
				return Movie.newBuilder(m).setIdentifier(Identifier.ParentDirectory).build();
		}

		return null;
	}
	
	private boolean isBasePath(String path) {
		if (path.length() >= 2 && (path.endsWith("/") || path.endsWith("\\")))
			path = path.substring(0, path.length() - 2);
		
		for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
			if (path.toLowerCase() == c.getPath().toLowerCase())
				return true;
		}
		
		return false;
	}

}
