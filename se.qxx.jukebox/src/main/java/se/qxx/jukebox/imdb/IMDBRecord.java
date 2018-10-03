package se.qxx.jukebox.imdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.Imdb.InfoPatterns.InfoPattern;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.tools.WebResult;
import se.qxx.jukebox.tools.WebRetriever;

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
	private List<String> seasons = new ArrayList<String>();
	private Date firstAirDate = null;
	private String imageUrl;
	
	private IMDBRecord() {
	}
	
	public static IMDBRecord parse(String webResult) {
		Pattern p;
		Matcher m;
		
		IMDBRecord rec = new IMDBRecord();
		Document doc = Jsoup.parse(webResult);
		Log.Debug(String.format("IMDBRECORD :: Initializing parsing"), LogType.IMDB);
		
		rec.parseTitle(doc);
		rec.parseDirector(doc);
		rec.parseDuration(doc);
		rec.parseGenres(doc);
		rec.parseFirstAirDate(doc);
		rec.parseImage(doc);
		
		return rec;
	}

	private void parseImage(Document doc) {
		Elements elm = doc.select(".poster img");
		
		if (elm.size() > 0) {
			String value = elm.attr("src").trim();
			File f;
			try {
				f = WebRetriever.getWebFile(value, Util.getTempDirectory());
				this.setImageUrl(value);;
				this.setImage(readFile(f));

			} catch (IOException e) {
				Log.Error("Error when downloading file", LogType.IMDB);
			}
			
		}
	}

	private void parseFirstAirDate(Document doc) {
		Elements elm = doc.select(".title_wrapper a[href~=/.*releaseinfo.*]");
		if (elm.size() > 0) {
			Pattern p = Pattern.compile("(.*?)\\((.*)\\)");
			Matcher m = p.matcher(elm.text());
			
			String dateValue = elm.get(0).text().trim();
			if (m.find()) {
				dateValue = m.group(1).trim();
			}
			
			this.setFirstAirDate(dateValue);
		}
	}

	private void parseGenres(Document doc) {
		Elements elm = doc.select(".title_wrapper a[href~=/genre.*]");
		for (int i = 0; i < elm.size(); i++) {
			String genre = StringEscapeUtils.unescapeHtml4(elm.get(i).text()).trim();
			this.getAllGenres().add(genre);
		}
	}

	private void parseDuration(Document doc) {
		Elements elm = doc.select(".title_wrapper time");

		if (elm.size() > 0) {
			String duration = elm.get(0).attr("datetime");
			Duration dur = Duration.parse(duration);
			int minutes = (int) (dur.getSeconds() / 60);

			Log.Debug(String.format("IMDBRECORD :: Setting duration :: %s", minutes), LogType.IMDB);
			this.setDurationMinutes(minutes);
		}
	}

	private void parseDirector(Document doc) {
		Elements elm = doc.select(".credit_summary_item:contains(Director) > a");
		
		if (elm.size() > 0) {
			String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.get(0).text());				
			Log.Debug(String.format("IMDBRECORD :: Setting director :: %s", unescapedValue), LogType.IMDB);
			this.setDirector(unescapedValue);
		}
	}

	private void parseTitle(Document doc) {
		Elements elm = doc.select(".title_wrapper > h1");
		if (elm.size() > 0) {		
			Pattern p = Pattern.compile("(.*?)\\((\\d{4})\\)");
			Matcher m = p.matcher(elm.text());
		
			if (m.find()) {
				String title = m.group(1);
				String year = m.group(2);
			
				String unescapedValue = StringEscapeUtils.unescapeHtml4(title).trim();
				Log.Debug(String.format("IMDBRECORD :: Setting title :: %s", unescapedValue), LogType.IMDB);
				this.setTitle(unescapedValue);
				
				unescapedValue = StringEscapeUtils.unescapeHtml4(year).trim();
				Log.Debug(String.format("IMDBRECORD :: Setting title :: %s", unescapedValue), LogType.IMDB);
				this.setYear(Integer.parseInt(unescapedValue));
			}
			else {
				String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.text()).trim();
				Log.Debug(String.format("IMDBRECORD :: Setting title :: %s", unescapedValue), LogType.IMDB);
				this.setTitle(unescapedValue);
			}
		}
	}
	
	private static byte[] readFile(File f) {
		try {
			FileInputStream fs = new FileInputStream(f);
			long length = f.length();
			if (length > Integer.MAX_VALUE) {
				fs.close();
				throw new ArrayIndexOutOfBoundsException();
			}			
			byte[] data = new byte[(int)length];
			int offset = 0;
			int numRead = 0;
			while (offset < data.length && (numRead = fs.read(data, offset, data.length - offset)) >= 0) {
				offset += numRead;
			}
			
		    // Ensure all the bytes have been read in
		    if (offset < data.length) {
		    	fs.close();
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
						
		try {
			Log.Debug(String.format("IMDBRECORD :: Making web request to url :: %s", internalUrl), LogType.IMDB);
	
			WebResult webResult = WebRetriever.getWebResult(internalUrl);
			return getFromWebResult(webResult);
		}
		catch (Exception e) {
			Log.Error(String.format("Failed to get IMDB information from url :: %s", url), LogType.FIND, e);
		}		
		
		return null;
		
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
	
	public List<String> getAllSeasonUrls() {
		return this.seasons;
	}

	public Date getFirstAirDate() {
		return firstAirDate;
	}

	public void setFirstAirDate(String firstAirDate) {
		try {
			Log.Debug(String.format("IMDB :: parsing date :: %s", firstAirDate), LogType.IMDB);
			this.firstAirDate = DateUtils.parseDate(firstAirDate, Settings.imdb().getDatePatterns().getPattern().toArray(new String[]{}));
			Log.Debug(String.format("IMDB :: parsed date :: %s", this.firstAirDate), LogType.IMDB);
		} catch (ParseException e) {
			Log.Error(String.format("IMDB :: Unable to parse date :: %s", firstAirDate), LogType.IMDB, e);
		}
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
