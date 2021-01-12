package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.watcher.FileRepresentation;

public interface IMovieIdentifier {

	void addFile(FileRepresentation f);

	/**
	 * Gets IMDB and metadata information from media and adds it to the database.
	 * 
	 * @param m
	 * @param newMedia
	 */
	Movie getMovieInfo(Movie movie);

	/**
	 * Gets IMDB and metadata information from media and adds it to the database.
	 * 
	 * @param m
	 * @param newMedia
	 */
	Series getSeriesInfo(Series series, int season, int episode, Media media);

	Runnable getRunnable();

}
