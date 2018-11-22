package se.qxx.jukebox.subtitles;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.interfaces.ISubFinder;
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
		this.setClassName("Subscene");
		this.setLanguage(Language.English);
	}
	
	public Subscene(SubFinderSettings subFinderSettings, int minWaitSeconds, int maxWaitSeconds) {
		super(subFinderSettings);
		this.setClassName("Subscene");
		this.setLanguage(Language.English);
		this.setMinWaitSeconds(minWaitSeconds);
		this.setMaxWaitSeconds(maxWaitSeconds);
	}


	@Override
	public List<SubFile> findSubtitles(
			MovieOrSeries mos,
			List<String> languages) {
		
		String searchString = getSearchString(mos.getTitle());
		
		// find list on title
		List<SubFile> listOnTitle = parseSubtitleList(searchString, mos, languages);
		
		// if no match found - try searching on title
		if (!containsMatch(listOnTitle)) {
			searchString = getSearchString(FilenameUtils.getBaseName(mos.getMedia().getFilename()));
			List<SubFile> listOnFilename = parseSubtitleList(searchString, mos, languages);
			
			return performDownload(mos, listOnFilename);
		}
		
		return performDownload(mos, listOnTitle);

	}
	
	public List<SubFile> parseSubtitleList(
			String searchString,
			MovieOrSeries mos,
			List<String> languages) {

		List<SubFile> files = new ArrayList<SubFile>(); 
		
		if (!StringUtils.isEmpty(searchString)) {
			String url = this.getSetting(SETTING_URL).replaceAll("__searchString__", searchString);
			String baseUrl = getBaseUrl(url);

			Log.Debug(String.format("%s :: searchUrl :: %s", this.getClassName(), url), LogType.SUBS);
			String webResult = performSearch(url);
			
			//Get the first result 
			url = getMatchingResult(webResult);
			Log.Debug(String.format("Matching url :: %s", url), LogType.SUBS);
			
			if (!StringUtils.isEmpty(url)) {
				url = getFullUrl(url, baseUrl);
				
				// Get the subfiles from the underlying web result
				webResult = performSearch(url);
			}
			else {
				//We could have been redirected directly to the list (!)
				// continue and hope for match in the next phase
			}
				
			// Now we have a list of subs. But each download link is hidden one step below.
			// The list is enough to rate the list at least.
			if (!StringUtils.isEmpty(webResult)) {
				files = collectSubFiles(
						mos, 
						webResult, 
						this.getSetting(SETTING_LISTRESULT_REGEX),
						Integer.parseInt(this.getSetting(SETTING_LISTRESULT_URLGROUP)),
						Integer.parseInt(this.getSetting(SETTING_LISTRESULT_NAMEGROUP)),
						Integer.parseInt(this.getSetting(SETTING_LISTRESULT_LANGUAGEGROUP)));

			}
		}
		
		return files;
	}
	
	public List<SubFile> performDownload(MovieOrSeries mos, List<SubFile> listSubs) {
		String url = this.getSetting(SETTING_URL);		
		String baseUrl = getBaseUrl(url);
		
		// We need to replace the download links in each and every subfile
		// We get an error because listsubs is null
		listSubs = replaceDownloadLinks(listSubs, baseUrl);
		
		return downloadSubs(mos, listSubs);
	}
	
	private String getBaseUrl(String url) {
		URL fullUrl;
		String result = url;
		try {
			fullUrl = new URL(url);
			result = String.format("%s://%s", fullUrl.getProtocol(), fullUrl.getHost());
		} catch (MalformedURLException e) {
			Log.Error("Illegal url in subfinder", LogType.SUBS);
		}
		
		return result;
	}
	
	/***
	 * Expects a sorted list of subs
	 * @param listSubs
	 * @return
	 */
	private List<SubFile> replaceDownloadLinks(List<SubFile> listSubs, String baseUrl) {
		for (SubFile sf : listSubs) {
			
			String url = getFullUrl(sf.getUrl(), baseUrl);
			
			String webResult = performSearch(url);
			
			Pattern p = Pattern.compile(this.getSetting(SETTING_DOWNLOAD_REGEX), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
			Matcher matcher = p.matcher(webResult);
			
			if (matcher.find()){
				String foundUrl = matcher.group(Integer.parseInt(this.getSetting(SETTING_DOWNLOAD_URLGROUP)));
				sf.setUrl(getFullUrl(foundUrl, baseUrl));
			}
		}
		
		return listSubs;
	}

	private String getMatchingResult(String webResult) {
		Pattern p = Pattern.compile(this.getSetting(SETTING_SEARCHRESULT_REGEX), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
		Matcher matcher = p.matcher(webResult);

		// we take the first one... 
		if (matcher.find()) {
			return matcher.group(Integer.parseInt(this.getSetting(SETTING_SEARCHRESULT_URLGROUP)));
		}
		
		return StringUtils.EMPTY;
	}

	protected String getSearchString(String title) {
		String searchString = title;
		
		try {
			searchString = java.net.URLEncoder.encode(searchString.trim(), "ISO-8859-1");
		}
		catch (UnsupportedEncodingException e) {
			searchString = StringUtils.EMPTY;
		}
		return searchString;
	}
	
	private String getFullUrl(String url, String baseUrl) {
		if (!url.startsWith("http"))
			return baseUrl + (url.startsWith("/") ? "" : "/") + url;
		else
			return url;
	}

}
