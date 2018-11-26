package se.qxx.jukebox.imdb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.DomainUtil;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.factories.IMDBParserFactory;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IIMDBFinder;
import se.qxx.jukebox.interfaces.IIMDBParser;
import se.qxx.jukebox.interfaces.IIMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IRandomWaiter;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.settings.imdb.Imdb;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.tools.WebResult;

@Singleton
public class IMDBFinder implements IIMDBFinder {
	private static ReentrantLock lock = new ReentrantLock();			
	
	private ISettings settings;
	private IWebRetriever webRetriever;
	private IIMDBUrlRewrite urlRewrite;
	private IMDBParserFactory parserFactory;
	private IJukeboxLogger log;
	private IRandomWaiter waiter;
	
	@Inject
	public IMDBFinder(ISettings settings, 
			IWebRetriever webRetriever, 
			IIMDBUrlRewrite urlRewrite, 
			IMDBParserFactory parserFactory,
			LoggerFactory loggerFactory, 
			IRandomWaiter waiter) {
		
		this.setWaiter(waiter);
		this.setSettings(settings);
		this.setWebRetriever(webRetriever);
		this.setUrlRewrite(urlRewrite);
		this.setParserFactory(parserFactory);
		this.setLog(loggerFactory.create(LogType.IMDB));
	}
	
	public IRandomWaiter getWaiter() {
		return waiter;
	}

