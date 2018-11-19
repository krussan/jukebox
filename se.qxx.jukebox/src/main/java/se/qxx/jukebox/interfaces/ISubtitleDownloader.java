package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public interface ISubtitleDownloader {
	public void addMovie(Movie m);
	public void reenlistMovie(Movie m);
	public void reenlistEpisode(Episode ep);
	public void addEpisode(Episode episode);
	public Runnable getRunnable();
}
