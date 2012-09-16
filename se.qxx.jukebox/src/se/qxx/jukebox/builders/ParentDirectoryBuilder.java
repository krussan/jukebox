package se.qxx.jukebox.builders;

import java.io.File;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.settings.Settings;

public class ParentDirectoryBuilder extends FilenameBuilder {

	@Override
	public Movie extractMovie(String filepath, String filename) {
		File path = new File(filepath); 

		Log.Info(String.format("ParentDirectoryBuilder path :: %s", filepath), LogType.FIND);
		// check if path exist and if that is under one of the base directory
		if (path.exists() && !isBasePath(filepath)) {
			// add .dummy as extension as this is removed by FilenameBuilder
			Movie m = super.extractMovie(path.getParent(), path.getName() + ".dummy");
			if (m != null) 
				return Movie.newBuilder(m).setIdentifier(Identifier.ParentDirectory).build();
		}

		return null;
	}
	
	private boolean isBasePath(String path) {
		if (path.length() >= 2 && (path.endsWith("/") || path.endsWith("\\")))
			path = path.substring(0, path.length() - 2);
		
		Log.Info(String.format("Path to match :: %s", path), LogType.FIND);
		for (Catalog c : Settings.get().getCatalogs().getCatalog()) {
			if (path.toLowerCase().equals(c.getPath().toLowerCase()))
				return true;
		}
		
		return false;
	}

}
