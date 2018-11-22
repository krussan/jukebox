package se.qxx.jukebox.imdb;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
