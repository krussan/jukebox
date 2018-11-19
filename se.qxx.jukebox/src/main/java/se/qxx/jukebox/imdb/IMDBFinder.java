package se.qxx.jukebox.imdb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.interfaces.IIMDBFinder;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.Imdb;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.tools.WebResult;
import se.qxx.jukebox.tools.WebRetriever;

@Singleton
public class IMDBFinder implements IIMDBFinder {
	private long nextSearch = 0;
	private static ReentrantLock lock = new ReentrantLock();			
	
	private ISettings settings;
	
	@Inject
	public IMDBFinder(ISettings settings) {
		this.setSettings(settings);
	}
	
	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBFinder#Get(se.qxx.jukebox.domain.JukeboxDomain.Movie)
	 */
	@Override
	public Movie Get(Movie m) throws IOException, NumberFormatException, ParseException {
		lock.lock();
		try {
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
		finally {
			lock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBFinder#Get(se.qxx.jukebox.domain.JukeboxDomain.Series, int, int)
	 */
	@Override
	public Series Get(Series series, int season, int episode) throws IOException, NumberFormatException, ParseException {
		Log.Debug("---------------------------------------------------------------------------", LogType.IMDB);
		Log.Debug(String.format("Starting search on series title :: %s (%s) S%s E%s", series.getTitle(), series.getYear(), season, episode), LogType.IMDB);
		Log.Debug("---------------------------------------------------------------------------", LogType.IMDB);


		lock.lock();
		try {
			Series s = series;
			Season sn = DomainUtil.findSeason(s, season);
			Episode ep = DomainUtil.findEpisode(sn, episode);
	
			Log.Debug(String.format("IMDB :: Number of episodes in season :: %s", sn.getEpisodeCount()), LogType.IMDB);		
	
			if (sn == null || s == null || ep == null)
				throw new IllegalArgumentException("Object hierarchy for series need to be created before IMDB call");
	
			IMDBRecord seriesRec = null;
			if (StringUtils.isEmpty(s.getImdbUrl()) || StringUtils.isEmpty(sn.getImdbUrl())) {
				seriesRec = getSeriesRecord(s, season);
	
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
		finally {
			lock.unlock();
		}
	}

	private Episode populateEpisode(String seasonUrl, Episode ep)
			throws IOException, ParseException, MalformedURLException {
		IMDBRecord episodeRec;

		Log.Debug("IMDB :: Getting episode info", LogType.IMDB);
		
		if (!StringUtils.isEmpty(seasonUrl)) {
			episodeRec = getEpisodeRec(seasonUrl, ep.getEpisodeNumber());
			
			return populateEpisodeInfo(ep, episodeRec);
		}
		else {
			return ep;
		}
	}

	private Season populateSeason(Season sn, IMDBRecord seriesRec) {
		Log.Debug("IMDB :: Updating season object with urls", LogType.IMDB);
		String seasonUrl = seriesRec.getAllSeasonUrls().get(sn.getSeasonNumber());
		Log.Debug(String.format("IMDB :: Season URL :: %s", seasonUrl), LogType.IMDB);
		
		return Season.newBuilder(sn)
				.setImdbUrl(seasonUrl)
				.setImdbId(Util.getImdbIdFromUrl(seasonUrl))
				.build();
	}
	
	private Series populateSeries(Series s, IMDBRecord seriesRec) {
		Log.Debug("IMDB :: Creating new series object", LogType.IMDB);
		return extractSeriesInfo(s, seriesRec);					
	}
	
	private IMDBRecord getSeriesRecord(Series s, int season) throws NumberFormatException, IOException, ParseException {
		if (StringUtils.isEmpty(s.getImdbUrl())) {
			return searchSeriesAndCheckSeason(s, season);
		}
		else
			return IMDBRecord.get(s.getImdbUrl());
	}

	/***
	 * Finds the series and iterate through the result list
	 * If a series is matched then also check that the series
	 * contains the actual season. Otherwise continue with the next series
	 * 
	 * @param s
	 * @param season
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private IMDBRecord searchSeriesAndCheckSeason(Series s, int season)
			throws IOException, ParseException {
		List<String> seriesBlacklist = new ArrayList<String>();
		
		boolean found = false;
		IMDBRecord seriesRec = null;
		while(!found) {
			seriesRec =
				Search(
					s.getTitle(), 
					s.getYear(), 
					seriesBlacklist, 
					true);

			// exit if series contains season or if the series record is null (no series found)
			found = seriesRec == null || seriesRec.getAllSeasonUrls().containsKey(season);
			seriesBlacklist.add(Util.getImdbIdFromUrl(seriesRec.getUrl()));
		}
		
		return seriesRec; 
	}


	/**
	 * Checks if an IMDB url is among the blacklisted url:s
	 * @param imdbUrl		 The Url to check
	 * @param blacklistedIDs The list of blacklisted IMDB id's
	 * @return
	 */
	private boolean urlIsBlacklisted(String imdbUrl, List<String> blacklist) {
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

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBFinder#Search(java.lang.String, int, java.util.List, boolean)
	 */
	@Override
	public  IMDBRecord Search(
			String searchString, 
			int yearToFind, 
			List<String> blacklist, 
			boolean isTvEpisode) throws IOException, NumberFormatException, ParseException {
		
		
		String searchUrl = this.getSettings().getImdb().getSearchUrl();
		
		lock.lock();
		try {
			waitRandom();
			WebResult webResult = getSearchResult(searchString, searchUrl);
			
			// Accomodate for that sometimes IMDB redirects you
			// directly to the correct movie. (i.e. "Cleanskin")
			IMDBRecord rec = null;

			if (webResult.isRedirected()) {
				Log.Info(String.format("IMDB :: %s is redirected to movie", searchString), LogType.IMDB);
				rec = IMDBRecord.getFromWebResult(webResult);
			}
			else {				
				Log.Info(String.format("IMDB :: %s is NOT redirected to movie", searchString), LogType.IMDB);
				String url = 
					findUrl(blacklist
						, webResult.getResult()
						, yearToFind
						, isTvEpisode);
				
				if (!StringUtils.isEmpty(url))
					rec = IMDBRecord.get(url);			
			}
			
			setNextSearchTimer();

			return rec;
		} catch (InterruptedException e) {
			return null;
		}
		finally {
			lock.unlock();
		}
	}

	private void waitRandom() throws InterruptedException {
		long currentTimeStamp = Util.getCurrentTimestamp();
		// wait a while to avoid hammering
		if (currentTimeStamp < nextSearch) {
			Log.Debug(String.format("Waiting %s seconds", (nextSearch - currentTimeStamp) / 1000), LogType.IMDB);
			Thread.sleep(nextSearch - currentTimeStamp);
		}
	}

	protected WebResult getSearchResult(String title, String searchUrl)
			throws UnsupportedEncodingException, IOException {
		String urlParameters = java.net.URLEncoder.encode(title, "ISO-8859-1");
		String urlString = searchUrl.replace("%%TITLE%%", urlParameters);
		//String urlString = "http://www.imdb.com/find?s=tt&q=" + urlParameters;

		Log.Debug(String.format("Making web request. Url :: %s", urlString), LogType.IMDB);

		WebResult webResult = WebRetriever.getWebResult(urlString);
		return webResult;
	}

	private void setNextSearchTimer() {
		// sleep randomly to avoid detection
		Random r = new Random();
		int minSeconds = this.getSettings().getImdb().getSettings().getSleepSecondsMin() * 1000;
		int maxSeconds = this.getSettings().getImdb().getSettings().getSleepSecondsMax() * 1000;
		int n = r.nextInt(minSeconds) + maxSeconds - minSeconds;
		
		nextSearch = Util.getCurrentTimestamp() + n;
	}

	private Movie extractMovieInfo(Movie m, IMDBRecord rec) {
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
				Log.Debug(String.format("Setting image. Length :: %s",  rec.getImage().length), LogType.IMDB);
				
				ByteString image = ByteString.copyFrom(rec.getImage());
				b.setImage(image);
				try {
					b.setThumbnail(Util.getScaledImage(image));
				} catch (IOException e) {
					Log.Error("Error when creating thumbnail", LogType.IMDB);
				}
			}
			else {
				Log.Debug("Image IS NULL", LogType.IMDB);
			}
			
			if (m.getYear() == 0)
				b.setYear(rec.getYear());
			
			return b.build();
		}
		else
			return m;
	}
	
	private Episode populateEpisodeInfo(Episode ep, IMDBRecord rec) {
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
					.setYear(rec.getYear());
			
			if (rec.getFirstAirDate() != null)
					b.setFirstAirDate(rec.getFirstAirDate().getTime());
			
			if (!StringUtils.isEmpty(preferredTitle)) 
				b.setTitle(preferredTitle);

			if (rec.getImage() != null) {
				Log.Debug(String.format("Setting image. Length :: %s",  rec.getImage().length), LogType.IMDB);
				
				ByteString image = ByteString.copyFrom(rec.getImage());
				b.setImage(image);
				try {
					b.setThumbnail(Util.getScaledImage(image));
				} catch (IOException e) {
					Log.Error("Error when creating thumbnail", LogType.IMDB);
				}
			}

			return b.build();
		}
		else
			return null;
	}
	
	private Series extractSeriesInfo(Series s, IMDBRecord rec) {
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
			
			if (rec.getImage() != null) {
				Log.Debug(String.format("Setting image. Length :: %s",  rec.getImage().length), LogType.IMDB);
				
				ByteString image = ByteString.copyFrom(rec.getImage());
				b.setImage(image);
				try {
					b.setThumbnail(Util.getScaledImage(image));
				} catch (IOException e) {
					Log.Error("Error when creating thumbnail", LogType.IMDB);
				}
			}
			
			if (s.getYear() == 0)
				b.setYear(rec.getYear());
			
			return b.build();
		}
		else
			return s;
	}
		
	private boolean usePreferredCountryDefault() {
		String preferredTitleCountry = this.getSettings().getImdb().getTitle().getPreferredLanguage();
		
		return StringUtils.isEmpty(preferredTitleCountry) 
				|| StringUtils.equalsIgnoreCase(preferredTitleCountry, "default");
	}
	
	private String getPreferredTitle(IMDBRecord rec) {
		String title = rec.getTitle();
		boolean useOriginal = this.getSettings().getImdb().getTitle().isUseOriginalIfExists();
		String preferredTitleCountry = this.getSettings().getImdb().getTitle().getPreferredLanguage();		
		
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

		


	public String findUrl(
			List<String> blacklist, 
			String text,
			int yearToFind,
			boolean isTvEpisode) {
		
		try {
			Document doc = Jsoup.parse(text);
			
			// get first url (when we search by year
			String url = findUrlInSearchResult(doc, yearToFind, isTvEpisode, blacklist);
			
			// if not found (and yearToFind exists) search without year
			if (StringUtils.isEmpty(url) && yearToFind > 0)
				url = findUrlInSearchResult(doc, 0, isTvEpisode, blacklist);
			
			return url;
		}
		catch (Exception e) {
			Log.Error(String.format("Error occured when trying to find %s in IMDB", text), LogType.FIND, e);
		}

		return null;
	}

	private String findUrlInSearchResult(
			Document doc,
			int yearToFind,
			boolean isTvEpisode,
			List<String> blacklist) {
		String selector = getElementSelector(yearToFind, isTvEpisode);

		Elements elm = doc.select(selector);
		for (Element e : elm) {
			String url = e.attr("href");

			// Check if movie was blacklisted. If it was get the next record matching
			if (!urlIsBlacklisted(url, blacklist))
				// if year and title matches then continue to the URL and extract information about the movie.
				return url;
		}
		
		return StringUtils.EMPTY;
	}

	private String getElementSelector(int yearToFind, boolean isTvEpisodeSearch) {
		String selector = StringUtils.EMPTY;
		
		if (!isTvEpisodeSearch) {
			if (yearToFind > 0)
				selector = String.format("tr.findResult td.result_text:matches(\\(%s\\)):not(:matches(\\(TV\\sEpisode\\))) a", yearToFind);
			else
				selector = "tr.findResult td.result_text:not(:matches(TV\\\\sEpisode)) a";
		}
		else {
			if (yearToFind > 0)
				selector = String.format("tr.findResult:matches(\\(%s\\).*?\\(TV\\s*Series\\)):not(:matches(\\(TV\\sEpisode\\))) a", yearToFind);
			else
				selector = "tr.findResult td.result_text:matches(\\(TV\\s*Series\\)):not(:matches(\\(TV\\sEpisode\\))) a";
		}
		return selector;
	}

	public String findPreferredTitle(String text, String country) {
		Log.Info(String.format("Finding preferred title for %s", country), LogType.FIND);
		Imdb.Title t = this.getSettings().getImdb().getTitle();

		if (!t.isUseOriginalIfExists()) {
			Document doc = Jsoup.parse(text);
			Elements elms = doc.select(String.format("table#akas tr:has(td:matches(%s))", country));
			
			if (elms.size() > 0) {
				return elms.get(0).text();
			}
		}
		
		return StringUtils.EMPTY;
	}

	private IMDBRecord getEpisodeRec(String seasonUrl, int episode) throws IOException, NumberFormatException, ParseException {
		Log.Debug(String.format("IMDB :: Epsiode :: %s - URL :: %s", episode, seasonUrl), LogType.IMDB);
		
		// get the record for the season page
		IMDBRecord rec = IMDBRecord.get(seasonUrl);
		
		String episodeUrl = rec.getAllEpisodeUrls().get(episode);
		if (!StringUtils.isEmpty(episodeUrl)) {
			return IMDBRecord.get(episodeUrl);
		}
		
		return null;
		
	}

}
