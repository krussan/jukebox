package se.qxx.jukebox.subtitles;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.ISubFileUtilHelper;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMovieBuilderFactory;
import se.qxx.jukebox.interfaces.IRandomWaiter;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubFileDownloaderHelper;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.tools.WebResult;

@Singleton
public class SubFileDownloaderHelper implements ISubFileDownloaderHelper {
	
	private ISettings settings;
	private boolean isRunning = true;

	private final int MAX_SUBS_DOWNLOADED = 15;
	private final int MIN_WAIT_SECONDS = 20;
	private final int MAX_WAIT_SECONDS = 30;

	private IWebRetriever webRetriever;
	private IMovieBuilderFactory movieBuilderFactory;
	private IJukeboxLogger log;
	private ISubFileUtilHelper fileUtilHelper;
	
	private Map<String, Map<String, String>> subSettings = new HashMap<String, Map<String, String>>();
	private IRandomWaiter waiter;
	
	@Inject
	public SubFileDownloaderHelper(ISettings settings, 
			IWebRetriever webRetriever,
			IMovieBuilderFactory movieBuilderFactory,
			LoggerFactory loggerFactory,
			IRandomWaiter waiter,
			ISubFileUtilHelper fileUtilHelper) {
		
		this.setFileUtilHelper(fileUtilHelper);
		this.setWaiter(waiter);
		this.setWebRetriever(webRetriever);
		this.setSettings(settings);
		this.setMovieBuilderFactory(movieBuilderFactory);
		this.setLog(loggerFactory.create(LogType.SUBS));
	}

	public ISubFileUtilHelper getFileUtilHelper() {
		return fileUtilHelper;
	}

	public void setFileUtilHelper(ISubFileUtilHelper fileUtilHelper) {
		this.fileUtilHelper = fileUtilHelper;
	}

	@Override
	public IRandomWaiter getWaiter() {
		return waiter;
	}

