package se.qxx.jukebox.interfaces;

import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.subtitles.Language;
import se.qxx.jukebox.subtitles.SubFile;

public interface ISubFileDownloaderHelper {
	List<SubFile> downloadSubs(String subFileClass, MovieOrSeries mos, List<SubFile> listSubs);
	void exit();
	String createTempSubsPath(MovieOrSeries mos);
	boolean containsMatch(List<SubFile> subs);
	String getSetting(String className, String setting);
	String performSearch(String url);
	List<SubFile> collectSubFiles(String className, List<Language> language, MovieOrSeries mos, String webResult, String pattern, int urlGroup, int nameGroup,
			int languageGroup);
	Rating rateSub(MovieOrSeries mos, String subFileDescription);
	IJukeboxLogger getLog();
}
