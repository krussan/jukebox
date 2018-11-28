package se.qxx.jukebox.imdb;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IFileReader;
import se.qxx.jukebox.interfaces.IIMDBParser;
import se.qxx.jukebox.interfaces.IIMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IWebRetriever;

public class IMDBParser implements IIMDBParser {
	private IFileReader fileReader;
	private ISettings settings;
	private IIMDBUrlRewrite urlRewrite;
	private Document document;
	private IJukeboxLogger log;

	private final Pattern seasonPattern = Pattern.compile("season\\=(\\d*)");
	private final Pattern episodePattern = Pattern.compile("ttep_ep(\\d*)");

	@Inject
	public IMDBParser(IFileReader fileReader, 
			ISettings settings, 
			IIMDBUrlRewrite urlRewrite, 
			IWebRetriever webRetriever,
			LoggerFactory loggerFactory,
			@Assisted Document document) {
		
		this.setFileReader(fileReader);
		this.setSettings(settings);
		this.setDocument(document);
		this.setUrlRewrite(urlRewrite);	
		
		if (loggerFactory != null)
			this.setLog(loggerFactory.create(LogType.IMDB));
	}
	

	public IJukeboxLogger getLog() {
		return log;
	}


	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public IIMDBUrlRewrite getUrlRewrite() {
		return urlRewrite;
	}

	public void setUrlRewrite(IIMDBUrlRewrite urlRewrite) {
		this.urlRewrite = urlRewrite;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
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
	public Map<Integer, String> parseEpisodes() {
		Elements elm = this.getDocument().select("strong > a[href~=ttep_ep]");
		
		Map<Integer, String> result = new HashMap<Integer, String>();
		for (Element e : elm) {
			String url = StringUtils.EMPTY;
			try {
				url = this.getUrlRewrite().fixUrl(e.attr("href"));
			} catch (MalformedURLException e1) {
				this.getLog().Error("Error parsing imdb url for season");
			}				
			this.getLog().Debug(String.format("IMDBRECORD :: Setting episode url :: %s", url));
			
			Pair<Integer, String> entry = parseEpisodeUrl(url);
			if (entry != null)
				result.put(entry.getLeft(), entry.getRight());
		}
		
		return result;
	}

	@Override
	public Map<Integer, String> parseSeasons() {
		Elements elm = this.getDocument().select(".seasons-and-year-nav a[href~=season]");
		
		Map<Integer, String> result = new HashMap<Integer, String>();
		for (Element e : elm) {
			String url = StringUtils.EMPTY;
			try {
				url = this.getUrlRewrite().fixUrl(e.attr("href"));
			} catch (MalformedURLException e1) {
				this.getLog().Error("Error parsing imdb url for season");
			}				
			this.getLog().Debug(String.format("IMDBRECORD :: Setting season url :: %s", url));
			
			Pair<Integer, String> entry = parseSeasonUrl(url);
			if (entry != null)
				result.put(entry.getLeft(), entry.getRight());

		}
		
		return result;

	}

	@Override
	public String parseStory() {
		Elements elm = this.getDocument().select("div.summary_text");

		if (elm.size() > 0) {
			String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.get(0).text());
			this.getLog().Debug(String.format("IMDBRECORD :: Setting story :: %s", unescapedValue));
			return unescapedValue;
		}
		
		return StringUtils.EMPTY;
	}


	@Override
	public String parseRating() {
		Elements elm = this.getDocument().select("span[itemprop=ratingValue]");

		if (elm.size() > 0) {
			String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.get(0).text());
			this.getLog().Debug(String.format("IMDBRECORD :: Setting rating :: %s", unescapedValue));
			return unescapedValue;
		}
		
