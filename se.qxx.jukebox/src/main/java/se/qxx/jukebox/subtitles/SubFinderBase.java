package se.qxx.jukebox.subtitles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubFileDownloader;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.WebResult;
import se.qxx.jukebox.tools.WebRetriever;

public class SubFinderBase implements ISubFinder {
	private String className;
	private Language language;
	
	private IWebRetriever webRetriever;
	private IMovieBuilderFactory movieBuilderFactory;
	private ISubFileDownloader subFileDownloader;
	private ISettings settings;
	

	private Map<String, String> subSettings = new HashMap<>();
	
	public SubFinderBase(String className,
			ISettings settings, 
			IWebRetriever webRetriever,
			IMovieBuilderFactory movieBuilderFactory,
			ISubFileDownloader subFileDownloader) {

		this.setClassName(className);
		this.setSettings(settings);
		this.setSubFileDownloader(subFileDownloader);
		this.setWebRetriever(webRetriever);
		this.setMovieBuilderFactory(movieBuilderFactory);
		initSubSettings();
	}

	private void initSubSettings() {
		Optional<SubFinder> subFinderSettings = this.getSettings().getSettings().getSubFinders().getSubFinder().stream().filter(x -> StringUtils.equalsIgnoreCase(x.getClazz(), this.getClassName())).findFirst();
		
		if (subFinderSettings.isPresent()) {
			subFinderSettings.get().getSubFinderSettings().getSetting().forEach(x -> this.subSettings.put(x.getKey(), x.getValue()));
		}

	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	public ISubFileDownloader getSubFileDownloader() {
		return subFileDownloader;
	}

	public void setSubFileDownloader(ISubFileDownloader subFileDownloader) {
		this.subFileDownloader = subFileDownloader;
	}

	public IMovieBuilderFactory getMovieBuilderFactory() {
		return movieBuilderFactory;
	}

	public void setMovieBuilderFactory(IMovieBuilderFactory movieBuilderFactory) {
		this.movieBuilderFactory = movieBuilderFactory;
	}

	public IWebRetriever getWebRetriever() {
		return webRetriever;
	}

	public void setWebRetriever(IWebRetriever webRetriever) {
		this.webRetriever = webRetriever;
	}


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
		
	public abstract List<SubFile> findSubtitles(MovieOrSeries mos, List<String> languages);

	protected String getSetting(String key) {
		return this.subSettings.get(key);
	}
	
	protected String performSearch(String url) {
		String result = StringUtils.EMPTY;
		try {
			WebResult webResult = this.getWebRetriever().getWebResult(url);
			
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
		MovieOrSeries subMos = this.getMovieBuilderFactory()
				.identify("", subFileDescription + ".dummy");
		
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

}
