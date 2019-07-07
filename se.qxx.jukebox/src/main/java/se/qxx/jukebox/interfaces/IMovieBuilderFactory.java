package se.qxx.jukebox.interfaces;

import java.util.List;

import se.qxx.jukebox.domain.MovieOrSeries;

public interface IMovieBuilderFactory {
	/**
	 * Execute all enabled builders, perform rating and return the one with the best
	 * match.
	 * 
	 * @param filepath
	 * @param filename
	 * @return
	 */
	MovieOrSeries identify(String filepath, String filename);

	/**
	 * Builds the movie from the first proposal in the list
	 * 
	 * @param filepath
	 * @param filename
	 * @param proposals
	 * @param imdbUrl
	 * @param part
	 * @return The movie
	 */
	MovieOrSeries build(String filepath, String filename, List<MovieOrSeries> proposals);

}
