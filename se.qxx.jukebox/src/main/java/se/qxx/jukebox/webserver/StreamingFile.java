package se.qxx.jukebox.webserver;

public class StreamingFile {

	private String uri;
	private String mimeType;
	
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

	public StreamingFile(String uri, String mimeType) {
		this.setUri(uri);
		this.setMimeType(mimeType);
	}
}
