package se.qxx.jukebox.builders;

import se.qxx.jukebox.builders.exceptions.DeprecatedBuilderException;
import se.qxx.jukebox.domain.MovieOrSeries;

public class FilenameBuilder extends MovieBuilder {

	@Override
	public MovieOrSeries extract(String filepath, String filename) throws DeprecatedBuilderException {
		throw new DeprecatedBuilderException("Filename builder has been deprecated. Please use ParserBuilder");
	}
	
}