		return StringUtils.EMPTY;
	}

	@Override
	public String parseImageUrl() {
		Elements elm = this.getDocument().select(".poster img");
		
		if (elm.size() > 0)
			return elm.attr("src").trim();
		
		return null;
	}

	@Override
	public Date parseFirstAirDate() {
		Elements elm = this.getDocument().select(".title_wrapper a[href~=/.*releaseinfo.*]");
		if (elm.size() > 0) {
			String dateValue = fetchAirDateByParenthesis(elm);
			
			if (StringUtils.isEmpty(dateValue))
				dateValue = fetchAirDateByEpisodeAired(elm);
			
			if (StringUtils.isNotEmpty(dateValue)) {
				this.getLog().Debug(String.format("IMDBRECORD :: Setting first air date :: %s", dateValue));
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
	public List<String> parseGenres() {
		Elements elm = this.getDocument().select(".title_wrapper a[href~=genre]");
		
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < elm.size(); i++) {
			String genre = StringEscapeUtils.unescapeHtml4(elm.get(i).text()).trim();
			result.add(genre);
		}
		this.getLog().Debug(String.format("IMDBRECORD :: Setting genres :: %s", String.join(",", result)));
		
		return result;
	}

	@Override
	public int parseDuration() {
		Elements elm = this.getDocument().select(".title_wrapper time");

		if (elm.size() > 0) {
			String duration = elm.get(0).attr("datetime");
			Duration dur = Duration.parse(duration);
			int minutes = (int) (dur.getSeconds() / 60);

			this.getLog().Debug(String.format("IMDBRECORD :: Setting duration :: %s", minutes));
			return minutes;
		}
		
		return 0;
	}

	@Override
	public String parseDirector() {
		Elements elm = this.getDocument().select(".credit_summary_item:contains(Director) > a");

		if (elm.size() > 0) {
			String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.get(0).text());
			this.getLog().Debug(String.format("IMDBRECORD :: Setting director :: %s", unescapedValue));
			return unescapedValue;
		}
		
		return StringUtils.EMPTY;
	}
	
	@Override
	public int parseYear() {
		String parsed = extractTitle(TitleType.Year);
		if (!StringUtils.isEmpty(parsed) && StringUtils.isNumeric(parsed))
			return Integer.parseInt(parsed);
		
		return 0;
	}

	private enum TitleType {
		Title,
		Year
	}
	
	private String extractTitle(TitleType type) {
		Elements elm = this.getDocument().select(".title_wrapper > h1");
		if (elm.size() > 0) {
			Pattern p = Pattern.compile("(.*?)\\((\\d{4})\\)");
			Matcher m = p.matcher(elm.text());

			if (m.find()) {
				String match = m.group(type == TitleType.Title ? 1 : 2);

				String unescapedValue = StringEscapeUtils.unescapeHtml4(match).trim();
				return unescapedValue;
			} else if (type == TitleType.Title) {
				String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.text()).trim();
				this.getLog().Debug(String.format("IMDBRECORD :: Setting title :: %s", unescapedValue));
				return unescapedValue;
			}
			
		}
		return StringUtils.EMPTY;

	}


	@Override
	public String parseTitle() {
		return extractTitle(TitleType.Title);
	}
	
	private Date parseDate(String date) {
		try {
			this.getLog().Debug(String.format("IMDB :: parsing date :: %s", date));
			Date parsedDate = DateUtils.parseDate(date,
					getSettings().getImdb().getDatePatterns().getPattern().toArray(new String[] {}));
			this.getLog().Debug(String.format("IMDB :: parsed date :: %s", parsedDate));
			
			return parsedDate;
		} catch (ParseException e) {
			this.getLog().Info(String.format("IMDB :: Unable to parse date :: %s", date));
		}
		
		return null;
	}
	
	public Pair<Integer, String> parseEpisodeUrl(String url) {
		Matcher m = episodePattern.matcher(url);
		if (m.find()) {
			int episode = Integer.parseInt(m.group(1));
			return Pair.of(episode, url);
		}
		
		return null;
	}

	public Pair<Integer, String> parseSeasonUrl(String url) {		
		Matcher m = seasonPattern.matcher(url);
		if (m.find()) {
			int season = Integer.parseInt(m.group(1));
			return Pair.of(season, url);
		}
		
		return null;		
	}

	@Override
	public IMDBRecord parse(String url) {
		IMDBRecord rec = new IMDBRecord(url);
		
		this.getLog().Debug(String.format("IMDBRECORD :: Initializing parsing"));
		
		rec.setTitle(this.parseTitle());
		rec.setYear(this.parseYear());
		rec.setDirector(this.parseDirector());
		rec.setDurationMinutes(this.parseDuration());
		rec.addGenres(this.parseGenres());
		rec.setFirstAirDate(this.parseFirstAirDate());
		
	
		rec.setImageUrl(this.parseImageUrl());
		
		rec.setRating(this.parseRating());
		rec.setStory(this.parseStory());
		
		rec.setAllSeasonUrls(this.parseSeasons());
		rec.setAllEpisodeUrls(this.parseEpisodes());

		return rec;
	}

}
