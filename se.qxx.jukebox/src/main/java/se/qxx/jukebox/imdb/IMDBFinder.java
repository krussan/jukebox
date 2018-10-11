package se.qxx.jukebox.imdb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.Imdb;
import se.qxx.jukebox.settings.imdb.Imdb.EpisodePatterns.EpisodePattern;
import se.qxx.jukebox.settings.imdb.Imdb.SearchPatterns.SearchResultPattern;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.tools.WebResult;
import se.qxx.jukebox.tools.WebRetriever;
import se.qxx.jukebox.settings.imdb.SearchPatternComparer;

import com.google.protobuf.ByteString;

public class IMDBFinder {
	private static long nextSearch = 0;
	
	public synchronized static Movie Get(Movie m) throws IOException, NumberFormatException, ParseException {
		Log.Debug("---------------------------------------------------------------------------", LogType.IMDB);
		Log.Debug(String.format("Starting search on title :: %s (%s)", m.getTitle(), m.getYear()), LogType.IMDB);
		Log.Debug("---------------------------------------------------------------------------", LogType.IMDB);
		String imdbUrl = m.getImdbUrl();
 
		IMDBRecord rec = null;
		if (StringUtils.isEmpty(imdbUrl) || urlIsBlacklisted(imdbUrl, m.getBlacklistList())) {
			rec = Search(m.getTitle(), m.getYear(), m.getBlacklistList(), false); 
		}
		else {
			Log.Debug(String.format("IMDB url found."), LogType.IMDB);
			
			rec = IMDBRecord.get(imdbUrl);
		}
		
		return extractMovieInfo(m, rec);
	}
	
	public synchronized static Series Get(Series series, int season, int episode) throws IOException, NumberFormatException, ParseException {
		Log.Debug("---------------------------------------------------------------------------", LogType.IMDB);
		Log.Debug(String.format("Starting search on series title :: %s (%s) S%s E%s", series.getTitle(), series.getYear(), season, episode), LogType.IMDB);
		Log.Debug("---------------------------------------------------------------------------", LogType.IMDB);


		Series s = series;
		Season sn = DomainUtil.findSeason(s, season);
		Episode ep = DomainUtil.findEpisode(sn, episode);

		Log.Debug(String.format("IMDB :: Number of episodes in season :: %s", sn.getEpisodeCount()), LogType.IMDB);		

		if (sn == null || s == null || ep == null)
			throw new IllegalArgumentException("Object hierarchy for series need to be created before IMDB call");

		IMDBRecord seriesRec = null;
		if (StringUtils.isEmpty(s.getImdbUrl()) || StringUtils.isEmpty(sn.getImdbUrl())) {
			seriesRec = getSeriesRecord(s);

			// seriesRecord indicate if we have
			if (seriesRec != null) {
				s = populateSeries(s, seriesRec);
				sn = populateSeason(sn, seriesRec);
			}
			else {
				Log.Error("No series found in IMDB !!", LogType.IMDB);
			}
		}			
		
		// extract episode info from that page
		ep = populateEpisode(sn.getImdbUrl(), ep);
		
		if (ep != null) {
			Log.Debug("IMDB :: Updating episode in season object", LogType.IMDB);
			sn = DomainUtil.updateEpisode(sn, ep);
	
			Log.Debug("IMDB :: Updating season in series object", LogType.IMDB);
			s = DomainUtil.updateSeason(s, sn);
			
			Log.Debug(String.format("IMDB :: Number of episodes in season :: %s", sn.getEpisodeCount()), LogType.IMDB);		
		}
		else {
			Log.Debug("No episode found!", LogType.IMDB);
		}
		return s;
	}

	private static Episode populateEpisode(String seasonUrl, Episode ep)
			throws IOException, ParseException, MalformedURLException {
		IMDBRecord episodeRec;
		IMDBEpisode iep;

		Log.Debug("IMDB :: Getting episode info", LogType.IMDB);
		
		if (!StringUtils.isEmpty(seasonUrl)) {
			iep = getEpisodeRec(seasonUrl, ep.getEpisodeNumber());
			
			if (iep == null)
				return null;
			
			episodeRec = IMDBRecord.get(iep.getUrl());
			
			return populateEpisodeInfo(ep, episodeRec);
		}
		else {
			return ep;
		}
	}

	private static Season populateSeason(Season sn, IMDBRecord seriesRec) {
		Log.Debug("IMDB :: Updating season object with urls", LogType.IMDB);
		String seasonUrl = seriesRec.getAllSeasonUrls().get(sn.getSeasonNumber());
		Log.Debug(String.format("IMDB :: Season URL :: %s", seasonUrl), LogType.IMDB);
		
		return Season.newBuilder(sn)
				.setImdbUrl(seasonUrl)
				.setImdbId(Util.getImdbIdFromUrl(seasonUrl))
				.build();
	}
	
	private static Series populateSeries(Series s, IMDBRecord seriesRec) {
		Log.Debug("IMDB :: Creating new series object", LogType.IMDB);
		return extractSeriesInfo(s, seriesRec);					
	}
	
	private static IMDBRecord getSeriesRecord(Series s) throws NumberFormatException, IOException, ParseException {
		if (StringUtils.isEmpty(s.getImdbUrl())) {
			return Search(s.getTitle(), s.getYear(), null, true);
		}
		else
			return IMDBRecord.get(s.getImdbUrl());
	}


