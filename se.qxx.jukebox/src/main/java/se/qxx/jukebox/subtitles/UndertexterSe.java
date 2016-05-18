package se.qxx.jukebox.subtitles;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.Language;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.WebRetriever;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;


public class UndertexterSe extends SubFinderBase {
	
	private String className;
	private Language language;
	
	public UndertexterSe(SubFinderSettings subFinderSettings) {
		super(subFinderSettings);
		this.setClassName("UndertexterSe");
		this.setLanguage(Language.Swedish);
	}
	
	public UndertexterSe(String className, Language language, SubFinderSettings subFinderSettings) {
		super(subFinderSettings);
		this.setClassName(className);
		this.setLanguage(language);
	}	

	private final String SETTING_URL = "url";
	private final String SETTING_PATTERN = "regex";
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}
	
	@Override
	public List<SubFile> findSubtitles(
			Movie m, 
			List<String> languages) {

		List<SubFile> files = new ArrayList<SubFile>();
		String searchString = getSearchString(m);
		if (!StringUtils.isEmpty(searchString)) {
			String url = this.getSetting(SETTING_URL).replaceAll("__searchString__", searchString);
			
			Log.Debug(String.format("%s :: searchUrl :: %s", this.getClassName(), url), LogType.SUBS);
			String webResult = performSearch(url);
					
			if (!StringUtils.isEmpty(webResult)) {
				// rate subs first and extract all information
				// if we found an exact match get that one
				// if we found a postive match get that one
				// otherwise get all
				List<SubFile> listSubs = collectSubFiles(m, webResult);
				files = downloadSubs(m, listSubs);
			}
		}
		
		return files;
	}

	protected List<SubFile> downloadSubs(Movie m, List<SubFile> listSubs) {
		List<SubFile> files = new ArrayList<SubFile>();
		
		//Store downloaded files in temporary storage
		//SubtitleDownloader will move them to correct path
		String tempSubPath = createTempSubsPath(m);

		Collections.sort(listSubs);
		
		int sizeCollection = listSubs.size();
		int c = 1;
		
		for (SubFile sf : listSubs) {
			try {
				File file = WebRetriever.getWebFile(sf.getUrl(), tempSubPath);
				if (file != null) {
					sf.setFile(file);
					
					files.add(sf);
		
					Log.Debug(String.format("%s :: [%s/%s] :: File downloaded: %s"
							, this.getClassName()
							, c
							, sizeCollection
							, sf.getFile().getName())
						, Log.LogType.SUBS);
		
					if (sf.getRating() == Rating.ExactMatch || sf.getRating() == Rating.PositiveMatch)  {
						Log.Debug(String.format("%s :: Exact or positive match found. exiting...", this.getClassName()), Log.LogType.SUBS);
						break;
					}
				}
				
				c++;
			}
			catch (IOException e) {
				Log.Debug(String.format("%s :: Error when downloading subtitle :: %s", this.getClassName(), sf.getFile().getName()), LogType.SUBS);
			}
			
			try {
				Random r = new Random();
				int n = r.nextInt(20000) + 10000;
				
				// sleep randomly to avoid detection (from 10 sec to 30 sec)
				Thread.sleep(n);
			} catch (InterruptedException e) {
				Log.Error(String.format("Subtitle downloader interrupted", this.getClassName()), Log.LogType.SUBS, e);
			}
			
		}
		
		return files;
		
	}

	protected List<SubFile> collectSubFiles(Movie m, String webResult) {
		String pattern = this.getSetting(SETTING_PATTERN);
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		Matcher matcher = p.matcher(webResult);
		
		String urlRegexGroup = getSetting("urlRegexGroup");
		String nameRegexGroup = getSetting("nameRegexGroup");
		
		int intUrlGroup = Integer.parseInt(urlRegexGroup);
		int intNameGroup = Integer.parseInt(nameRegexGroup);
		
		List<SubFile> listSubs = new ArrayList<SubFile>();
		
		Log.Debug(String.format("%s :: Finding subtitles for %s", this.getClassName(), m.getMedia(0).getFilename()), Log.LogType.SUBS);
		
		while (matcher.find()) {
			String urlString = matcher.group(intUrlGroup);
			String description = matcher.group(intNameGroup); 
//			String part = matcher.group(intPartGroup);
//			int nrOfParts = 0;
//			if (Util.tryParseInt(part)) 
//				nrOfParts = Integer.parseInt(part);
//			
			//TODO:: is it safe to do this? Could it be that one of the media has not yet been identified?
			// if (nrOfParts == m.getMediaCount())
				
			SubFile sf = new SubFile(urlString, description, this.getLanguage());
			Rating r = this.rateSub(m, description);
			sf.setRating(r);
			Log.Debug(String.format("%s :: Sub with description %s rated as %s", this.getClassName(), description, r.toString()), Log.LogType.SUBS);
			
			listSubs.add(sf);
		}
		
		if (listSubs.size()==0)
			Log.Debug(String.format("%s :: No subs found", this.getClassName()), Log.LogType.SUBS);
					
		return listSubs;
	}

	protected String performSearch(String url) {
		String webResult;
		try {
			webResult = WebRetriever.getWebResult(url).getResult();
	
			// replace newline
			webResult = webResult.replace("\r", "");
			webResult = webResult.replace("\n", "");
			//pattern = "<tr[^>]*?>.*?<td[^>]*?>.*?<a[^>]*?alt\s*=\s*"Ladda\sner\sundertext[^"]*?".*?href\s*=\s*\"(?<url>[^"]*?)"[^>]*?>.*?Nedladdningar.*?<br>\s*(?<name>[^<]*?)</td[^>]*?>.*?</tr[^>]*?>"
			//pattern = "<tr[^>]*?>.*?<td[^>]*?>.*?<a[^>]*?alt\\s*=\\s*\"Ladda\\sner\\sundertext[^\"]*?\".*?href\\s*=\\s*\\\"(?<url>[^\\\"]*?)\\\"[^>]*?>.*?Nedladdningar.*?<br>\\s*(?<name>[^<]*?)</td[^>]*?>.*?</tr[^>]*?>";

		}
		catch (IOException e) {
			webResult = StringUtils.EMPTY;
		}
		
		return webResult;	
	}

	protected String getSearchString(Movie m) {
		String searchString;
	
		if (!StringUtils.isEmpty(m.getImdbUrl())) {
			searchString = Util.getImdbIdFromUrl(m.getImdbUrl());
		}
		else {
			searchString = m.getTitle();
		}			
		
		try {
			searchString = java.net.URLEncoder.encode(searchString.trim(), "ISO-8859-1");
		}
		catch (UnsupportedEncodingException e) {
			searchString = StringUtils.EMPTY;
		}
		return searchString;
	}



}
