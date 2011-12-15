package se.qxx.jukebox.subtitles;

import java.io.File;
import java.util.List;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings;

public interface ISubtitleFinder {
	public List<SubFile>  findSubtitles(Movie m, List<String> languages, String subsPath, JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings subFinderSettings) throws java.io.IOException;
}
