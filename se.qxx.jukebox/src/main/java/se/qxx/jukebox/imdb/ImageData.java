package se.qxx.jukebox.imdb;

public class ImageData {
	private byte[] data;
	private String url;
	
	public ImageData(String url, byte[] data) {
		this.data = data;
		this.url = url;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
