package se.qxx.jukebox.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import se.qxx.jukebox.core.FileReader;
import se.qxx.jukebox.factories.IMDBParserFactory;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.imdb.IMDBParser;
import se.qxx.jukebox.imdb.IMDBRecord;
import se.qxx.jukebox.imdb.IMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IFileReader;
import se.qxx.jukebox.interfaces.IIMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;

public class TestImdbParser {
	@Before
	public void init() throws IOException, JAXBException {
	}
	
	@Test
	public void TestImdbMovie() throws IOException {
		String movieHtml = readResource("TestImdb1.html");

		Document doc = Jsoup.parse(movieHtml);
		
		IParserSettings parserSettings = new ParserSettings();
		IImdbSettings imdbSettings = new ImdbSettings();
		Settings settings = new Settings(imdbSettings, parserSettings, null);
		IFileReader fileReader = new FileReader();
		IIMDBUrlRewrite urlRewrite = new IMDBUrlRewrite();
		
		IMDBParser parser = new IMDBParser(fileReader, settings, urlRewrite, loggerFactory, doc)
		
		IMDBRecord rec = IMDBRecord.parse(StringUtils.EMPTY, movieHtml);
		
		assertEquals("The Amazing Spider-Man 2", rec.getTitle());
		assertEquals(3, rec.getAllGenres().size());
		assertEquals("Action", rec.getAllGenres().get(0));
		assertEquals("Adventure", rec.getAllGenres().get(1));
		assertEquals("Sci-Fi", rec.getAllGenres().get(2));
		assertEquals("Marc Webb", rec.getDirector());
		assertEquals(142, rec.getDurationMinutes());
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		assertEquals("2014-05-02", format.format(rec.getFirstAirDate()));
		assertNotNull(rec.getImage());
		assertTrue(rec.getImage().length > 0);
		
	}
	
	private String readResource(String resourceName) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourceName)));
		StringBuilder sb = new StringBuilder();
		
		String line;
		while ((line = in.readLine()) != null){
			sb.append(line).append("\n");
		}
		
		return sb.toString();	
	}

	@Test
	public void Test_Movie2() throws IOException, NumberFormatException, ParseException {
		String movieHtml = readResource("TestImdb2.html");
		IMDBRecord rec = IMDBRecord.parse(StringUtils.EMPTY, movieHtml);		
		
		assertEquals("A Most Wanted Man", rec.getTitle());
		assertEquals(2014, rec.getYear());
		assertArrayEquals(new String[] {"Crime", "Drama", "Thriller"}, rec.getAllGenres().toArray(new String[] {}));
		assertEquals("Anton Corbijn", rec.getDirector());
		assertEquals(122, rec.getDurationMinutes());
		assertNotNull(rec.getImageUrl());
		assertTrue(rec.getImage().length > 0);
		assertEquals("6.8", rec.getRating());
		assertEquals("A Chechen Muslim illegally immigrates to Hamburg, where he gets caught in the international war on terror.", rec.getStory());
			
	}
	
	@Test
	public void Test_Series1() throws IOException {
		String movieHtml = readResource("TestSeries1.html");
		IMDBRecord rec = IMDBRecord.parse(StringUtils.EMPTY, movieHtml);		
		
		assertEquals("The Walking Dead", rec.getTitle());
		assertArrayEquals(new String[] {"Drama", "Horror", "Sci-Fi"}, rec.getAllGenres().toArray(new String[] {}));
		assertEquals("", rec.getDirector());
		assertEquals(44, rec.getDurationMinutes());
		assertNotNull(rec.getImageUrl());
		assertEquals("8.4", rec.getRating());
		assertEquals("Sheriff Deputy Rick Grimes wakes up from a coma to learn the world is in ruins, and must lead a group of survivors to stay alive.", rec.getStory());
		assertEquals(9, rec.getAllSeasonUrls().size());
	}
	
	@Test
	public void Test_ReleaseInfo_No_specific_for_country() throws IOException {
		String searchResults = readResource("TestReleaseInfo.html");
		String title = IMDBFinder.findPreferredTitle(searchResults, "Sweden");
		
		assertEquals("", title);
	}
	
	@Test
	public void Test_Movie_SearchResults() throws IOException {
		String searchResults = readResource("TestImdbSearchResult.html");
		String url = IMDBFinder.findUrl(new ArrayList<String>(), searchResults, 2014, false);
		
		assertEquals("/title/tt1972571/?ref_=fn_tt_tt_1", url);
	}
	
	@Test
	public void Test_Episode1() throws IOException {
		String episodeHtml = readResource("TestEpisode1-S1E1.html");
		IMDBRecord rec = IMDBRecord.parse(StringUtils.EMPTY, episodeHtml);
		
		assertEquals("Days Gone Bye", rec.getTitle());
		assertArrayEquals(new String[] {"Drama", "Horror", "Sci-Fi"}, rec.getAllGenres().toArray(new String[] {}));
		assertEquals("Frank Darabont", rec.getDirector());
		assertEquals(67, rec.getDurationMinutes());
		assertNotNull(rec.getImageUrl());
		assertEquals("9.2", rec.getRating());
		assertEquals("Deputy Sheriff Rick Grimes awakens from a coma, and searches for his family in a world ravaged by the undead.", rec.getStory());
		
	}
	

}
