package se.qxx.jukebox;

public class IMDBTitle {
	private String title;
	private String country;
	
	public String getTitle() {
		return title;
	}
	private void setTitle(String title) {
		this.title = title;
	}
	public String getCountry() {
		return country;
	}
	private void setCountry(String country) {
		this.country = country;
	}
	
	public IMDBTitle(String title, String country) {
		this.setTitle(title);
		this.setCountry(country);
	}
	
}
