package se.qxx.jukebox.interfaces;

import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.subtitles.Language;
import se.qxx.jukebox.subtitles.SubFile;

public interface ISubtitleDownloader {
	void addMovie(Movie m);
	void reenlistMovie(Movie m);
	void reenlistEpisode(Episode ep);
	void addEpisode(Episode episode);
	Runnable getRunnable();
	List<SubFile> checkMovieDirForSubs(Media md);
	List<Language> getPreferredLanguages();
	Movie clearSubs(Movie m);
	Episode clearSubs(Episode ep);
}
