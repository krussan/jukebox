package se.qxx.jukebox.subtitles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.WebResult;
import se.qxx.jukebox.tools.WebRetriever;


public abstract class SubFinderBase {
	private String className;
	private Language language;
	private int minWaitSeconds = 20;
	private int maxWaitSeconds = 30;
	
	private final int MAX_SUBS_DOWNLOADED = 15;
	
	protected String getClassName() {
		return className;
	}

	protected void setClassName(String className) {
		this.className = className;
	}
	
	protected Language getLanguage() {
		return language;
	}

	protected void setLanguage(Language language) {
		this.language = language;
	}
	
	protected int getMinWaitSeconds() {
		return minWaitSeconds;
	}

	protected void setMinWaitSeconds(int minWaitSeconds) {
		this.minWaitSeconds = minWaitSeconds;
	}

	protected int getMaxWaitSeconds() {
		return maxWaitSeconds;
	}

	protected void setMaxWaitSeconds(int maxWaitSeconds) {
		this.maxWaitSeconds = maxWaitSeconds;
	}	

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

	protected List<SubFile> downloadSubs(Movie m, List<SubFile> listSubs) {
		List<SubFile> files = new ArrayList<SubFile>();
		
		//Store downloaded files in temporary storage
		//SubtitleDownloader will move them to correct path
		String tempSubPath = createTempSubsPath(m);
		
		int sizeCollection = listSubs.size();
		int c = 1;
		
		for (SubFile sf : listSubs) {
			try {
				File file = WebRetriever.getWebFile(sf.getUrl(), tempSubPath);
				if (file != null) {
					sf.setFile(file);
					
					files.add(sf);
		
					Log.Debug(String.format("%s :: [%s/%s] :: File downloaded: %s"
							, this.getClassName()
							, c
							, sizeCollection
							, sf.getFile().getName())
						, Log.LogType.SUBS);
		
					if (sf.getRating() == Rating.ExactMatch || sf.getRating() == Rating.PositiveMatch)  {
						Log.Debug(String.format("%s :: Exact or positive match found. exiting...", 
								this.getClassName()), 
								Log.LogType.SUBS);
						break;
					}
				}
				
				c++;
				
				if (c > MAX_SUBS_DOWNLOADED)
					break;
				
			}
			catch (IOException e) {
				Log.Debug(String.format("%s :: Error when downloading subtitle :: %s", this.getClassName(), sf.getFile().getName()), LogType.SUBS);
			}
			
			if (listSubs.size() > 1) {
				try {
					Random r = new Random();
					int n = r.nextInt((this.getMaxWaitSeconds() - this.getMinWaitSeconds()) * 1000 + 1) + this.getMinWaitSeconds() * 1000;
					
					// sleep randomly to avoid detection (from 10 sec to 30 sec)
					Thread.sleep(n);
				} catch (InterruptedException e) {
					Log.Error(String.format("Subtitle downloader interrupted", this.getClassName()), Log.LogType.SUBS, e);
				}
			}
			
		}
		
		return files;
		
	}
	
	
	protected String performSearch(String url) {
		String result = StringUtils.EMPTY;
		try {
			WebResult webResult = WebRetriever.getWebResult(url);
			
			result = webResult.getResult();
	
			// replace newline
			result = result.replace("\r", "");
			result = result.replace("\n", "");
		}
		catch (IOException e) {
			Log.Error("Error while making web call", LogType.SUBS, e);
		}
		
		return result;	
	}
	
	/***
	 * Collects download links from a webresult.
	 * Also rates the name of the sub in accordance with the name of the media file
	 * - if we found an exact match get that one
	 * - if we found a postive match get that one
	 * - otherwise get all up to a cut-off
	 * 
	 * Regex for the pattern to extract the information needs to be supplied accompanied
	 * by the groups for download link and name.
	 * 
	 * A language group can also be defined. If set to zero this one will be ignored.
	 * If the language does _not_ match the language set by the sub-finder implementation then
	 * the download link is ignored.
	 * 
	 * @param m
	 * @param webResult
	 * @param pattern
	 * @param urlGroup
	 * @param nameGroup
	 * @param languageGroup
	 * @return
	 */
	protected List<SubFile> collectSubFiles(Movie m, String webResult, String pattern, int urlGroup, int nameGroup, int languageGroup) {
		//String pattern = this.getSetting(SETTING_PATTERN);
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		Matcher matcher = p.matcher(webResult);
		
		List<SubFile> listSubs = new ArrayList<SubFile>();
		
		Log.Debug(String.format("%s :: Finding subtitles for %s", this.getClassName(), m.getMedia(0).getFilename()), Log.LogType.SUBS);
		
		while (matcher.find()) {
			String urlString = matcher.group(urlGroup).trim();
			String description = matcher.group(nameGroup).trim();
			String language = matcher.group(languageGroup).trim();

			if (languageGroup == 0 || StringUtils.equalsIgnoreCase(language, this.getLanguage().toString())) {
				SubFile sf = new SubFile(urlString, description, this.getLanguage());
				Rating r = this.rateSub(m, description);
				sf.setRating(r);
				Log.Debug(String.format("%s :: Sub with description %s rated as %s", this.getClassName(), description, r.toString()), Log.LogType.SUBS);
				
				listSubs.add(sf);				
			}
			else {
				Log.Debug(String.format("%s :: Skipping subtitle with language %s", this.getClassName(), language), LogType.SUBS);
			}
				
		}
		
		if (listSubs.size()==0)
			Log.Debug(String.format("%s :: No subs found", this.getClassName()), Log.LogType.SUBS);

		Collections.sort(listSubs);
		
		return listSubs;
	}

	
	
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
