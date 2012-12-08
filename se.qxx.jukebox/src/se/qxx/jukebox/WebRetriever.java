package se.qxx.jukebox;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;

public class WebRetriever {
	
	public static WebResult getWebResult(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection(); 
		httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
		
		String result = Util.readMessageFromStream(httpcon.getInputStream());
        
//		Log.Info(String.format("1st url :: %s", url.toString()), LogType.MAIN);
//		Log.Info(String.format("2nd url :: %s", httpcon.getURL().toString()), LogType.MAIN);
		
		WebResult res = new WebResult(httpcon.getURL(), result, !url.toString().equals(httpcon.getURL().toString()));
		
		httpcon.disconnect();
		httpcon = null;
				
		return res;
	}
	
	public static File getWebFile(String urlString, String savePath) throws IOException {
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
			//Log.Debug(httpcon.getResponseMessage(), Log.LogType.MAIN);
			
			if (code == 302) {
				newUrl = httpcon.getHeaderField("Location");
				httpcon.disconnect();
				
				//String regex = "^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$";
				String regex = "^(http|s):\\/\\/([^\\/]*)(.*)$";
				java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
				java.util.regex.Matcher m = p.matcher(newUrl);
				if (m.matches()) {
					try {
						URI uri = new URI(m.group(1), m.group(2), m.group(3), "");
						
						//newUrl = m.group(1) + "://" + m.group(2) + URLEncoder.encode(m.group(3), "iso-8859-1");
						//Log.Debug(m.group(2));

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
		
		if (!StringUtils.isEmpty(contentDisposition)) {
			//attachment; filename="Catch..44.2011.BRRip.XviD.AC3-FTW._www.ENGSUB.NET.zip"
			Pattern p = Pattern.compile("filename=\"([^\"]*)\"");
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
