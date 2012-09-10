package se.qxx.jukebox;

import java.net.URL;

public class WebResult {
	private String url;
	private String result;
	private boolean isRedirected;
	
	public WebResult(URL url, String result, boolean isRedirected) {
		this.setUrl(url);
		this.setResult(result);
		this.setRedirected(isRedirected);
	}
	
	public String getUrl() {
		return url;
	}
	private void setUrl(URL url) {
		this.url = url.toString();
	}
	public String getResult() {
		return result;
	}
	private void setResult(String result) {
		this.result = result;
	}

	public boolean isRedirected() {
		return isRedirected;
	}

	private void setRedirected(boolean isRedirected) {
		this.isRedirected = isRedirected;
	}
	
	

}
