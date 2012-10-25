package se.qxx.jukebox;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.text.html.HTML;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.Imdb.InfoPatterns.InfoPattern;

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
	
	private IMDBRecord() {
	}
	
	private IMDBRecord(String url) {
		this.url = url;
		
		
		//TODO: Extract all regex:es to config file in case IMDB decides to change layout
		try {
			String webResult = WebRetriever.getWebResult(url).getResult();
			parse(webResult);
			
		}
		catch (Exception e) {
			Log.Error(String.format("Failed to get IMDB information from url :: %s", url), LogType.FIND, e);
		}		
		
	}
	
	private void parse(String webResult) {
		Pattern p;
		Matcher m;

		List<InfoPattern> patterns = Settings.imdb().getInfoPatterns().getInfoPattern();
		for (InfoPattern infoPattern : patterns) {
			p = Pattern.compile(
				  infoPattern.getRegex()
				, Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
			m = p.matcher(webResult);
			
			if (m.find()) {
				try {
					String value = StringUtils.trim(m.group(infoPattern.getGroup()));
					String unescapedValue = StringEscapeUtils.unescapeHtml4(value);
					
					switch (infoPattern.getType()) {
					case TITLE:
						this.setTitle(unescapedValue);
						break;
					case DIRECTOR:
						this.setDirector(unescapedValue);
						break;
					case DURATION:
						this.setDurationMinutes(Integer.parseInt(unescapedValue));
						break;
					case GENRES:
						this.genres.add(unescapedValue);					
						while (m.find()) {
							value = StringUtils.trim(m.group(infoPattern.getGroup()));
							unescapedValue = StringEscapeUtils.unescapeHtml4(value);
							
							this.genres.add(unescapedValue);					
						}
						
						break;
					case POSTER:
						File f = WebRetriever.getWebFile(value, Util.getTempDirectory());
						this.setImage(readFile(f));
						
						f.delete();
						break;
					case RATING:
						this.setRating(unescapedValue);
						break;
					case STORY:
						this.setStory(unescapedValue);
						break;		
					case YEAR:
						this.setYear(Integer.parseInt(unescapedValue));
						break;
					}
				}
				catch (Exception e) {
					Log.Error(String.format("IMDBFinder for url %s - unable to set %s", url, infoPattern.getType().toString()), LogType.MAIN, e);					
				}
			}
		}
	}
	
	private byte[] readFile(File f) {
		try {
			FileInputStream fs = new FileInputStream(f);
			long length = f.length();
			if (length > Integer.MAX_VALUE)
				throw new ArrayIndexOutOfBoundsException();
			
			byte[] data = new byte[(int)length];
			int offset = 0;
			int numRead = 0;
			while (offset < data.length && (numRead = fs.read(data, offset, data.length - offset)) >= 0) {
				offset += numRead;
			}
			
		    // Ensure all the bytes have been read in
		    if (offset < data.length) {
		        throw new IOException("Could not completely read file " + f.getName());
		    }
		    
		    // Close the input stream and return bytes
		    fs.close();
		    return data;	
		}
		catch (Exception e) {
			Log.Error("Error when reading file", LogType.FIND, e);
			return null;
		}

    }
	
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
	
	public static IMDBRecord get(String url) throws MalformedURLException {
		String internalUrl = StringUtils.trim(url);
		
		if (StringUtils.startsWithIgnoreCase(internalUrl, "www.imdb.com"))
			internalUrl = "http://" + internalUrl;
		
		if (StringUtils.startsWithIgnoreCase(internalUrl, "/"))
			internalUrl = "http://www.imdb.com" + internalUrl;

		if (!StringUtils.startsWithIgnoreCase(internalUrl, "http://www.imdb.com"))
			throw new MalformedURLException(String.format("A IMDB url must start with http://www.imdb.com. Url was :: %s", internalUrl));
				
		IMDBRecord rec = new IMDBRecord(internalUrl);
	
		return rec;
	}
	
	public static IMDBRecord getFromWebResult(WebResult webResult) {
		IMDBRecord rec = new IMDBRecord();
		rec.url = webResult.getUrl();
		
		try {
			rec.parse(webResult.getResult());
		}
		catch (Exception e) {
			Log.Error(String.format("Failed to get IMDB information from url :: %s", webResult.getUrl()), LogType.FIND, e);
		}
		return rec;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	private void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public String getRating() {
		return rating;
	}

	private void setRating(String rating) {
		this.rating = rating;
	}

	public String getDirector() {
		return director;
	}

	private void setDirector(String director) {
		this.director = director;
	}

	public String getStory() {
		return story;
	}

	private void setStory(String story) {
		this.story = story;
	}

	public byte[] getImage() {
		return image;
	}

	private void setImage(byte[] image) {
		this.image = image;
	}
	
	public List<String> getAllGenres() {
		return this.genres;
	}

	public String getTitle() {
		return title;
	}

	private void setTitle(String title) {
		this.title = title;
	}
}
