package se.qxx.jukebox.subtitles;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.WebRetriever;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.subtitles.SubFile.Rating;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

public class UndertexterSe extends SubFinderBase {
	
	public UndertexterSe(SubFinderSettings subFinderSettings) {
		super(subFinderSettings);
	}

	private final String SETTING_URL = "url";
	private final String SETTING_PATTERN = "regex";
	
	@Override
	public List<SubFile> findSubtitles(
			Movie m, 
			List<String> languages) throws IOException {
				
		String searchString = getSearchString(m);
		String url = this.getSetting(SETTING_URL).replaceAll("__searchString__", searchString);
		String webResult = performSearch(url);
				
		// rate subs first and extract all information
		// if we found an exact match get that one
		// if we found a postive match get that one
		// otherwise get all
		List<SubFile> listSubs = collectSubFiles(m, webResult);
		List<SubFile> files = downloadSubs(listSubs);

		return files;
	}

	protected List<SubFile> downloadSubs(List<SubFile> listSubs) throws IOException {
		List<SubFile> files = new ArrayList<SubFile>();
		
		//Store downloaded files in temporary storage
		//SubtitleDownloader will move them to correct path
		String tempSubPath = createTempSubsPath();
		
		for (SubFile sf : listSubs) {
			File file = WebRetriever.getWebFile(sf.getUrl(), tempSubPath);
			sf.setFile(file);
			
			files.add(sf);

			Log.Debug(String.format("File downloaded: %s", sf.getFile().getName()), Log.LogType.SUBS);

			if (sf.getRating() == Rating.ExactMatch || sf.getRating() == Rating.PositiveMatch)  {
				Log.Debug("Exact or positive match found. exiting...", Log.LogType.SUBS);
				break;
			}
			
			try {
				Random r = new Random();
				int n = r.nextInt(20000) + 10000;
				
				// sleep randomly to avoid detection (from 10 sec to 30 sec)
				Thread.sleep(n);
			} catch (InterruptedException e) {
				Log.Error("Subtitle downloader interrupted", Log.LogType.SUBS, e);
			}
			
		}
		
		return files;
		
	}

	protected List<SubFile> collectSubFiles(Movie m, String webResult) {
		String pattern = this.getSetting(SETTING_PATTERN);
		NamedPattern p = NamedPattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		NamedMatcher matcher = p.matcher(webResult);
		
		List<SubFile> listSubs = new ArrayList<SubFile>();
		
		Log.Debug(String.format("UndertexterSe :: Finding subtitles for %s", m.getFilename()), Log.LogType.SUBS);
		
		while (matcher.find()) {
			String urlString = matcher.group("url");
			String description = matcher.group("name"); 
			
			SubFile sf = new SubFile(urlString, description);
			Rating r = Util.rateSub(m, description);
			sf.setRating(r);
			Log.Debug(String.format("UndertexterSe :: Sub with description %s rated as %s", description, r.toString()), Log.LogType.SUBS);
			
			listSubs.add(sf);
		}
		
		Collections.sort(listSubs);
		return listSubs;
	}

	protected String performSearch(String url) throws IOException {
		String webResult = WebRetriever.getWebResult(url).getResult();

		// replace newline
		webResult = webResult.replace("\r", "");
		webResult = webResult.replace("\n", "");
		//pattern = "<tr[^>]*?>.*?<td[^>]*?>.*?<a[^>]*?alt\s*=\s*"Ladda\sner\sundertext[^"]*?".*?href\s*=\s*\"(?<url>[^"]*?)"[^>]*?>.*?Nedladdningar.*?<br>\s*(?<name>[^<]*?)</td[^>]*?>.*?</tr[^>]*?>"
		//pattern = "<tr[^>]*?>.*?<td[^>]*?>.*?<a[^>]*?alt\\s*=\\s*\"Ladda\\sner\\sundertext[^\"]*?\".*?href\\s*=\\s*\\\"(?<url>[^\\\"]*?)\\\"[^>]*?>.*?Nedladdningar.*?<br>\\s*(?<name>[^<]*?)</td[^>]*?>.*?</tr[^>]*?>";
		return webResult;
	}

	protected String getSearchString(Movie m)
			throws UnsupportedEncodingException {
		String searchString;
		String imdbId = m.getImdbId();
		
		if (imdbId != null && imdbId.length() > 0)
			searchString = m.getImdbId();
		else
			searchString = m.getTitle();
		
		searchString = java.net.URLEncoder.encode(searchString.trim(), "ISO-8859-1");
		return searchString;
	}

}
