package se.qxx.jukebox.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.interfaces.IWebRetriever;

@Singleton
public class WebRetriever implements IWebRetriever {
	
	@Inject
	public WebRetriever() {
		
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.tools.IWebRetriever#getWebResult(java.lang.String)
	 */
	@Override
	public WebResult getWebResult(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection(); 
		httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
		httpcon.addRequestProperty("Accept-Language", "en-US");
		
		String result = Util.readMessageFromStream(httpcon.getInputStream());
	
		WebResult res = new WebResult(httpcon.getURL(), result, !url.toString().equals(httpcon.getURL().toString()));
		
		httpcon.disconnect();
		httpcon = null;
				
		return res;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.tools.IWebRetriever#getWebFile(java.lang.String, java.lang.String)
	 */
	@Override
	public File getWebFile(String urlString, String savePath) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
		httpcon.setInstanceFollowRedirects(false);
		
		httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
		
		httpcon.connect();
		
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
						
						Log.Error("Error when parsing redirect url", Log.LogType.MAIN, e);
					}
				}
				
				
				
				httpcon = (HttpURLConnection) url.openConnection(); 
			}
			else {
				found = true;
			}
		}
	
		
		String contentDisposition = httpcon.getHeaderField("content-disposition");
		String filename = url.getPath().substring(url.getPath().lastIndexOf("/") + 1, url.getPath().length());
		
		Log.Debug(String.format("Content-Disposition :: %s", contentDisposition), LogType.MAIN);
		
		if (!StringUtils.isEmpty(contentDisposition)) {
			Pattern p = Pattern.compile("filename=\"?(.*?)\"?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
			Matcher m = p.matcher(contentDisposition);
			
			if (m.find()) 
				filename = m.group(1);
		}
		String filenameAndPath = String.format("%s/%s", savePath, filename);
		
		File f = new File(filenameAndPath);
		InputStream is = httpcon.getInputStream();
		FileOutputStream os = new FileOutputStream(f);
		
        int len;
        byte[] buffer = new byte[4096];
        
        while (-1 != (len = is.read(buffer)))
          os.write(buffer, 0, len);
		        
        os.flush();
        os.close();
        is.close();
		
		httpcon.disconnect();
		httpcon = null;
		
		return f;
	}	
}
