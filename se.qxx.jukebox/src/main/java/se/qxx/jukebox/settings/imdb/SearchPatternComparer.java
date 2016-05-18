package se.qxx.jukebox.settings.imdb;

import java.util.Comparator;

import se.qxx.jukebox.settings.imdb.Imdb.SearchPatterns.SearchResultPattern;

public class SearchPatternComparer implements Comparator<Imdb.SearchPatterns.SearchResultPattern> {

	@Override
	public int compare(SearchResultPattern arg0, SearchResultPattern arg1) {
		return Integer.valueOf(arg0.getPriority()).compareTo(arg1.getPriority());
	}

}
