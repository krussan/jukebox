package se.qxx.jukebox.imdb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
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
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.tools.Util;

public class IMDBParser2022 implements IIMDBParser {
	private IFileReader fileReader;
	private ISettings settings;
	private IIMDBUrlRewrite urlRewrite;
	private Document document;
	private IJukeboxLogger log;
	private IUtils utils;

	private final Pattern seasonPattern = Pattern.compile("season\\=(\\d*)");
	private final Pattern episodePattern = Pattern.compile("ttep_ep(\\d*)");

	@Inject
	public IMDBParser2022(IFileReader fileReader,
					      ISettings settings,
					      IIMDBUrlRewrite urlRewrite,
						  IUtils utils,
					      LoggerFactory loggerFactory,
					      @Assisted Document document) {

		this.setUtils(utils);
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

	public IUtils getUtils() {
		return utils;
	}

	public void setUtils(IUtils utils) {
		this.utils = utils;
	}

	@Override
	public Map<Integer, String> parseEpisodes(String rootUrl) {
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

	private int tryParseInt(String s) {
		int x = 0;
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public Map<Integer, String> parseSeasons(String rootUrl) {
		Elements elm = this.getDocument().select("select#browse-episodes-season > option");

		Map<Integer, String> result = new HashMap<>();
		for (Element e : elm) {
			int sn = tryParseInt(e.attr("value"));
			if (sn > 0) {
				String url = String.format("%s/episodes?season=%s", rootUrl, e.attr("value"));
				this.getLog().Debug(String.format("IMDBRECORD :: Setting season url :: %s", url));

				result.put(sn, url);
			}

		}

		return result;

	}

	@Override
	public String parseStory() {
		//Elements elm = this.getDocument().select("div.summary_text");
		Elements elm = this.getDocument().select("span[data-testid=plot-xs_to_m]");

		return extractValue(elm, "story");
	}


	@Override
	public String parseRating() {
		//Elements elm = this.getDocument().select("span[itemprop=ratingValue]");
		Elements elm = this.getDocument().select(".jGRxWM");
		return extractValue(elm, "rating");
	}

	private String extractValue(Elements elm, String valueType) {
		if (elm.size() > 0) {
			String unescapedValue = StringEscapeUtils.unescapeHtml4(elm.get(0).text().trim());
			this.getLog().Debug(String.format("IMDBRECORD :: Setting %s :: %s", valueType, unescapedValue));
			return unescapedValue;
		}

		return StringUtils.EMPTY;
	}

	@Override
	public String parseImageUrl() {
		//Elements elm = this.getDocument().select(".poster img");
		Elements elm = this.getDocument().select("a.ipc-lockup-overlay[href~=tt_ov_i]");

		if (elm.size() > 0)
			return elm.attr("href").trim();

		return null;
	}

	@Override
	public Date parseFirstAirDate() {
		// this.getDocument().select(".title_wrapper a[href~=/.*releaseinfo.*]");
		Elements elm = this.getDocument().select("li[data-testid=title-details-releasedate] > div a");
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
		//Elements elm = this.getDocument().select(".title_wrapper a[href~=genre]");
		Elements elm = this.getDocument().select("span.ipc-chip__text");

		List<String> result = elm.stream().map(x -> StringEscapeUtils.unescapeHtml4(x.text()).trim()).filter(x -> !x.equalsIgnoreCase("Back to top")).collect(Collectors.toList());
		this.getLog().Debug(String.format("IMDBRECORD :: Setting genres :: %s", String.join(",", result)));

		return result;
	}

	private int parseDurationText(String s) {
		Pattern p = Pattern.compile("((\\d+)\\s+hour(s?))?\\s*((\\d+)\\s+minute(s?))?");
		Matcher m = p.matcher(s);
		if (m.find()) {

			int hours = tryParseInt(m.group(2));
			int minutes = tryParseInt(m.group(5));
			return hours * 60 + minutes;
		}
		else {
			return 0;
		}
	}

	@Override
	public int parseDuration() {
		Elements elm = this.getDocument().select("li[data-testid~=title-techspec_runtime] > div");

		if (elm.size() > 0) {
			int totalMinutes = parseDurationText(elm.get(0).text());
			this.getLog().Debug(String.format("IMDBRECORD :: Setting duration :: %s", totalMinutes));

			return totalMinutes;
		}

		return 0;
	}

	@Override
	public String parseDirector() {
		//Elements elm = this.getDocument().select(".credit_summary_item:contains(Director) > a");
		Elements elm = this.getDocument().select("a[href~=tt_ov_dr]");
		return extractValue(elm, "director");
	}

	@Override
	public int parseYear() {
		Elements elm = this.getDocument().select("a[href~=tt_ov_rdat]");
		if (elm.size() > 0) {
			String parsed = StringEscapeUtils.unescapeHtml4(elm.get(0).text()).trim().substring(0, 4);
			this.getLog().Debug(String.format("IMDBRECORD :: Setting year :: %s", parsed));

			if (!StringUtils.isEmpty(parsed) && StringUtils.isNumeric(parsed))
				return Integer.parseInt(parsed);
		}

		return 0;
	}

	private enum TitleType {
		Title,
		Year
	}

	@Override
	public String parseTitle() {
		//Elements elm = this.getDocument().select(".title_wrapper > h1");
		Elements elm = this.getDocument().select("h1[data-testid~=title]");
		return extractValue(elm, "title");
	}

	private Date parseDate(String date) {
		try {
			this.getLog().Debug(String.format("IMDB :: parsing date :: %s", date));
			Date parsedDate = DateUtils.parseDate(date,
					getSettings().getImdb().getDatePatterns().toArray(new String[]{}));
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
			int season = 1;
			if (NumberUtils.isParsable(m.group(1)))
				season = Integer.parseInt(m.group(1));

			return Pair.of(season, url);
		}

		return null;
	}

	@Override
	public IMDBRecord parse(String url, boolean ignoreJson) {
		if (!ignoreJson) {
			IMDBRecord recFromJson = parseFromJson(url);
			if (recFromJson != null)
				return recFromJson;
		}

		IMDBRecord rec = new IMDBRecord(url);

		this.getLog().Debug("IMDBRECORD :: Initializing parsing");

		rec.setTitle(this.parseTitle());
		rec.setYear(this.parseYear());
		rec.setDirector(this.parseDirector());
		rec.setDurationMinutes(this.parseDuration());
		rec.addGenres(this.parseGenres());
		rec.setFirstAirDate(this.parseFirstAirDate());


		rec.setImageUrl(this.parseImageUrl());

		rec.setRating(this.parseRating());
		rec.setStory(this.parseStory());

		rec.setAllSeasonUrls(this.parseSeasons(url));
		rec.setAllEpisodeUrls(this.parseEpisodes(url));

		return rec;
	}

	public IMDBRecord parse(String url, ImdbJson.ImdbRoot root) {
		IMDBRecord rec = new IMDBRecord(url);

		rec.setTitle(root.name);
		rec.setYear(LocalDate.parse(root.datePublished, DateTimeFormatter.ISO_DATE).getYear());
		if (root.director != null)
			rec.setDirector(root.director.get(0).name);

		if (root.duration != null)
			rec.setDurationMinutes((int)Duration.parse(root.duration).toMinutes());

		rec.addGenres(root.genre);
		rec.setFirstAirDate(java.sql.Date.valueOf(LocalDate.parse(root.datePublished)));

		rec.setImageUrl(root.image);

		rec.setRating(Double.toString(root.aggregateRating.ratingValue));
		rec.setStory(root.description);


		//rec.setAllSeasonUrls(this.parseSeasons());
		//rec.setAllEpisodeUrls(this.parseEpisodes());

		return rec;
	}

	public IMDBRecord parseFromJson(String url) {
		try {
			Elements elm = this.getDocument().select("script[type=application/ld+json]");
			if (elm.size() > 0) {
				String json = elm.get(0).data();
				ImdbJson.ImdbRoot root = ImdbJson.parseJson(json);
				if (root != null)
					return parse(url, root);
			}
		} catch (IOException e) {
			this.getLog().Error("Error while parsing json", e);
		}

		return null;
	}
}