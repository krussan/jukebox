package se.qxx.jukebox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.Imdb;
import se.qxx.jukebox.settings.imdb.Imdb.EpisodePatterns.EpisodePattern;
import se.qxx.jukebox.settings.imdb.Imdb.SearchPatterns.SearchResultPattern;
import se.qxx.jukebox.settings.imdb.SearchPatternComparer;

import com.google.protobuf.ByteString;

public class IMDBFinder {
	private static long nextSearch = 0;
	
	public synchronized static Movie Get(Movie m) throws IOException, NumberFormatException, ParseException {
		Log.Debug(String.format("Starting search on title :: %s (%s)", m.getTitle(), m.getYear()), LogType.IMDB);
		String imdbUrl = m.getImdbUrl();
 
		IMDBRecord rec = null;
		if (StringUtils.isEmpty(imdbUrl) || urlIsBlacklisted(imdbUrl, m)) {
			rec = Search(m.getTitle(), Settings.imdb().getSearchUrl()); 
		}
		else {
			Log.Debug(String.format("IMDB url found."), LogType.IMDB);
			
			rec = IMDBRecord.get(imdbUrl);
		}
		
		return extractMovieInfo(m, rec);
	}
	
	public synchronized static Series Get(Series series, int season, int episode) throws IOException, NumberFormatException, ParseException {
		Log.Debug(String.format("Starting search on series title :: %s (%s)", series.getTitle(), series.getYear()), LogType.IMDB);
		String imdbUrl = series.getImdbUrl();
 
		if (StringUtils.isEmpty(imdbUrl)) {	
			return Search(series, season, episode, Settings.imdb().getSearchUrl());
		}
		else {
			Log.Debug(String.format("IMDB url found."), LogType.IMDB);
			
			IMDBRecord rec = IMDBRecord.get(imdbUrl);
			return extractMovieInfo(m, rec);
		}
	}
	

	/**
	 * Checks if an IMDB url is among the blacklisted url:s
	 * @param imdbUrl		 The Url to check
	 * @param blacklistedIDs The list of blacklisted IMDB id's
	 * @return
	 */
	private static boolean urlIsBlacklisted(String imdbUrl, Movie m) {
		String imdbid = Util.getImdbIdFromUrl(imdbUrl);
		if (!StringUtils.isEmpty(imdbid)) {	
			for (String entry : m.getBlacklistList()) {	
				if (StringUtils.equalsIgnoreCase(imdbid, entry)) {
					return true;
				}
			}
		}
		
		return false;
	}

	private synchronized static IMDBRecord Search(String searchString, String searchUrl) throws IOException, NumberFormatException, ParseException {
		long currentTimeStamp = Util.getCurrentTimestamp();
		
		try {
			// wait a while to avoid hammering
			if (currentTimeStamp < nextSearch) {
				Log.Debug(String.format("Waiting %s seconds", (nextSearch - currentTimeStamp) / 1000), LogType.IMDB);
				Thread.sleep(nextSearch - currentTimeStamp);
			}
			WebResult webResult = getSearchResult(searchString, searchUrl);
			
			// Accomodate for that sometimes IMDB redirects you
			// directly to the correct movie. (i.e. "Cleanskin")
			IMDBRecord rec;

			if (webResult.isRedirected()) {
				Log.Info(String.format("IMDB :: %s is redirected to movie", m.getTitle()), LogType.IMDB);
				rec = IMDBRecord.getFromWebResult(webResult);
			}
			else {				
				Log.Info(String.format("IMDB :: %s is NOT redirected to movie", m.getTitle()), LogType.IMDB);				
				rec = findMovieInSearchResults(m, webResult.getResult());			
			}
			
			setNextSearchTimer();

			return rec;
		} catch (InterruptedException e) {
			return null;
		}
	}