	/**
	 * Checks if an IMDB url is among the blacklisted url:s
	 * @param imdbUrl		 The Url to check
	 * @param blacklistedIDs The list of blacklisted IMDB id's
	 * @return
	 */
	private static boolean urlIsBlacklisted(String imdbUrl, List<String> blacklist) {
		String imdbid = Util.getImdbIdFromUrl(imdbUrl);
		if (!StringUtils.isEmpty(imdbid) && blacklist != null) {	
			for (String entry : blacklist) {	
				if (StringUtils.equalsIgnoreCase(imdbid, entry)) {
					return true;
				}
			}
		}
		
		return false;
	}

	public synchronized static IMDBRecord Search(
			String searchString, 
			int yearToFind, 
			List<String> blacklist, 
			boolean isTvEpisode) throws IOException, NumberFormatException, ParseException {
		long currentTimeStamp = Util.getCurrentTimestamp();
		
		String searchUrl = Settings.imdb().getSearchUrl();
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
				Log.Info(String.format("IMDB :: %s is redirected to movie", searchString), LogType.IMDB);
				rec = IMDBRecord.getFromWebResult(webResult);
			}
			else {				
				Log.Info(String.format("IMDB :: %s is NOT redirected to movie", searchString), LogType.IMDB);
				rec = IMDBRecord.get(
					findUrl(
						  blacklist
						, webResult.getResult()
						, yearToFind
						, isTvEpisode));			
			}
			
			setNextSearchTimer();

			return rec;
		} catch (InterruptedException e) {
			return null;
		}
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
			
			if (rec.getImage() != null) {
				ByteString image = ByteString.copyFrom(rec.getImage());
				b.setImage(image);
				try {
					b.setThumbnail(Util.getScaledImage(image));
				} catch (IOException e) {
					Log.Error("Error when creating thumbnail", LogType.IMDB);
				}
			}
			
			if (m.getYear() == 0)
				b.setYear(rec.getYear());
			
			return b.build();
		}
		else
			return m;
	}
	
	private static Episode populateEpisodeInfo(Episode ep, IMDBRecord rec) {
		if (rec != null) {
			// get releaseInfo to get the correct international title
			String preferredTitle = getPreferredTitle(rec);
			
			Episode.Builder b = Episode.newBuilder(ep)
					.setImdbUrl(rec.getUrl())
					.setImdbId(Util.getImdbIdFromUrl(rec.getUrl()))
					.setDirector(rec.getDirector())
					.setDuration(rec.getDurationMinutes())
					.setStory(rec.getStory())
					.setRating(rec.getRating())
					.addAllGenre(rec.getAllGenres())
					.setYear(rec.getYear())
					.setFirstAirDate(rec.getFirstAirDate().getTime());
			
			if (!StringUtils.isEmpty(preferredTitle)) 
				b.setTitle(preferredTitle);
			
			if (rec.getImage() != null)
				b.setImage(ByteString.copyFrom(rec.getImage()));

			return b.build();
		}
		else
			return null;
	}
	
	private static Series extractSeriesInfo(Series s, IMDBRecord rec) {
		if (rec != null) {
			// get releaseInfo to get the correct international title
			String preferredTitle = getPreferredTitle(rec);
			
			Series.Builder b = Series.newBuilder(s)
					.setImdbUrl(rec.getUrl())
					.setImdbId(Util.getImdbIdFromUrl(rec.getUrl()))
					.setStory(rec.getStory())
					.setRating(rec.getRating())
					.addAllGenre(rec.getAllGenres());
			
			if (!StringUtils.isEmpty(preferredTitle)) 
				b.setTitle(preferredTitle);
			
			if (rec.getImage() != null)
				b.setImage(ByteString.copyFrom(rec.getImage()));
			
			if (s.getYear() == 0)
				b.setYear(rec.getYear());
			
			return b.build();
		}
		else
			return s;
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

		


	public static String findUrl(
			List<String> blacklist, 
			String text,
			int yearToFind,
			boolean isTvEpisode) {
		
		try {
			Document doc = Jsoup.parse(text);
			String selector = getElementSelector(yearToFind, isTvEpisode);
					
			Elements elm = doc.select(selector);
			for (Element e : elm) {
				String url = e.attr("href");
	
				// Check if movie was blacklisted. If it was get the next record matching
				if (!urlIsBlacklisted(url, blacklist))
					// if year and title matches then continue to the URL and extract information about the movie.
					return url;
			}
			
		}
		catch (Exception e) {
			Log.Error(String.format("Error occured when trying to find %s in IMDB", text), LogType.FIND, e);
		}

		return null;
	}

	private static String getElementSelector(int yearToFind, boolean isTvEpisodeSearch) {
		String selector = StringUtils.EMPTY;
		if (!isTvEpisodeSearch) {
			selector = yearToFind > 0 ?
				String.format("tr.findResult:matches(\\(%s\\)) a", yearToFind) :
				"tr.findResult a";
		}
		else {
			selector = yearToFind > 0 ?
				String.format("tr.findResult:matches(\\(%s\\).*?\\(TV\\s*Series\\))", yearToFind) :
				"tr.findResult:matches(\\(TV\\s*Series\\))";
		}
		return selector;
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

	private static IMDBEpisode getEpisodeRec(String url, int episode) throws IOException, NumberFormatException, ParseException {
		Log.Debug(String.format("IMDB :: Epsiode :: %s - URL :: %s", episode, url), LogType.IMDB);
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
			if (StringUtils.contains(url, String.format("season=%s", seasonNumber))) {
				return url;
			}
		}
		
		return StringUtils.EMPTY;
	}

}
