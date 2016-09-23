package se.qxx.jukebox.subtitles;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;


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

//	protected String createSubsPath(Movie m) {
//		String filename = FilenameUtils.normalize(String.format("%s/%s"
//				, Settings.get().getSubFinders().getSubsPath()
//				, FilenameUtils.getBaseName(m.getFilename())));
//		
//		File f = new File(filename);
//		if (!f.exists())
//			f.mkdirs();
//		return filename;
//	}
	
//	/**
//	 * Return a temporary filename to download subtitles to.
//	 * @param filename The filename of the movie
//	 * @return
//	 */
//	public static String getTempSubsName(Movie m) {
//		String path = createTempSubsPath();
//        return String.format("%s/%s_%s", path, Thread.currentThread().getId(), filename);
//	}
	
	
	/**
	 * Rates a sub file (or a string) depending on the all categories in the Movie
	 * class
	 * 
	 * @param m 				 - The movie to compare against
	 * @param subFileDescription - The description of the subtitle file. Could be the same as the filename but without extension.
	 * @return Rating			 - A rating based on the Rating enumeration				
	 */
	protected Rating rateSub(Movie m, String subFileDescription) {
		MovieOrSeries mos = MovieBuilder.identify("", subFileDescription + ".dummy");
		

		//PartPattern moviePP = new PartPattern(FilenameUtils.getBaseName(m.getMedia(0).getFilename()));
		//PartPattern subPP = new PartPattern(subFileDescription);

		if (mos != null) {
			String subFilename = FilenameUtils.getBaseName(mos.getMedia().getFilename());
			for (Media md : m.getMediaList()) {
				String mediaFilename = FilenameUtils.getBaseName(md.getFilename());
				if (StringUtils.equalsIgnoreCase(mediaFilename, subFilename))
					return Rating.ExactMatch;
			}
		
			String group = m.getGroupName();
			String subGroup = mos.getGroupName();
			
			String subFormat = mos.getFormat();
			
			if (StringUtils.equalsIgnoreCase(subGroup, group) && !StringUtils.isEmpty(subGroup)) {
				if (StringUtils.equalsIgnoreCase(subFormat, m.getFormat()) && !StringUtils.isEmpty(subFormat))
					return Rating.PositiveMatch;
				else
					return Rating.ProbableMatch;
			}
		
		}
		
		return Rating.NotMatched;
	
	}	
	
	/**
	 * Returns a temporary path to download subtitles to
	 * @return
	 */
	public static String createTempSubsPath(Movie m) {
		String tempPath = 
			FilenameUtils.normalize(
				String.format("%s/temp/%s"
					, Settings.get().getSubFinders().getSubsPath()
					, m.getID()));

		File path = new File(tempPath);
		if (!path.exists())
			path.mkdirs();
		
		return tempPath;
	}	
	
}