	private static Series Search(Series series, int season, int episode, String searchUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	protected static WebResult getSearchResult(String title, String searchUrl)
			throws UnsupportedEncodingException, IOException {
		String urlParameters = java.net.URLEncoder.encode(title, "ISO-8859-1");
		String urlString = searchUrl.replace("%%TITLE%%", urlParameters);
		//String urlString = "http://www.imdb.com/find?s=tt&q=" + urlParameters;

		Log.Debug(String.format("Making web request. Url :: %s", urlString), LogType.IMDB);

		WebResult webResult = WebRetriever.getWebResult(urlString);
		return webResult;
	}

	private static void setNextSearchTimer() {
		// sleep randomly to avoid detection
		Random r = new Random();
		int minSeconds = Settings.imdb().getSettings().getSleepSecondsMin() * 1000;
		int maxSeconds = Settings.imdb().getSettings().getSleepSecondsMax() * 1000;
		int n = r.nextInt(minSeconds) + maxSeconds - minSeconds;
		
		nextSearch = Util.getCurrentTimestamp() + n;
	}

	private static Movie extractMovieInfo(Movie m, IMDBRecord rec) {
		if (rec != null) {
			// get releaseInfo to get the correct international title
			String preferredTitle = getPreferredTitle(rec);
			
			Builder b = Movie.newBuilder(m)
					.setImdbUrl(rec.getUrl())
					.setImdbId(Util.getImdbIdFromUrl(rec.getUrl()))
					.setDirector(rec.getDirector())
					.setDuration(rec.getDurationMinutes())
					.setStory(rec.getStory())
					.setRating(rec.getRating())
					.addAllGenre(rec.getAllGenres());
			
			if (!StringUtils.isEmpty(preferredTitle)) 
				b.setTitle(preferredTitle);
			
			if (rec.getImage() != null)
				b.setImage(ByteString.copyFrom(rec.getImage()));
			
			if (m.getYear() == 0)
				b.setYear(rec.getYear());
			
			return b.build();
		}
		else
			return m;
	}
		
	private static boolean usePreferredCountryDefault() {
		String preferredTitleCountry = Settings.imdb().getTitle().getPreferredLanguage();
		
		return StringUtils.isEmpty(preferredTitleCountry) 
				|| StringUtils.equalsIgnoreCase(preferredTitleCountry, "default");
	}
	
	private static String getPreferredTitle(IMDBRecord rec) {
		String title = rec.getTitle();
		boolean useOriginal = Settings.imdb().getTitle().isUseOriginalIfExists();
		String preferredTitleCountry = Settings.imdb().getTitle().getPreferredLanguage();		
		
		if (usePreferredCountryDefault() && !useOriginal) {
			return title;
		}
		else {
			String url = rec.getUrl() + "releaseinfo";
			
			try {
				WebResult webResult = WebRetriever.getWebResult(url);
				
				String foundTitle = findPreferredTitle(webResult.getResult(), preferredTitleCountry);
				return (StringUtils.isEmpty(foundTitle) ? title : foundTitle);
				
			} catch (IOException e) {
				Log.Error("Error when trying to get releaseinfo from IMDB", LogType.FIND, e);
			}
		}
		
		return title;
	}

	private static IMDBRecord findMovieInSearchResults(Movie m, String text) {
		List<SearchResultPattern> patterns = Settings.imdb().getSearchPatterns().getSearchResultPattern();
		
		Collections.sort(patterns, new SearchPatternComparer());
		IMDBRecord rec = null;
		for (SearchResultPattern p : patterns) {
			if (p.isEnabled()) {
				if ((m.getIsTvEpisode() && p.isTvPattern()) || (!m.getIsTvEpisode() && !p.isTvPattern()))
				{
					Log.Debug(String.format("Using pattern :: %s", p.getName()), LogType.IMDB);
					
					rec = findUrl(
							  m
							, text
							, StringUtils.trim(p.getBlockPattern())
							, p.getGroupBlock()
							, StringUtils.trim(p.getRecordPattern())
							, p.getGroupRecordUrl()
							, p.getGroupRecordYear());
					
					if (rec != null)
						break;
				}
			}
		}
		
		return rec;
	}
	
	private static boolean testResult(Movie m, IMDBRecord result) {
		if (m == null || result == null)
			return false;
		
		return (m.getYear() == 0 || m.getYear() == result.getYear());
	}

	private static IMDBRecord findUrl(
			Movie movie, 
			String text, 
			String patternForBlock, 
			int patternGroupForBlock,
			String patternForRecord,
			int urlGroup,
			int yearGroup) {
		
		//TODO: Also match by length of movie
		try {
			List<String[]> records = parseBlockAndRecords(
					text
					, patternForBlock
					, patternGroupForBlock
					, patternForRecord
					, urlGroup
					, yearGroup);
		
			for (String[] record : records) {
				String url = record[0];
				int year = Integer.parseInt(record[1]);
								
				IMDBRecord rec = new IMDBRecord(url, year);
				
				// Check if movie was blacklisted. If it was get the next record matching
				if (!urlIsBlacklisted(url, movie) && testResult(movie, rec))
				{
					// if year and title matches then continue to the URL and extract information about the movie.
					rec = IMDBRecord.get(url);

					// If the duration of the movie corresponds with the information retreived from MediaInfo then we're
					// probably right. 

					return rec;
				}				
			}
		}
		catch (Exception e) {
			Log.Error(String.format("Error occured when trying to find %s in IMDB", movie.getTitle()), LogType.FIND, e);
		}

		return null;
	}

	private static String findPreferredTitle(String text, String country) {
		Log.Info(String.format("Finding preferred title for %s", country), LogType.FIND);
		Imdb.Title t = Settings.imdb().getTitle();
		Imdb.Title.TitleResultPattern s = t.getTitleResultPattern();
		
		List<String[]> records = parseBlockAndRecords(
				text
				, StringUtils.trim(s.getBlockPattern())
				, s.getGroupBlock()
				, StringUtils.trim(s.getRecordPattern())
				, s.getGroupRecordTitle()
				, s.getGroupRecordCountry());
		
		boolean useOriginal = t.isUseOriginalIfExists();
		
		for (String[] record : records) {
			String recordCountry = record[1];
			String recordTitle = record[0];
			
			if (useOriginal && StringUtils.containsIgnoreCase(recordCountry, "(original title)")) {
				Log.Info("IMDB :: TITLE :: Found original", LogType.FIND);
				return recordTitle;
			}
			
			if (StringUtils.containsIgnoreCase(recordCountry, country)) {
				Log.Info("IMDB :: TITLE :: Found specific", LogType.FIND);
				return recordTitle;		
			}
		}
		
		return StringUtils.EMPTY;
	}

	private static List<String[]> parseBlockAndRecords(
			String text, 
			String patternForBlock, 
			int patternGroupForBlock,
			String patternForRecord,
			int... groupIndex) {
				
		List<String[]> ret = new ArrayList<String[]>();
		
		Pattern p = Pattern.compile(patternForBlock
				, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		
		if (m.find()) {
			Log.Debug(String.format("Block pattern found"), LogType.IMDB);

			String blockMatch = m.group(patternGroupForBlock);
			
			Pattern pRec = Pattern.compile(patternForRecord
					, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
			Matcher mRec = pRec.matcher(blockMatch);
			
			while (mRec.find()) {				
				List<String> values = new ArrayList<String>();
				
				for (int i=0;i<groupIndex.length;i++) {
					values.add(mRec.group(groupIndex[i]));
				}
				
				ret.add(values.toArray(new String[values.size()]));
			}				
		}
		
		Log.Debug(String.format("%s records found", ret.size()), LogType.IMDB);

		return ret;
		
	}
	
	private static Movie getTvEpisodeInfo(Movie m, IMDBRecord rec) throws NumberFormatException, IOException, ParseException {
		Movie newMovie;
		
		// mainMovie contains the overall info about the series
		// mainMovie should contain the episode title. 
		// So transfer the properties to the season object 
		// get the page referencing the wanted season
		if (m.getIsTvEpisode() && m.getEpisode() != null && m.getEpisode().getSeason() != null) {
			int seasonNumber = m.getEpisode().getSeason().getSeasonNumber();
			Log.Debug(String.format("Finding TV Epsiode info for %s. Season :: %s, Episode :: %s", m.getTitle(), seasonNumber, m.getEpisode()), LogType.IMDB);
			
			String seasonUrl = getSeasonUrl(seasonNumber, rec.getAllSeasonUrls());

			// extract episode info from that page
			IMDBEpisode ep = getEpisodeUrl(seasonUrl, m.getEpisode().getEpisodeNumber());
			
			// extract movie info from episode
			IMDBRecord episodeRec = IMDBRecord.get(ep.getUrl());
			
			newMovie = extractMovieInfo(m, episodeRec);

			// Keep the title from the original movie.
			// That should point to the Series title
			newMovie = Movie.newBuilder(newMovie).setTitle(m.getTitle()).build();
			
			// transfer main movie properties to the season object
//			Season s = Season.newBuilder(m.getSeason())
//					.addAllGenre(m.getGenreList())
//					.setImage(m.getImage())
//					.setRating(m.getRating())
//					.setStory(m.getStory())
//					.build();
			
			// build 
			newMovie = Movie.newBuilder(newMovie)
//					.setSeason(s)
					.setEpisode(ep.getEpisodeNumber())
					.setFirstAirDate(ep.getFirstAirDate().getTime())
					.setEpisodeTitle(ep.getTitle())			
					.build();
			
			return newMovie;
		}
		else {
			Log.Debug(String.format("%s appears to be a tv episode but no season found", m.getMedia(0).getFilename()), LogType.IMDB);
		}
		
		return m;
	}

	private static IMDBEpisode getEpisodeUrl(String url, int episode) throws IOException, NumberFormatException, ParseException {
		WebResult result = WebRetriever.getWebResult(url);
		
		for (EpisodePattern p : Settings.imdb().getEpisodePatterns().getEpisodePattern()) {
			Pattern pattern = Pattern.compile(p.getRegex(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(result.getResult());
			while(matcher.find()){
				IMDBEpisode ep = new IMDBEpisode(
					matcher.group(p.getUrlGroup())
					, Integer.parseInt(matcher.group(p.getEpisodeGroup()))
					, matcher.group(p.getTitleGroup())
					, DateUtils.parseDate(matcher.group(p.getAirDateGroup()), 
							Settings.imdb().getDatePatterns().getPattern().toArray(new String[]{})));
						
				if (ep.getEpisodeNumber() == episode) {
					return ep;
				}
			}
		}
		
		return null;
	}

	private static String getSeasonUrl(int seasonNumber, List<String> seasonUrls) {
		for(String url : seasonUrls) {
			Log.Debug(String.format("Testing episode url %s", url), LogType.IMDB);
			if (StringUtils.contains(url, String.format("episodes?season=%s", seasonNumber))) {
				return url;
			}
		}
		
		return StringUtils.EMPTY;
	}

}
