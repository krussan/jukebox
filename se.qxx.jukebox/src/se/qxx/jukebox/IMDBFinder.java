package se.qxx.jukebox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Movie.Builder;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.Imdb;
import se.qxx.jukebox.settings.imdb.Imdb.SearchPatterns.SearchResultPattern;
import se.qxx.jukebox.settings.imdb.SearchPatternComparer;

import com.google.protobuf.ByteString;

public class IMDBFinder {
	private static long nextSearch = 0;
	
	public synchronized static Movie Get(Movie m, List<String> blacklistedIDs) throws IOException {
		Log.Debug(String.format("Starting search on title :: %s (%s)", m.getTitle(), m.getYear()), LogType.IMDB);
		String imdbUrl = m.getImdbUrl();

		if (StringUtils.isEmpty(imdbUrl) || urlIsBlacklisted(imdbUrl, blacklistedIDs)) {	
			return Search(m, blacklistedIDs);
		}
		else {
			Log.Debug(String.format("IMDB url found."), LogType.IMDB);
			
			IMDBRecord rec = IMDBRecord.get(imdbUrl);
			return extractMovieInfo(m, rec);
		}
	}
	
	private static boolean urlIsBlacklisted(String imdbUrl, List<String> blacklistedIDs) {
		String imdbid = Util.getImdbIdFromUrl(imdbUrl);
		if (!StringUtils.isEmpty(imdbid)) {	
			for (String entry : blacklistedIDs) {	
				if (StringUtils.equalsIgnoreCase(imdbid, entry)) {
					return true;
				}
			}
		}
		
		return false;
	}

	private synchronized static Movie Search(Movie m, List<String> blacklistedIDs) throws IOException {
        //http://www.imdb.com/find?s=all&q=the+decent
        // search for :
        // Titles (Exact Matches)
        // Popular Titles               <-- This is the one
        // Titles (Partial Matches)
        // Titles (Approx Matches)
        // find first href after that

        // Titles\s\(Exact\sMatches\).*?\<a\s*href\s*=\s*["|'](?<url>.*?)["|']
        // Popular\sTitles.*?\<a\s*href\s*=\s*["|'](?<url>.*?)["|']
	
		long currentTimeStamp = Util.getCurrentTimestamp();
		try {
			if (currentTimeStamp < nextSearch) {
				Log.Debug(String.format("Waiting %s seconds", (nextSearch - currentTimeStamp) / 1000), LogType.IMDB);
				Thread.sleep(nextSearch - currentTimeStamp);
			}
			WebResult webResult = getSearchResult(m);
			
			// Accomodate for that sometimes IMDB redirects you
			// directly to the correct movie. (i.e. "Cleanskin")
			IMDBRecord rec;

			if (webResult.isRedirected()) {
				Log.Info(String.format("IMDB :: %s is redirected to movie", m.getTitle()), LogType.IMDB);
				rec = IMDBRecord.getFromWebResult(webResult);
			}
			else {				
				Log.Info(String.format("IMDB :: %s is NOT redirected to movie", m.getTitle()), LogType.IMDB);				
				rec = findMovieInSearchResults(m, webResult.getResult(), blacklistedIDs);			
			}

			setNextSearchTimer();
			
			return extractMovieInfo(m, rec);
		} catch (InterruptedException e) {
			return m;
		}
	}

	protected static WebResult getSearchResult(Movie m)
			throws UnsupportedEncodingException, IOException {
		String urlParameters = java.net.URLEncoder.encode(m.getTitle(), "ISO-8859-1");
		String urlString = Settings.imdb().getSearchUrl().replace("%%TITLE%%", urlParameters);
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

	private static IMDBRecord findMovieInSearchResults(Movie m, String text, List<String> blacklistedIDs) {
		List<SearchResultPattern> patterns = Settings.imdb().getSearchPatterns().getSearchResultPattern();
		
		Collections.sort(patterns, new SearchPatternComparer());
		IMDBRecord rec = null;
		for (SearchResultPattern p : patterns) {
			if (p.isEnabled()) {
				Log.Debug(String.format("Using pattern :: %s", p.getName()), LogType.IMDB);
				
				rec = findUrl(m
						, text
						, StringUtils.trim(p.getBlockPattern())
						, p.getGroupBlock()
						, StringUtils.trim(p.getRecordPattern())
						, p.getGroupRecordUrl()
						, p.getGroupRecordYear()
						, blacklistedIDs);
				
				if  (rec!=null)
					break;
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
			int yearGroup, 
			List<String> blacklistedIDs) {
		
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
				if (!urlIsBlacklisted(url, blacklistedIDs) && testResult(movie, rec))
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
}
