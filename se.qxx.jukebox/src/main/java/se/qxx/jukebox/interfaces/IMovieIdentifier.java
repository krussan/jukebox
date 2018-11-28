package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.watcher.FileRepresentation;

public interface IMovieIdentifier {

	public void addFile(FileRepresentation f);

	/**
	 * Gets IMDB and metadata information from media and adds it to the database.
	 * 
	 * @param m
	 * @param newMedia
	 */
	public Movie getMovieInfo(Movie movie, Media media);

	/**
	 * Gets IMDB and metadata information from media and adds it to the database.
	 * 
	 * @param m
	 * @param newMedia
	 */
	public Series getSeriesInfo(Series series, int season, int episode, Media media);

	public Runnable getRunnable();
	
}