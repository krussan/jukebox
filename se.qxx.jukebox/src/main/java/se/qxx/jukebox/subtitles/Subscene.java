package se.qxx.jukebox.subtitles;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Language;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;

public class Subscene extends SubFinderBase {

	private final String SETTING_URL = "url";
	private final String SETTING_SEARCHRESULT_REGEX = "searchResultRegex";
	private final String SETTING_SEARCHRESULT_URLGROUP = "searchResultUrlGroup";
	private final String SETTING_LISTRESULT_REGEX = "listResultRegex";
	private final String SETTING_LISTRESULT_URLGROUP = "searchResultUrlGroup";
	private final String SETTING_LISTRESULT_NAMEGROUP = "listResultNameGroup";
	
	private final String SETTING_LISTRESULT_LANGUAGEGROUP = "listResultLanguageGroup";
	private final String SETTING_DOWNLOAD_REGEX = "downloadUrlRegex";
	private final String SETTING_DOWNLOAD_URLGROUP = "downloadUrlGroup";
	
	
	public Subscene(SubFinderSettings subFinderSettings) {
		super(subFinderSettings);
		this.setClassName("Subscene");
		this.setLanguage(Language.English);
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
			
			//Get the first result 
			url = getMatchingResult(m, webResult);
			
			if (!StringUtils.isEmpty(url)) {
				// Get the subfiles from the underlying web result
				webResult = performSearch(url);
				
				// Now we have a list of subs. But each download link is hidden one step below.
				// The list is enough to rate the list at least.
					
				if (!StringUtils.isEmpty(webResult)) {

					List<SubFile> listSubs = collectSubFiles(
							m, 
							webResult, 
							this.getSetting(SETTING_LISTRESULT_REGEX),
							Integer.parseInt(this.getSetting(SETTING_LISTRESULT_URLGROUP)),
							Integer.parseInt(this.getSetting(SETTING_LISTRESULT_NAMEGROUP)),
							Integer.parseInt(this.getSetting(SETTING_LISTRESULT_LANGUAGEGROUP)));
					
					// We need to replace the download links in each and every subfile
					listSubs = replaceDownloadLinks(listSubs);
					
					files = downloadSubs(m, listSubs);
				}
			
			}

		}
		
		return files;
	}
	
	/***
	 * Expects a sorted list of subs
	 * @param listSubs
	 * @return
	 */
	private List<SubFile> replaceDownloadLinks(List<SubFile> listSubs) {
		for (SubFile sf : listSubs) {
			
			String url = sf.getUrl();
			String webResult = performSearch(url);
			
			Pattern p = Pattern.compile(this.getSetting(SETTING_DOWNLOAD_REGEX), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
			Matcher matcher = p.matcher(webResult);
			
			if (matcher.find())
				sf.setUrl(matcher.group(Integer.parseInt(this.getSetting(SETTING_DOWNLOAD_URLGROUP))));
			
			// break if enough matches found
			if (sf.getRating() == Rating.ExactMatch || sf.getRating() == Rating.PositiveMatch)
				break;
		}
		
		return listSubs;
	}

	private String getMatchingResult(Movie m, String webResult) {
		Pattern p = Pattern.compile(this.getSetting(SETTING_SEARCHRESULT_REGEX), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		Matcher matcher = p.matcher(webResult);

		// we take the first one... 
		if (matcher.find()) {
			return matcher.group(Integer.parseInt(this.getSetting(SETTING_SEARCHRESULT_URLGROUP)));
		}
		
		return StringUtils.EMPTY;
	}

	protected String getSearchString(Movie m) {
		String searchString = m.getTitle();
		
		try {
			searchString = java.net.URLEncoder.encode(searchString.trim(), "ISO-8859-1");
		}
		catch (UnsupportedEncodingException e) {
			searchString = StringUtils.EMPTY;
		}
		return searchString;
	}

}
