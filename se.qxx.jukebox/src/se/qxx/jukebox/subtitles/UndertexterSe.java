package se.qxx.jukebox.subtitles;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.WebRetriever;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.subtitles.SubFile.Rating;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

public class UndertexterSe implements ISubtitleFinder {
	public UndertexterSe() {
	}
	
	@Override
	public List<SubFile> findSubtitles(Movie m, List<String> languages, String subsPath, JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings subFinderSettings) throws IOException {
		String pattern = "";
		String url = "";
		
		for (JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings.Setting setting : subFinderSettings.getSetting()) {
			String key = setting.getKey();
			if (key.equals("regex")) pattern = setting.getValue().trim();
			if (key.equals("url")) url = setting.getValue().trim();
		}
		
		String searchString;
		String imdbId = m.getImdbId();
		
		if (imdbId != null && imdbId.length() > 0)
			searchString = m.getImdbId();
		else
			searchString = m.getTitle();
		
		searchString = java.net.URLEncoder.encode(searchString.trim(), "ISO-8859-1");
		
		url = url.replaceAll("__searchString__", searchString);
		String webResult = WebRetriever.getWebResult(url);

		// replace newline
		webResult = webResult.replace("\r", "");
		webResult = webResult.replace("\n", "");
		//pattern = "<tr[^>]*?>.*?<td[^>]*?>.*?<a[^>]*?alt\s*=\s*"Ladda\sner\sundertext[^"]*?".*?href\s*=\s*\"(?<url>[^"]*?)"[^>]*?>.*?Nedladdningar.*?<br>\s*(?<name>[^<]*?)</td[^>]*?>.*?</tr[^>]*?>"
		//pattern = "<tr[^>]*?>.*?<td[^>]*?>.*?<a[^>]*?alt\\s*=\\s*\"Ladda\\sner\\sundertext[^\"]*?\".*?href\\s*=\\s*\\\"(?<url>[^\\\"]*?)\\\"[^>]*?>.*?Nedladdningar.*?<br>\\s*(?<name>[^<]*?)</td[^>]*?>.*?</tr[^>]*?>";
		
		Log.Debug(String.format("Pattern :: %s", pattern));
		NamedPattern p = NamedPattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		NamedMatcher matcher = p.matcher(webResult);
		
		List<SubFile> files = new ArrayList<SubFile>();
		
		// create sub store for this movie
		String filename = createSubsPath(m, subsPath);
		
		//TODO: rate subs first and extract all information
		//TODO: if we found an exact match get that one
		//      if we found a postive match get that one
		//      otherwise get all
		List<SubFile> listSubs = new ArrayList<SubFile>();
		
		while (matcher.find()) {
			String urlString = matcher.group("url");
			String description = matcher.group("name");
			
			SubFile sf = new SubFile(urlString, description);
			Rating r = Util.rateSub(m, description);
			sf.setRating(r);
			
			listSubs.add(sf);
		}
		
		Collections.sort(listSubs);
		for (SubFile sf : listSubs) {
			File file = WebRetriever.getWebFile(sf.getUrl(), filename);
			sf.setFile(file);
			
			files.add(sf);

			Log.Debug(String.format("File downloaded: %s", sf.getFile().getName()));

			if (sf.getRating() == Rating.ExactMatch || sf.getRating() == Rating.PositiveMatch)  {
				Log.Debug("Exact or positive match found. exiting...");
				break;
			}
			
			try {
				Random r = new Random();
				int n = r.nextInt(20000) + 10000;
				
				// sleep randomly to avoid detection (from 10 sec to 30 sec)
				Thread.sleep(n);
			} catch (InterruptedException e) {
				Log.Error("Subtitle downloader interrupted", e);
				return files;
			}
			
		}
		while (matcher.find()) {			
		}

		return files;
	}

	private String createSubsPath(Movie m, String subsPath) {
		String filename = subsPath + "/" + m.getFilename();
		filename = filename.substring(0, filename.lastIndexOf("."));
		File f = new File(filename);
		if (!f.exists())
			f.mkdirs();
		return filename;
	}
}
