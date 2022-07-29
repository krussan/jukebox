package se.qxx.jukebox.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Map;

import se.qxx.jukebox.imdb.IMDBRecord;

public interface IIMDBParser {

	Map<Integer, String> parseEpisodes();
	Map<Integer, String> parseSeasons();
	String parseStory();
	String parseRating();
	List<String> parseGenres();
	int parseDuration();
	String parseDirector();
	String parseTitle();
	int parseYear();
	Date parseFirstAirDate();
	String parseImageUrl();
	
    IMDBRecord parse(String url, boolean ignoreJson);
}