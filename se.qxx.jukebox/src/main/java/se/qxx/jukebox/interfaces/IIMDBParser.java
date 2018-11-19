package se.qxx.jukebox.interfaces;

import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Document;

import se.qxx.jukebox.imdb.ImageData;

public interface IIMDBParser {

	List<String> parseEpisodes(Document doc);

	List<String> parseSeasons(Document doc);

	String parseStory(Document doc);

	String parseRating(Document doc);

	ImageData parseImage(Document doc);

	void parseGenres(Document doc);

	void parseDuration(Document doc);

	void parseDirector(Document doc);

	void parseTitle(Document doc);

	Date parseFirstAirDate(Document doc);

}