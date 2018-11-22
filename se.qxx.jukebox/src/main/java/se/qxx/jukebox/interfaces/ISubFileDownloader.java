package se.qxx.jukebox.interfaces;

import java.util.List;

import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.subtitles.SubFile;

public interface ISubFileDownloader {
	List<SubFile> downloadSubs(String subFileClass, MovieOrSeries mos, List<SubFile> listSubs);
	void exit();
}
