package se.qxx.jukebox.subtitles;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.builders.FilenameBuilder;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.subtitles.SubFile.Rating;

public abstract class SubFinderBase {

	private HashMap<String, String> settings = new HashMap<String, String>();

	public abstract List<SubFile> findSubtitles(Movie m, List<String> languages);

	public SubFinderBase(JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings subFinderSettings) {
		for (JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings.Setting setting : subFinderSettings.getSetting()) {
			this.settings.put(StringUtils.trim(setting.getKey()), StringUtils.trim(setting.getValue()));
		}
	}

	protected String getSetting(String key) {
		return this.settings.get(key);
	}

	protected String createSubsPath(Movie m) {
		String filename = FilenameUtils.normalize(String.format("%s/%s"
				, Settings.get().getSubFinders().getSubsPath()
				, FilenameUtils.getBaseName(m.getFilename())));
		
		File f = new File(filename);
		if (!f.exists())
			f.mkdirs();
		return filename;
	}
	
	/**
	 * Return a temporary filename to download subtitles to.
	 * @param filename The filename of the movie
	 * @return
	 */
//	public static String getTempSubsName(Movie m) {
//		String path = createTempSubsPath();
//        return String.format("%s/%s_%s", path, Thread.currentThread().getId(), filename);
//	}
	
	/**
	 * Returns a temporary path to download subtitles to
	 * @return
	 */
	public static String createTempSubsPath(Movie m) {
		String tempPath = FilenameUtils.normalize(String.format("%s/temp/%s", Settings.get().getSubFinders().getSubsPath(), FilenameUtils.getBaseName(m.getFilename())));
		File path = new File(tempPath);
		if (!path.exists())
			path.mkdirs();
		
		return tempPath;
	}	
	
	/**
	 * Rates a sub file (or a string) depending on the all categories in the Movie
	 * class
	 * 
	 * @param m 				- The movie to compare against
	 * @param subFilename		- the filename or string of the subfile
	 * @return Rating			- A rating based on the Rating enumeration				
	 */
	protected Rating rateSub(Movie m, String subFilename) {
		FilenameBuilder b = new FilenameBuilder();
		Movie subMovie = b.extractMovie("", subFilename);
		Rating r = Rating.NotMatched;
		
		if (subMovie != null) {
			//Check if filenames match exactly
			String filenameWithoutExtension = FilenameUtils.getBaseName(m.getFilename());

			if (StringUtils.equalsIgnoreCase(filenameWithoutExtension, subFilename))
				return Rating.ExactMatch;
			
			String group = m.getGroup();
			String subGroup = subMovie.getGroup();
			
			if (subGroup != null) {
				if (StringUtils.equalsIgnoreCase(subGroup, group) && !StringUtils.isEmpty(subGroup)) {
					if (StringUtils.equalsIgnoreCase(subMovie.getFormat(), m.getFormat()) && !StringUtils.isEmpty(subMovie.getFormat()))
						r = Rating.PositiveMatch;
					else
						r = Rating.ProbableMatch;
				}
			}
		}
		return r;
	
	}	
}