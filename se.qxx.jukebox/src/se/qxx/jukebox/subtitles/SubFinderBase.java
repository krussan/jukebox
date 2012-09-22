package se.qxx.jukebox.subtitles;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;

public abstract class SubFinderBase {

	private HashMap<String, String> settings = new HashMap<String, String>();

	public abstract List<SubFile> findSubtitles(Movie m, List<String> languages) throws java.io.IOException;

	public SubFinderBase(JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings subFinderSettings) {
		for (JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings.Setting setting : subFinderSettings.getSetting()) {
			this.settings.put(StringUtils.trim(setting.getKey()), StringUtils.trim(setting.getValue()));
		}
	}

	protected String getSetting(String key) {
		return this.settings.get(key);
	}

	protected String createSubsPath(Movie m) {
		String filename = String.format("%s/%s"
				, Settings.get().getSubFinders().getSubsPath()
				, Util.getFilenameWithoutExtension(m.getFilename()));
		
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
	public static String getTempSubsName(String filename) {
		String path = createTempSubsPath();
        return String.format("%s/%s_%s", path, Thread.currentThread().getId(), filename);
	}
	
	/**
	 * Returns a temporary path to download subtitles to
	 * @return
	 */
	public static String createTempSubsPath() {
		String tempPath = Settings.get().getSubFinders().getSubsPath() + "/temp";
		File path = new File(tempPath);
		if (!path.exists()) {
			path.mkdir();
		}
		
		return tempPath;
	}	
	
}
