package se.qxx.jukebox;

import java.util.Date;

public class IMDBEpisode {
	private Date firstAirDate;
	private String url;
	private int episodeNumber;
	private String title;
	
	public Date getFirstAirDate() {
		return firstAirDate;
	}
	public void setFirstAirDate(Date firstAirDate) {
		this.firstAirDate = firstAirDate;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getEpisodeNumber() {
		return episodeNumber;
	}
	public void setEpisodeNumber(int episodeNumber) {
		this.episodeNumber = episodeNumber;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public IMDBEpisode(String url, int episodeNumber, String title, Date firstAirDate) {
		this.setEpisodeNumber(episodeNumber);
		this.setFirstAirDate(firstAirDate);
		this.setTitle(title);
		this.setUrl(url);
	}
	
}