	public void setWaiter(IRandomWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public Map<String, Map<String, String>> getSubSettings() {
		return subSettings;
	}

	public void setSubSettings(Map<String, Map<String, String>> subSettings) {
		this.subSettings = subSettings;
	}

	public IMovieBuilderFactory getMovieBuilderFactory() {
		return movieBuilderFactory;
	}

	public void setMovieBuilderFactory(IMovieBuilderFactory movieBuilderFactory) {
		this.movieBuilderFactory = movieBuilderFactory;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public IWebRetriever getWebRetriever() {
		return webRetriever;
	}

	public void setWebRetriever(IWebRetriever webRetriever) {
		this.webRetriever = webRetriever;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	@Override
	public List<SubFile> downloadSubs(String subFileClass, MovieOrSeries mos, List<SubFile> listSubs) {
		List<SubFile> files = new ArrayList<SubFile>();
		
		//Store downloaded files in temporary storage
		//SubtitleDownloader will move them to correct path
		String tempSubPath = this.getFileUtilHelper().createTempSubsPath(mos);
		
		int sizeCollection = listSubs.size();
		int c = 1;
		
		for (SubFile sf : listSubs) {
			try {
				SubFile sfi = downloadSubFile(subFileClass, sf, tempSubPath, sizeCollection, c);
				if (sfi != null)
					files.add(sfi);
				
				c++;
				
				if (c > MAX_SUBS_DOWNLOADED)
					break;
				
			}
			catch (IOException e) {
				this.getLog().Error(String.format("%s :: Error when downloading subtitle :: %s", subFileClass, sf.getFile().getName()), e);
			}
			
			if (listSubs.size() > 1) {
				this.getWaiter().sleep(MIN_WAIT_SECONDS, MAX_WAIT_SECONDS);
			}

			if (!this.isRunning())
				return new ArrayList<SubFile>();
		}
		
		return files;
		
	}

	private SubFile downloadSubFile(String subFileClass, SubFile sf, String tempSubPath, int sizeCollection, int c)
			throws IOException {
		File file = this.getWebRetriever().getWebFile(sf.getUrl(), tempSubPath);

		if (file != null) {
			sf.setFile(file);
			
			this.getLog().Debug(String.format("%s :: [%s/%s] :: File downloaded: %s"
					, subFileClass
					, c
					, sizeCollection
					, sf.getFile().getName())
				);
			
			return sf;

		}
		return null;
	}

	
	@Override
	public void exit() {
		this.setRunning(false);
	}

	@Override
	public boolean containsMatch(List<SubFile> subs) {
		for (SubFile sf : subs) {
			Rating r = sf.getRating();
			if (r == Rating.SubsExist || r == Rating.ExactMatch || r == Rating.PositiveMatch)
				return true;
		}
		
		return false;
	}

	@Override
	public String performSearch(String url) {
		String result = StringUtils.EMPTY;
		try {
			WebResult webResult = this.getWebRetriever().getWebResult(url);
			
			result = webResult.getResult();
	
			// replace newline
			result = result.replace("\r", "");
			result = result.replace("\n", "");
		}
		catch (IOException e) {
			this.getLog().Error("Error while making web call", e);
		}
		
		return result;	
	}

	@Override
	public String postSearch(String url, String query) {
		String result = StringUtils.EMPTY;
		try {
			WebResult webResult = this.getWebRetriever().postWebResult(url, query);

			result = webResult.getResult();

			// replace newline
			result = result.replace("\r", "");
			result = result.replace("\n", "");
		}
		catch (IOException e) {
			this.getLog().Error("Error while making web call", e);
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
	@Override
	public List<SubFile> collectSubFiles(
			String className,
			List<Language> languages,
			MovieOrSeries mos, 
			String webResult, 
			String pattern, 
			int urlGroup, 
			int nameGroup, 
			int languageGroup) {

		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		Matcher matcher = p.matcher(webResult);
		
		List<SubFile> listSubs = new ArrayList<>();
		
		this.getLog().Debug(String.format("%s :: Finding subtitles for %s", className, mos.getMedia().getFilename()));
		
		while (matcher.find()) {
			String urlString = matcher.group(urlGroup).trim();
			String description = matcher.group(nameGroup).trim();
			String matchLanguage = matcher.group(languageGroup).trim();

			Optional<Language> lang = getLanguageMatch(matchLanguage, languages);
			if (languageGroup == 0 || lang.isPresent()) {
				// remove duplicate links and descriptions matching whole season
				if (!linksContains(listSubs, urlString) && !matchesWholeSeason(mos.isSeries(), description)) {
					SubFile sf = new SubFile(
							urlString, 
							description,  
							languageGroup == 0 ? Language.Unknown : lang.get());
					
					Rating r = this.rateSub(mos, description);
					sf.setRating(r);
					this.getLog().Debug(String.format("%s :: Sub with description %s rated as %s", className, description, r.toString()));
					
					listSubs.add(sf);				
				}
			}
				
		}
		
		if (listSubs.size()==0)
			this.getLog().Debug(String.format("%s :: No subs found", className));

		return filterResult(className, listSubs);
	}

	private Optional<Language> getLanguageMatch(String matchLanguage, List<Language> languages) {
		return languages.stream()
			.filter(x -> StringUtils.equalsIgnoreCase(matchLanguage, x.toString()))
			.findFirst();
		
	}

	private boolean matchesWholeSeason(boolean isEpisode, String description) {
		if (!isEpisode)
			return false;
		
		Pattern pp = Pattern.compile("(S[0-9]*)(?!E[0-9]*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		Matcher m = pp.matcher(description);

		boolean containsSeasonString = StringUtils.containsIgnoreCase(description, "season");
		
		return m.matches() || containsSeasonString;
		
	}

	private boolean linksContains(List<SubFile> files, String url) {
		for (SubFile f : files) {
			if (StringUtils.equalsIgnoreCase(f.getUrl(), url))
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
	private List<SubFile> filterResult(String className, List<SubFile> listSubs) {
		Collections.sort(listSubs);
		
		List<SubFile> result = new ArrayList<SubFile>();
		
		for(SubFile sf : listSubs) {
			if (!StringUtils.containsIgnoreCase("season", sf.getDescription())) {
				result.add(sf);
			}
			else {
				this.getLog().Debug(String.format("Ignoring file :: %s  - as this appear to be a full season package", 
						sf.getFile().getName()));
			}
			
			if (sf.getRating() == Rating.ExactMatch || sf.getRating() == Rating.PositiveMatch)  {
				this.getLog().Debug(String.format("%s :: Exact or positive match found. exiting...", 
						className));
				break;
			}

		}
		
		return result;
	}

	/**
	 * Rates a sub file (or a string) depending on the all categories in the Movie
	 * class
	 * 
	 * @param mos 				 - The movie to compare against
	 * @param subFileDescription - The description of the subtitle file. Could be the same as the filename but without extension.
	 * @return Rating			 - A rating based on the Rating enumeration				
	 */
	@Override
	public Rating rateSub(MovieOrSeries mos, String subFileDescription) {
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

	@Override
	public String getSetting(SubFinder finder, String setting) {
		return finder.getSubFinderSettings().getSetting().stream().filter(x -> StringUtils.equalsIgnoreCase(x.getKey(), setting)).findFirst().get().getValue();
	}

}
