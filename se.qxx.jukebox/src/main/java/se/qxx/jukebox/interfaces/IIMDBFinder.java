package se.qxx.jukebox.interfaces;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.imdb.IMDBRecord;

public interface IIMDBFinder {

	Movie Get(Movie m) throws IOException, NumberFormatException, ParseException;

	Series Get(Series series, int season, int episode) throws IOException, NumberFormatException, ParseException;

	IMDBRecord Search(String searchString, int yearToFind, List<String> blacklist, boolean isTvEpisode)
			throws IOException, NumberFormatException, ParseException;

}