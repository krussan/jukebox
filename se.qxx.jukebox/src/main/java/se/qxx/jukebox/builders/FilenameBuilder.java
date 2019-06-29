package se.qxx.jukebox.builders;

import se.qxx.jukebox.builders.exceptions.DeprecatedBuilderException;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;

public class FilenameBuilder extends MovieBuilder {

	public FilenameBuilder(ISettings settings, IJukeboxLogger log) {
		super(settings, log);
	}

	@Override
	public MovieOrSeries extract(String filepath, String filename) throws DeprecatedBuilderException {
		throw new DeprecatedBuilderException("Filename builder has been deprecated. Please use ParserBuilder");
	}
	
}
