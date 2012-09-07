package se.qxx.jukebox;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import se.qxx.jukebox.Log.LogType;

public class IMDBRecord { 
	private String url;
	private int year;
	private int durationMinutes;
	private List<String> genres = new ArrayList<String>();
	private String rating;
	private String director;
	private String story;
	private byte[] image;
	
	private IMDBRecord(String url) {
		this.url = url;
		
		Pattern p;
		Matcher m;
		
		//TODO: Extract all regex:es to config file in case IMDB decides to change layout
		try {
			String webResult = WebRetriever.getWebResult(url);	

			/*
			// Poster
			try {
				p = Pattern.compile("<img\\s*src=\"([^\"]*)\"[^>]*?alt=\"[^\"]*Poster", Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				m = p.matcher(webResult);
			
				if (m.find()) {
					String posterUrl = m.group(1);
					File f = WebRetriever.getWebFile(posterUrl, Util.getTempDirectory());
					this.setImage(readFile(f));
				}
			}
			catch (Exception e) {
			}
			*/
			
			// Story
			try {
				p = Pattern.compile("<p\\s*itemprop=\"description\">(.*?)</p>", 
						Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				m = p.matcher(webResult);	
				if (m.find())
					this.setStory(m.group(1).trim());
			} catch (Exception e) {
				Log.Error(String.format("IMDBFinder for url %s - unable to set story", url) , LogType.MAIN, e);
			}
		
			try {
				// Year
				p = Pattern.compile("<a\\s*href=\"/year/(\\d{4})/\">",
						Pattern.DOTALL | Pattern.MULTILINE
								| Pattern.CASE_INSENSITIVE);
				m = p.matcher(webResult);
				if (m.find())
					this.year = Integer.parseInt(m.group(1).trim());
			} catch (Exception e) {
				Log.Error(String.format("IMDBFinder for url %s - unable to set year", url) , LogType.MAIN, e);
			}
			
			
			// Rating
			try {
				p = Pattern.compile("<span\\s*itemprop=\"ratingValue\"\\s*>((\\d\\.\\d)|(\\d\\d)|(\\d\\d\\.\\d)|(\\d\\d\\.\\d\\d))</span>", Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				m = p.matcher(webResult);	
				if (m.find())
					this.setRating(m.group(1).trim());
			} catch (Exception e) {
				Log.Error(String.format("IMDBFinder for url %s - unable to set rating", url) , LogType.MAIN, e);
			}
			
			// Genres
			try {
				p = Pattern.compile("href=\"/genre/[^\"]*\"\\s*>(.*?)</a>", Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				m = p.matcher(webResult);	
				while (m.find()) 
					this.genres.add(m.group(1).trim());
			} catch (Exception e) {
				Log.Error(String.format("IMDBFinder for url %s - unable to set genres", url) , LogType.MAIN, e);
			}
			
			// Duration
			try {
				p = Pattern.compile("<time\\s*itemprop=\"duration\".*?>(\\d{1,3})\\s*min</time>", Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				m = p.matcher(webResult);	
				if (m.find()) 
					this.setDurationMinutes(Integer.parseInt(m.group(1).trim()));
			} catch (NumberFormatException e) {
				Log.Error(String.format("IMDBFinder for url %s - unable to set duration", url) , LogType.MAIN, e);
			}
			
			// Director
			try {
				p = Pattern.compile("itemprop=\"director\"\\s*>(.*?)</a>", Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				m = p.matcher(webResult);	
				if (m.find()) 
					this.setDirector(m.group(1).trim());
			} catch (Exception e) {
				Log.Error(String.format("IMDBFinder for url %s - unable to set director", url) , LogType.MAIN, e);
			}
			
		}
		catch (Exception e) {
			Log.Error(String.format("Failed to get IMDB information from url :: %s", url), LogType.FIND, e);
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
		if (!url.startsWith("http://www.imdb.com") && url.startsWith("/")) {
			url = "http://www.imdb.com" + url;
		}
		else {
			throw new MalformedURLException("A IMDB url must start with http://www.imdb.com");
		}
		
		IMDBRecord rec = new IMDBRecord(url);
	
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
	
}
