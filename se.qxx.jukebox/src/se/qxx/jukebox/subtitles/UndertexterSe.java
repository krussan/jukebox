package se.qxx.jukebox.subtitles;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.WebRetriever;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
		
		// TODO Auto-generated method stub
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
		
		while (matcher.find()) {
			String urlString = matcher.group("url");
			String filename = subsPath + "/" + m.getFilename();
			filename = filename.substring(0, filename.lastIndexOf("."));
			File f = new File(filename);
			
			if (!f.exists())
				f.mkdirs();
			
			String description = matcher.group("name");
		
			File file = WebRetriever.getWebFile(urlString, filename);
			
			SubFile subFile = new SubFile(file);			
			subFile.setDescription(description);
			
			files.add(subFile);
			
			Log.Debug(String.format("File downloaded: %s", subFile.getFile().getName()));
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Log.Error("Subtitle downloader interrupted", e);
				return files;
			}
		}

		return files;
	}
}
