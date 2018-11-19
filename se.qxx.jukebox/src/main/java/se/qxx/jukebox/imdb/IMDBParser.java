package se.qxx.jukebox.imdb;

import java.io.File;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.inject.Inject;

import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.interfaces.IFileReader;
import se.qxx.jukebox.interfaces.IIMDBParser;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.tools.Util;
import se.qxx.jukebox.tools.WebRetriever;

public class IMDBParser implements IIMDBParser {
	private IFileReader fileReader;
	private ISettings settings;

	@Inject
	public IMDBParser(IFileReader fileReader, ISettings settings) {
		this.setFileReader(fileReader);
		this.setSettings(settings);
	}
	
	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	public IFileReader getFileReader() {
		return fileReader;
	}
	public void setFileReader(IFileReader fileReader) {
		this.fileReader = fileReader;
	}
	@Override
	public List<String> parseEpisodes(Document doc) {
		Elements elm = doc.select("strong > a[href~=ttep_ep]");
		
		List<String> result = new ArrayList<String>();
		for (Element e : elm) {
			String url = StringUtils.EMPTY;
			try {
				url = fixImdbUrl(e.attr("href"));
			} catch (MalformedURLException e1) {
				Log.Error("Error parsing imdb url for season", LogType.IMDB);
			}				
			Log.Debug(String.format("IMDBRECORD :: Setting episode url :: %s", url), LogType.IMDB);
			result.add(url);
		}
		
		return result;
	}

	@Override
	public List<String> parseSeasons(Document doc) {
		Elements elm = doc.select(".seasons-and-year-nav a[href~=season]");
		
		List<String> result = new ArrayList<String>();
		for (Element e : elm) {
			String url = StringUtils.EMPTY;
			try {
				url = fixImdbUrl(e.attr("href"));
			} catch (MalformedURLException e1) {
				Log.Error("Error parsing imdb url for season", LogType.IMDB);
			}				
			Log.Debug(String.format("IMDBRECORD :: Setting season url :: %s", url), LogType.IMDB);
			result.add(url);
		}
		
		return result;

	}

	@Override
	public String parseStory(Document doc) {
		Elements elm = doc.select("div.summary_text");

		if (elm.size() > 0) {
			String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.get(0).text());
			Log.Debug(String.format("IMDBRECORD :: Setting story :: %s", unescapedValue), LogType.IMDB);
			return unescapedValue;
		}
		
		return StringUtils.EMPTY;
	}


	@Override
	public String parseRating(Document doc) {
		Elements elm = doc.select("span[itemprop=ratingValue]");

		if (elm.size() > 0) {
			String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.get(0).text());
			Log.Debug(String.format("IMDBRECORD :: Setting rating :: %s", unescapedValue), LogType.IMDB);
			return unescapedValue;
		}
		
		return StringUtils.EMPTY;
	}

	@Override
	public ImageData parseImage(Document doc) {
		Elements elm = doc.select(".poster img");

		if (elm.size() > 0) {
			String value = elm.attr("src").trim();
			File f;
			try {
				f = WebRetriever.getWebFile(value, Util.getTempDirectory());
				byte[] data = this.getFileReader().readFile(f);
				
				Log.Debug(String.format("IMDBRECORD :: Setting image url :: %s", value), LogType.IMDB);
				Log.Debug(String.format("IMDBRECORD :: Setting image (length) :: %s", data.length), LogType.IMDB);
				
				
				return new ImageData(value, data);

			} catch (IOException e) {
				Log.Error("Error when downloading file", LogType.IMDB);
			}

		}
		
		return null;
	}

	@Override
	public Date parseFirstAirDate(Document doc) {
		Elements elm = doc.select(".title_wrapper a[href~=/.*releaseinfo.*]");
		if (elm.size() > 0) {
			String dateValue = fetchAirDateByParenthesis(elm);
			
			if (StringUtils.isEmpty(dateValue))
				dateValue = fetchAirDateByEpisodeAired(elm);
			
			if (StringUtils.isNotEmpty(dateValue)) {
				Log.Debug(String.format("IMDBRECORD :: Setting first air date :: %s", dateValue), LogType.IMDB);
				return parseDate(dateValue);
			}
		}
		
		return null;
	}
	
	public String fetchAirDateByEpisodeAired(Elements elm) {
		Pattern p = Pattern.compile("Episode\\s*aired\\s*");
		Matcher m = p.matcher(elm.text());
		if (m.find()) {
			return m.replaceFirst("");	
		}
		
		return StringUtils.EMPTY;
	}

	public String fetchAirDateByParenthesis(Elements elm) {
		Pattern p = Pattern.compile("(.*?)\\((.*)\\)");
		Matcher m = p.matcher(elm.text());

		if (m.find()) {
			return m.group(1).trim();
		}
		return StringUtils.EMPTY;
	}

	@Override
	public List<String> parseGenres(Document doc) {
		Elements elm = doc.select(".title_wrapper a[href~=genre]");
		
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < elm.size(); i++) {
			String genre = StringEscapeUtils.unescapeHtml4(elm.get(i).text()).trim();
			this.getAllGenres().add(genre);
		}
		Log.Debug(String.format("IMDBRECORD :: Setting genres :: %s", String.join(",", this.getAllGenres())), LogType.IMDB);
	}

	@Override
	public void parseDuration(Document doc) {
		Elements elm = doc.select(".title_wrapper time");

		if (elm.size() > 0) {
			String duration = elm.get(0).attr("datetime");
			Duration dur = Duration.parse(duration);
			int minutes = (int) (dur.getSeconds() / 60);

			Log.Debug(String.format("IMDBRECORD :: Setting duration :: %s", minutes), LogType.IMDB);
			this.setDurationMinutes(minutes);
		}
	}

	@Override
	public void parseDirector(Document doc) {
		Elements elm = doc.select(".credit_summary_item:contains(Director) > a");

		if (elm.size() > 0) {
			String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.get(0).text());
			Log.Debug(String.format("IMDBRECORD :: Setting director :: %s", unescapedValue), LogType.IMDB);
			this.setDirector(unescapedValue);
		}
	}


	@Override
	public void parseTitle(Document doc) {
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
				Log.Debug(String.format("IMDBRECORD :: Setting year :: %s", unescapedValue), LogType.IMDB);
				this.setYear(Integer.parseInt(unescapedValue));
			} else {
				String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.text()).trim();
				Log.Debug(String.format("IMDBRECORD :: Setting title :: %s", unescapedValue), LogType.IMDB);
				this.setTitle(unescapedValue);
			}
		}
	}

	public String fixImdbUrl(String url) throws MalformedURLException {
		if (StringUtils.startsWithIgnoreCase(url, "www.imdb.com"))
			url = "https://" + url;

		if (StringUtils.startsWithIgnoreCase(url, "/"))
			url = "https://www.imdb.com" + url;

		if (!StringUtils.startsWithIgnoreCase(url, "https://www.imdb.com"))
			throw new MalformedURLException(
					String.format("A IMDB url must start with https://www.imdb.com. Url was :: %s", url));
		
		return url;
	}
	
	private Date parseDate(String date) {
		try {
			Log.Debug(String.format("IMDB :: parsing date :: %s", date), LogType.IMDB);
			Date parsedDate = DateUtils.parseDate(date,
					getSettings().getImdb().getDatePatterns().getPattern().toArray(new String[] {}));
			Log.Debug(String.format("IMDB :: parsed date :: %s", parsedDate), LogType.IMDB);
		} catch (ParseException e) {
			Log.Info(String.format("IMDB :: Unable to parse date :: %s", date), LogType.IMDB);
		}
		
		return null;
	}
}