	public void setWaiter(IRandomWaiter waiter) {
		this.waiter = waiter;
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public IMDBParserFactory getParserFactory() {
		return parserFactory;
	}

	public void setParserFactory(IMDBParserFactory parserFactory) {
		this.parserFactory = parserFactory;
	}

	public IIMDBUrlRewrite getUrlRewrite() {
		return urlRewrite;
	}

	public void setUrlRewrite(IIMDBUrlRewrite urlRewrite) {
		this.urlRewrite = urlRewrite;
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

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBFinder#Get(se.qxx.jukebox.domain.JukeboxDomain.Movie)
	 */
	@Override
	public Movie Get(Movie m) throws IOException, NumberFormatException, ParseException {
		lock.lock();
		try {
			this.getLog().Debug("---------------------------------------------------------------------------");
			this.getLog().Debug(String.format("Starting search on title :: %s (%s)", m.getTitle(), m.getYear()));
			this.getLog().Debug("---------------------------------------------------------------------------");
			String imdbUrl = m.getImdbUrl();
	 
			IMDBRecord rec = null;
			if (StringUtils.isEmpty(imdbUrl) || urlIsBlacklisted(imdbUrl, m.getBlacklistList())) {
				rec = Search(m.getTitle(), m.getYear(), m.getBlacklistList(), false); 
			}
			else {
				this.getLog().Debug(String.format("IMDB url found."));
				
				rec = getImdbResult(imdbUrl);
			}
			
			return extractMovieInfo(m, rec);			
		}
		finally {
			lock.unlock();
		}
	}

	private IMDBRecord getImdbResult(String url) throws IOException {
		String internalUrl = this.getUrlRewrite().fixUrl(StringUtils.trim(url));
		
		this.getLog().Debug(String.format("IMDBRECORD :: Making web request to url :: %s", internalUrl));

		WebResult webResult = this.getWebRetriever().getWebResult(internalUrl);
		Document doc = Jsoup.parse(webResult.getResult());
		
		IIMDBParser parser = this.getParserFactory().create(doc);
		IMDBRecord rec = parser.parse(webResult.getUrl());
		
		if (!StringUtils.isEmpty(rec.getImageUrl())) 
			rec.setImage(this.getWebRetriever().getWebFileData(rec.getImageUrl()));
		
		return rec;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBFinder#Get(se.qxx.jukebox.domain.JukeboxDomain.Series, int, int)
	 */
	@Override
	public Series Get(Series series, int season, int episode) throws IOException, NumberFormatException, ParseException {
		this.getLog().Debug("---------------------------------------------------------------------------");
		this.getLog().Debug(String.format("Starting search on series title :: %s (%s) S%s E%s", series.getTitle(), series.getYear(), season, episode));
		this.getLog().Debug("---------------------------------------------------------------------------");


		lock.lock();
		try {
			Series s = series;
			Season sn = DomainUtil.findSeason(s, season);
			Episode ep = DomainUtil.findEpisode(sn, episode);
	
			this.getLog().Debug(String.format("IMDB :: Number of episodes in season :: %s", sn.getEpisodeCount()));		
	
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
					this.getLog().Error("No series found in IMDB !!");
				}
			}			
			
			// extract episode info from that page
			ep = populateEpisode(sn.getImdbUrl(), ep);
			
			if (ep != null) {
				this.getLog().Debug("IMDB :: Updating episode in season object");
				sn = DomainUtil.updateEpisode(sn, ep);
		
				this.getLog().Debug("IMDB :: Updating season in series object");
				s = DomainUtil.updateSeason(s, sn);
				
				this.getLog().Debug(String.format("IMDB :: Number of episodes in season :: %s", sn.getEpisodeCount()));		
			}
			else {
				this.getLog().Debug("No episode found!");
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

		this.getLog().Debug("IMDB :: Getting episode info");
		
		if (!StringUtils.isEmpty(seasonUrl)) {
			episodeRec = getEpisodeRec(seasonUrl, ep.getEpisodeNumber());
			
			return populateEpisodeInfo(ep, episodeRec);
		}
		else {
			return ep;
		}
	}

	private Season populateSeason(Season sn, IMDBRecord seriesRec) {
		this.getLog().Debug("IMDB :: Updating season object with urls");
		String seasonUrl = seriesRec.getAllSeasonUrls().get(sn.getSeasonNumber());
		this.getLog().Debug(String.format("IMDB :: Season URL :: %s", seasonUrl));
		
		return Season.newBuilder(sn)
				.setImdbUrl(seasonUrl)
				.setImdbId(getImdbIdFromUrl(seasonUrl))
				.build();
	}
	
	private Series populateSeries(Series s, IMDBRecord seriesRec) {
		this.getLog().Debug("IMDB :: Creating new series object");
		return extractSeriesInfo(s, seriesRec);					
	}
	
	private IMDBRecord getSeriesRecord(Series s, int season) throws NumberFormatException, IOException, ParseException {
		if (StringUtils.isEmpty(s.getImdbUrl())) {
			return searchSeriesAndCheckSeason(s, season);
		}
		else {
			return getImdbResult(s.getImdbUrl());
		}
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
			seriesBlacklist.add(getImdbIdFromUrl(seriesRec.getUrl()));
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
		String imdbid = getImdbIdFromUrl(imdbUrl);
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
	public IMDBRecord Search(
			String searchString, 
			int yearToFind, 
			List<String> blacklist, 
			boolean isTvEpisode) throws IOException, NumberFormatException, ParseException {
		
		
		String searchUrl = this.getSettings().getImdb().getSearchUrl();
		
		lock.lock();
		try {
			int minSeconds = this.getSettings().getImdb().getSettings().getSleepSecondsMin();
			int maxSeconds = this.getSettings().getImdb().getSettings().getSleepSecondsMax();

			this.getWaiter().sleep(maxSeconds, minSeconds);

			WebResult webResult = getSearchResult(searchString, searchUrl);
			
			// Accomodate for that sometimes IMDB redirects you
			// directly to the correct movie. (i.e. "Cleanskin")
			IMDBRecord rec = null;

			if (webResult.isRedirected()) {
				this.getLog().Info(String.format("IMDB :: %s is redirected to movie", searchString));
				return getImdbResult(webResult.getResult());
			}
			else {				
				this.getLog().Info(String.format("IMDB :: %s is NOT redirected to movie", searchString));
				String url = 
					findUrl(blacklist
						, webResult.getResult()
						, yearToFind
						, isTvEpisode);
				
				if (!StringUtils.isEmpty(url))
					rec = getImdbResult(url);			
			}
			
			return rec;
		}
		finally {
			lock.unlock();
		}
	}


	protected WebResult getSearchResult(String title, String searchUrl)
			throws UnsupportedEncodingException, IOException {
		String urlParameters = java.net.URLEncoder.encode(title, "ISO-8859-1");
		String urlString = searchUrl.replace("%%TITLE%%", urlParameters);
		//String urlString = "http://www.imdb.com/find?s=tt&q=" + urlParameters;

		this.getLog().Debug(String.format("Making web request. Url :: %s", urlString));

		WebResult webResult = this.getWebRetriever().getWebResult(urlString);
		return webResult;
	}


	private Movie extractMovieInfo(Movie m, IMDBRecord rec) {
		if (rec != null) {
			// get releaseInfo to get the correct international title
			String preferredTitle = getPreferredTitle(rec);
			
			Builder b = Movie.newBuilder(m)
					.setImdbUrl(rec.getUrl())
					.setImdbId(getImdbIdFromUrl(rec.getUrl()))
					.setDirector(rec.getDirector())
					.setDuration(rec.getDurationMinutes())
					.setStory(rec.getStory())
					.setRating(rec.getRating())
					.addAllGenre(rec.getAllGenres());
			
			if (!StringUtils.isEmpty(preferredTitle)) 
				b.setTitle(preferredTitle);
			
			if (rec.getImage() != null) {
				this.getLog().Debug(String.format("Setting image. Length :: %s",  rec.getImage().length));
				
				ByteString image = ByteString.copyFrom(rec.getImage());
				b.setImage(image);
				try {
					b.setThumbnail(Util.getScaledImage(image));
				} catch (IOException e) {
					this.getLog().Error("Error when creating thumbnail");
				}
			}
			else {
				this.getLog().Debug("Image IS NULL");
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
					.setImdbId(getImdbIdFromUrl(rec.getUrl()))
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
				this.getLog().Debug(String.format("Setting image. Length :: %s",  rec.getImage().length));
				
				ByteString image = ByteString.copyFrom(rec.getImage());
				b.setImage(image);
				try {
					b.setThumbnail(Util.getScaledImage(image));
				} catch (IOException e) {
					this.getLog().Error("Error when creating thumbnail");
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
					.setImdbId(getImdbIdFromUrl(rec.getUrl()))
					.setStory(rec.getStory())
					.setRating(rec.getRating())
					.addAllGenre(rec.getAllGenres());
			
			if (!StringUtils.isEmpty(preferredTitle)) 
				b.setTitle(preferredTitle);
			
			if (rec.getImage() != null) {
				this.getLog().Debug(String.format("Setting image. Length :: %s",  rec.getImage().length));
				
				ByteString image = ByteString.copyFrom(rec.getImage());
				b.setImage(image);
				try {
					b.setThumbnail(Util.getScaledImage(image));
				} catch (IOException e) {
					this.getLog().Error("Error when creating thumbnail");
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
				WebResult webResult = this.getWebRetriever().getWebResult(url);
				
				String foundTitle = findPreferredTitle(webResult.getResult(), preferredTitleCountry);
				return (StringUtils.isEmpty(foundTitle) ? title : foundTitle);
				
			} catch (IOException e) {
				this.getLog().Error("Error when trying to get releaseinfo from IMDB", e);
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
			this.getLog().Error(String.format("Error occured when trying to find %s in IMDB", text), e);
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
		this.getLog().Info(String.format("Finding preferred title for %s", country));
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
		this.getLog().Debug(String.format("IMDB :: Epsiode :: %s - URL :: %s", episode, seasonUrl));
		
		// get the record for the season page
		IMDBRecord rec = getImdbResult(seasonUrl);
		
		String episodeUrl = rec.getAllEpisodeUrls().get(episode);
		if (!StringUtils.isEmpty(episodeUrl)) {
			return getImdbResult(episodeUrl);
		}
		
		return null;
		
	}

	private String getImdbIdFromUrl(String imdbUrl) {
		// http://www.imdb.com/title/tt1541874/
		Pattern p = Pattern.compile("\\/(tt\\d*)\\/", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(imdbUrl);
		if (m.find())
			return m.group(1);
		else
			return StringUtils.EMPTY;
	}

}
