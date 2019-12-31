package se.qxx.jukebox.interfaces;

import se.qxx.jukebox.domain.MovieOrSeries;

import java.io.File;
import java.util.List;

public interface ISubFileUtilHelper {
	String createTempSubsPath(MovieOrSeries mos);
	List<String> findSubsInDirectory(File dir);
}
