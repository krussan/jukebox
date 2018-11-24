package se.qxx.jukebox.interfaces;

import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.subtitles.SubFile;

public interface ISubtitleDownloader {
	public void addMovie(Movie m);
	public void reenlistMovie(Movie m);
	public void reenlistEpisode(Episode ep);
	public void addEpisode(Episode episode);
	public Runnable getRunnable();
	public List<SubFile> checkMovieDirForSubs(Media md);
}
