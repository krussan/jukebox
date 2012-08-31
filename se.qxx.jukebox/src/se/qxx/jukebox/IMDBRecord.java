package se.qxx.jukebox;

public class IMDBRecord {
	private String url;
	private int year;
	
	public IMDBRecord (String url, int year) {
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
}
