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
	private boolean isRunning = true;
	
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
	
	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}	

	private HashMap<String, String> settings = new HashMap<String, String>();

	public abstract List<SubFile> findSubtitles(MovieOrSeries mos, List<String> languages);
	
	public SubFinderBase(JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings subFinderSettings) {
		for (JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings.Setting setting : subFinderSettings.getSetting()) {
			this.settings.put(StringUtils.trim(setting.getKey()), StringUtils.trim(setting.getValue()));
		}
	}

	protected String getSetting(String key) {
		return this.settings.get(key);
	}

	protected List<SubFile> downloadSubs(MovieOrSeries mos, List<SubFile> listSubs) {
		List<SubFile> files = new ArrayList<SubFile>();
		
		//Store downloaded files in temporary storage
		//SubtitleDownloader will move them to correct path
		String tempSubPath = createTempSubsPath(mos);
		
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
		
				}
				
				c++;
				
				if (c > MAX_SUBS_DOWNLOADED)
					break;
				
			}
			catch (IOException e) {
				Log.Error(String.format("%s :: Error when downloading subtitle :: %s", this.getClassName(), sf.getFile().getName()), LogType.SUBS, e);
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

			if (!this.isRunning)
				return new ArrayList<SubFile>();
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
	protected List<SubFile> collectSubFiles(MovieOrSeries mos, String webResult, String pattern, int urlGroup, int nameGroup, int languageGroup) {
		//String pattern = this.getSetting(SETTING_PATTERN);
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		Matcher matcher = p.matcher(webResult);
		
		List<SubFile> listSubs = new ArrayList<SubFile>();
		
		Log.Debug(String.format("%s :: Finding subtitles for %s", this.getClassName(), mos.getMedia().getFilename()), Log.LogType.SUBS);
		
		while (matcher.find()) {
			String urlString = matcher.group(urlGroup).trim();
			String description = matcher.group(nameGroup).trim();
			String language = matcher.group(languageGroup).trim();

			
			if (languageGroup == 0 || StringUtils.equalsIgnoreCase(language, this.getLanguage().toString())) {
				// remove duplicate links and descriptions matching whole season
				if (!linksContains(listSubs, urlString) && !matchesWholeSeason(mos.isSeries(), description)) {
					SubFile sf = new SubFile(urlString, description, this.getLanguage());
					Rating r = this.rateSub(mos, description);
					sf.setRating(r);
					Log.Debug(String.format("%s :: Sub with description %s rated as %s", this.getClassName(), description, r.toString()), Log.LogType.SUBS);
					
					listSubs.add(sf);				
				}
			}
				
		}
		
		if (listSubs.size()==0)
			Log.Debug(String.format("%s :: No subs found", this.getClassName()), Log.LogType.SUBS);

		return filterResult(listSubs);
	}
	
	private boolean matchesWholeSeason(boolean isEpisode, String description) {
		if (!isEpisode)
			return false;
		
		Pattern pp = Pattern.compile("(S[0-9]*)(?!E[0-9]*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		Matcher m = pp.matcher(description);

		boolean containsSeasonString = StringUtils.containsIgnoreCase("season", description);
		
		return m.matches() || containsSeasonString;
		
	}

	private boolean linksContains(List<SubFile> files, String url) {
		for (SubFile f : files) {
			if (StringUtils.equalsIgnoreCase(f.getUrl(), url))
				return true;
		}
		
		return false;
	}

	
	protected boolean containsMatch(List<SubFile> subs) {
		for (SubFile sf : subs) {
			Rating r = sf.getRating();
			if (r == Rating.SubsExist || r == Rating.ExactMatch || r == Rating.PositiveMatch)
				return true;
		}
		
		return false;
	}
	

	/***
	 * Filters a list of subfiles down to exact match or positive match if found.
	 * Otherwise return all subs 
	 * @param listSubs
	 * @return
	 */
	private List<SubFile> filterResult(List<SubFile> listSubs) {
		Collections.sort(listSubs);
		
		List<SubFile> result = new ArrayList<SubFile>();
		
		for(SubFile sf : listSubs) {
			if (!StringUtils.containsIgnoreCase("season", sf.getDescription())) {
				result.add(sf);
			}
			else {
				Log.Debug(String.format("Ignoring file :: %s  - as this appear to be a full season package", 
						sf.getFile().getName()), 
						Log.LogType.SUBS);
			}
			
			if (sf.getRating() == Rating.ExactMatch || sf.getRating() == Rating.PositiveMatch)  {
				Log.Debug(String.format("%s :: Exact or positive match found. exiting...", 
						this.getClassName()), 
						Log.LogType.SUBS);
				break;
			}

		}
		
		return result;
	}

	/**
	 * Rates a sub file (or a string) depending on the all categories in the Movie
	 * class
	 * 
	 * @param m 				 - The movie to compare against
	 * @param subFileDescription - The description of the subtitle file. Could be the same as the filename but without extension.
	 * @return Rating			 - A rating based on the Rating enumeration				
	 */
	protected Rating rateSub(MovieOrSeries mos, String subFileDescription) {
		MovieOrSeries subMos = MovieBuilder.identify("", subFileDescription + ".dummy");
		
		if (subMos != null) {
			String subFilename = FilenameUtils.getBaseName(subMos.getMedia().getFilename());
			String mediaFilename = FilenameUtils.getBaseName(mos.getMedia().getFilename());
			
			if (StringUtils.equalsIgnoreCase(mediaFilename, subFilename))
				return Rating.ExactMatch;
			
		
			String group = mos.getGroupName();
			String subGroup = subMos.getGroupName();
			String subFormat = subMos.getFormat();
			
			if (StringUtils.equalsIgnoreCase(subGroup, group) && !StringUtils.isEmpty(subGroup)) {
				if (StringUtils.equalsIgnoreCase(subFormat, mos.getFormat()) && !StringUtils.isEmpty(subFormat))
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
	public static String createTempSubsPath(MovieOrSeries mos) {
		String tempPath = 
			FilenameUtils.normalize(
				String.format("%s/temp/%s"
					, Settings.get().getSubFinders().getSubsPath()
					, mos.getID()));

		File path = new File(tempPath);
		if (!path.exists())
			path.mkdirs();
		
		return tempPath;
	}

	public void exit() {
		this.setRunning(false);
	}

}
