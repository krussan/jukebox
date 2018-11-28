package se.qxx.jukebox.interfaces;

import java.util.List;

import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.subtitles.Language;
import se.qxx.jukebox.subtitles.SubFile;

public interface ISubFinder {
	List<SubFile> findSubtitles(MovieOrSeries mos, List<Language> languages);
	String getClassName();
}
