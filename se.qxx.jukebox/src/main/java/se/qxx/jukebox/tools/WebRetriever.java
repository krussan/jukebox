package se.qxx.jukebox.tools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.interfaces.IWebRetriever;

@Singleton
public class WebRetriever implements IWebRetriever {

	public static final String USER_AGENT = "Mozilla/5.0";
	private IJukeboxLogger log;
	private IUtils utils;
	
	@Inject
	public WebRetriever(LoggerFactory loggerFactory, IUtils utils) {
		this.setUtils(utils);
		this.setLog(loggerFactory.create(LogType.MAIN));
	}
	
	public IUtils getUtils() {
		return utils;
	}

	public void setUtils(IUtils utils) {
		this.utils = utils;
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.tools.IWebRetriever#getWebResult(java.lang.String)
	 */
	@Override
	public WebResult getWebResult(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection(); 
		httpcon.addRequestProperty("User-Agent", USER_AGENT);
		httpcon.addRequestProperty("Accept-Language", "en-US");
		
		String result = this.getUtils().readMessageFromStream(httpcon.getInputStream());
	
		WebResult res = new WebResult(httpcon.getURL(), result, !url.toString().equals(httpcon.getURL().toString()));
		
		httpcon.disconnect();

		return res;
	}

	@Override
	public WebResult postWebResult(String baseUrl, String query) throws IOException {
		URL url = new URL(baseUrl);

		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
		httpcon.setRequestMethod("POST");
		httpcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		httpcon.setDoOutput(true);
		httpcon.addRequestProperty("User-Agent", USER_AGENT);
		httpcon.addRequestProperty("Accept-Language", "en-US");

		byte[] postData = query.getBytes(StandardCharsets.ISO_8859_1);
		try (var wr = new DataOutputStream(httpcon.getOutputStream())) {
			wr.write(postData);
		}

		String result = this.getUtils().readMessageFromStream(httpcon.getInputStream());

		WebResult res = new WebResult(httpcon.getURL(), result, !url.toString().equals(httpcon.getURL().toString()));

		httpcon.disconnect();

		return res;
	}

	@Override
	public File getWebFile(String urlString, String savePath) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection httpcon = setupHttpConnection(url);
		url = handleRedirect(httpcon, url);
			
		String filenameAndPath = getFilename(savePath, httpcon);
		File f = new File(filenameAndPath);

		readWebData(httpcon, new FileOutputStream(f));
		
		httpcon.disconnect();
		httpcon = null;
		
		return f;
	}

	private void readWebData(HttpURLConnection httpcon, OutputStream os) throws IOException {
		InputStream is = httpcon.getInputStream();

		IOUtils.copy(is, os);
		        
        os.flush();
        os.close();
        is.close();
	}

	private HttpURLConnection setupHttpConnection(URL url) throws IOException {
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
		httpcon.setInstanceFollowRedirects(false);
		httpcon.addRequestProperty("User-Agent", USER_AGENT);
		httpcon.connect();
		return httpcon;
	}

	private String getFilename(String savePath, HttpURLConnection httpcon) {
		URL url = httpcon.getURL();
		String contentDisposition = httpcon.getHeaderField("content-disposition");
		String filename = url.getPath().substring(url.getPath().lastIndexOf("/") + 1, url.getPath().length());
		
		this.getLog().Debug(String.format("Content-Disposition :: %s", contentDisposition));
		
		if (!StringUtils.isEmpty(contentDisposition)) {
			Pattern p = Pattern.compile("filename=\"?(.*?)\"?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
			Matcher m = p.matcher(contentDisposition);
			
			if (m.find()) 
				filename = m.group(1);
		}
		String filenameAndPath = String.format("%s/%s", savePath, filename);
		return filenameAndPath;
	}	
	
	private URL handleRedirect(HttpURLConnection httpcon, URL url) throws IOException {
		boolean found = false;
		int code;
		String newUrl;
		
		while (!found) {
			code = httpcon.getResponseCode();

			if (code == 302) {
				newUrl = httpcon.getHeaderField("Location");
				httpcon.disconnect();
				
				String regex = "^(http|s):\\/\\/([^\\/]*)(.*)$";
				java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
				java.util.regex.Matcher m = p.matcher(newUrl);
				if (m.matches()) {
					try {
						URI uri = new URI(m.group(1), m.group(2), m.group(3), "");
						
						url = uri.toURL();

					} catch (URISyntaxException e) {
						
						this.getLog().Error("Error when parsing redirect url", e);
					}
				}
				
				
				
				httpcon = (HttpURLConnection) url.openConnection(); 
			}
			else {
				found = true;
			}
		}
		
		return url;
	}

	@Override
	public byte[] getWebFileData(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection httpcon = setupHttpConnection(url);
		url = handleRedirect(httpcon, url);
			
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		readWebData(httpcon, bos);
		
		httpcon.disconnect();
		httpcon = null;
		
		return bos.toByteArray();
	}
}
