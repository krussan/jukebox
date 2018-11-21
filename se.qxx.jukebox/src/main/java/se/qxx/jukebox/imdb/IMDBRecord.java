package se.qxx.jukebox.imdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.interfaces.IIMDBParser;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.tools.WebResult;
import se.qxx.jukebox.tools.WebRetriever;

public class IMDBRecord {
	private String url = "";
	private int year = 0;
	private int durationMinutes = 0;
	private List<String> genres = new ArrayList<String>();
	private String rating = "";
	private String director = "";
	private String story = "";
	private byte[] image = null;
	private String title = "";
	
	private Map<Integer, String> seasons = new HashMap<Integer, String>();
	private Map<Integer, String> episodes = new HashMap<Integer, String>();
	
	private Date firstAirDate = null;
	private String imageUrl;
	

	public IMDBRecord(String url) {
		this.url = url;
	}

	public static IMDBRecord parse(String url, String webResult, ISettings settings, IMDBParserFactory parserFactory) {
		IMDBRecord rec = new IMDBRecord(url);
		
		Document doc = Jsoup.parse(webResult);
		Log.Debug(String.format("IMDBRECORD :: Initializing parsing"), LogType.IMDB);

		IIMDBParser parser = parserFactory.create(doc);
		rec.setTitle(parser.parseTitle());
		rec.setDirector(parser.parseDirector());
		rec.setDurationMinutes(parser.parseDuration());
		rec.addGenres(parser.parseGenres());
		rec.setFirstAirDate(parser.parseFirstAirDate());
		
		ImageData image = parser.parseImage();
		rec.setImage(image.getData());
		rec.setImageUrl(image.getUrl());
		
		rec.setRating(parser.parseRating());
		rec.setStory(parser.parseStory());
		
		rec.setAllSeasonUrls(parser.parseSeasons());
		rec.setAllEpisodeUrls(parser.parseEpisodes());

		return rec;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBParser#parseEpisodes(org.jsoup.nodes.Document)
	 */
	

	
	public IMDBRecord(String url, int year) {
		this.year = year;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public static IMDBRecord get(String url) throws MalformedURLException {
		String internalUrl = StringUtils.trim(url);

		internalUrl = fixImdbUrl(internalUrl);

		try {
			Log.Debug(String.format("IMDBRECORD :: Making web request to url :: %s", internalUrl), LogType.IMDB);

			WebResult webResult = WebRetriever.getWebResult(internalUrl);
			return getFromWebResult(webResult);
		} catch (Exception e) {
			Log.Error(String.format("Failed to get IMDB information from url :: %s", url), LogType.FIND, e);
		}

		return null;

	}

	

	public static IMDBRecord getFromWebResult(WebResult webResult) {
		IMDBRecord rec = null;
		try {
			rec = IMDBRecord.parse(webResult.getUrl(), webResult.getResult());
		} catch (Exception e) {
			Log.Error(String.format("Failed to get IMDB information from url :: %s", webResult.getUrl()), LogType.FIND,
					e);
		}
		return rec;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getStory() {
		return story;
	}

	public void setStory(String story) {
		this.story = story;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public List<String> getAllGenres() {
		return this.genres;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map<Integer, String> getAllSeasonUrls() {
		return this.seasons;
	}
	
	public void setAllSeasonUrls(Map<Integer, String> seasonMap) {
		this.seasons = seasonMap;
	}

	public Map<Integer, String> getAllEpisodeUrls() {
		return this.episodes;
	}
	
	public void setAllEpisodeUrls(Map<Integer, String> episodeMap) {
		this.episodes = episodeMap;
	}

	public Date getFirstAirDate() {
		return firstAirDate;
	}

	public void setFirstAirDate(Date date) {
		this.firstAirDate = date;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public void addGenres(List<String> genres) {
		this.getAllGenres().addAll(genres);
	}
	
	public void addSeasons(List<String> seasons) {
		this.getE().addAll(genres);
	}
	
	public void addEpisodes(List<String> episodes) {
		this.getAllGenres().addAll(genres);
	}
}
