package se.qxx.jukebox.subtitles;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;

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
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.tools.WebRetriever;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;


public class UndertexterSe extends SubFinderBase {
	
	
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
				List<SubFile> listSubs = collectSubFiles(
						m, 
						webResult, 
						this.getSetting(SETTING_PATTERN),
						Integer.parseInt(this.getSetting("urlRegexGroup")),
						Integer.parseInt(this.getSetting("nameRegexGroup")),
						0);
				
				files = downloadSubs(m, listSubs);
			}
		}
		
		return files;
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
