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
	
	private final Pattern seasonPattern = Pattern.compile("season\\=(\\d*)");
	private final Pattern episodePattern = Pattern.compile("ttep_ep(\\d*)");

	public IMDBRecord(String url) {
		this.url = url;
	}

	public static IMDBRecord parse(String url, String webResult, ISettings settings, IIMDBParser parser) {
		IMDBRecord rec = new IMDBRecord(url);
		
		Document doc = Jsoup.parse(webResult);
		Log.Debug(String.format("IMDBRECORD :: Initializing parsing"), LogType.IMDB);

		rec.parseTitle(doc);
		rec.parseDirector(doc);
		rec.parseDuration(doc);
		rec.parseGenres(doc);
		rec.parseFirstAirDate(doc, settings);
		rec.parseImage(doc);
		rec.parseRating(doc);
		rec.parseStory(doc);
		rec.parseSeasons(doc);
		rec.parseEpisodes(doc);

		return rec;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBParser#parseEpisodes(org.jsoup.nodes.Document)
	 */
	

	public void addEpisodeUrl(String url) {
		Matcher m = episodePattern.matcher(url);
		if (m.find()) {
			int episode = Integer.parseInt(m.group(1));
			this.getAllEpisodeUrls().put(episode, url);
		}
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBParser#parseSeasons(org.jsoup.nodes.Document)
	 */
	

	public void addSeasonUrl(String url) {		
		Matcher m = seasonPattern.matcher(url);
		if (m.find()) {
			int season = Integer.parseInt(m.group(1));
			this.getAllSeasonUrls().put(season, url);
		}
	}



	

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBParser#parseFirstAirDate(org.jsoup.nodes.Document, se.qxx.jukebox.interfaces.ISettings)
	 */

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBParser#parseGenres(org.jsoup.nodes.Document)
	 */

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBParser#parseDuration(org.jsoup.nodes.Document)
	 */

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBParser#parseDirector(org.jsoup.nodes.Document)
	 */

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.imdb.IIMDBParser#parseTitle(org.jsoup.nodes.Document)
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

	public Map<Integer, String> getAllEpisodeUrls() {
		return this.episodes;
	}

	public Date getFirstAirDate() {
		return firstAirDate;
	}

	public void setFirstAirDate(String firstAirDate, ISettings settings) {
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
