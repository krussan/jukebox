package se.qxx.jukebox.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import se.qxx.jukebox.core.FileReader;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.IMDBParserFactory;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.imdb.IMDBGalleryHelper;
import se.qxx.jukebox.imdb.IMDBParser;
import se.qxx.jukebox.imdb.IMDBParser2022;
import se.qxx.jukebox.imdb.IMDBRecord;
import se.qxx.jukebox.imdb.IMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IFileReader;
import se.qxx.jukebox.interfaces.IIMDBGalleryHelper;
import se.qxx.jukebox.interfaces.IIMDBParser;
import se.qxx.jukebox.interfaces.IIMDBUrlRewrite;
import se.qxx.jukebox.interfaces.IRandomWaiter;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.Util;

public class TestImdbParser2022 extends ImdbParserTestBase {
	@Mock LoggerFactory loggerFactoryMock;
	@Mock IWebRetriever webRetrieverMock;
	@Mock IMDBParserFactory parserFactoryMock;
	@Mock IRandomWaiter waiterMock;
	@Mock IUtils utilsMock;
	
	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

	private Settings settings;
	private IFileReader fileReader;
	private IIMDBUrlRewrite urlRewrite;
	private IUtils utils;
	private IIMDBGalleryHelper galleryHelper;

	@Before
	public void init() throws IOException {
		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(new Log(null, LogType.NONE));
		settings = new Settings();
		fileReader = new FileReader();
		urlRewrite = new IMDBUrlRewrite();
		utils = new Util();
		galleryHelper = new IMDBGalleryHelper(settings, webRetrieverMock, loggerFactoryMock);
	}
	
	@Test
	public void TestImdbMovie2022() throws IOException {
		String movieHtml = readResource("TestImdb2022.html");

		IIMDBParser parser = createParser(movieHtml);
		
		IMDBRecord rec = parser.parse(StringUtils.EMPTY, true);
		
		assertEquals("The Amazing Spider-Man 2", rec.getTitle());
		assertEquals(3, rec.getAllGenres().size());
		assertEquals("Action", rec.getAllGenres().get(0));
		assertEquals("Adventure", rec.getAllGenres().get(1));
		assertEquals("Sci-Fi", rec.getAllGenres().get(2));
		assertEquals("Marc Webb", rec.getDirector());
		assertEquals(142, rec.getDurationMinutes());
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		assertEquals("2014-05-02", format.format(rec.getFirstAirDate()));
		
	}

	@Test
	public void TestImdbMovie2022WithJson() throws IOException {
		String movieHtml = readResource("TestImdb2022.html");

		IIMDBParser parser = createParser(movieHtml);

		IMDBRecord rec = parser.parse(StringUtils.EMPTY, false);

		assertEquals("The Amazing Spider-Man 2", rec.getTitle());
		assertEquals(3, rec.getAllGenres().size());
		assertArrayEquals(List.of("Action", "Adventure", "Sci-Fi").toArray(), rec.getAllGenres().toArray());
		assertEquals("Marc Webb", rec.getDirector());
		assertEquals(142, rec.getDurationMinutes());

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		assertEquals("2014-05-02", format.format(rec.getFirstAirDate()));

	}


	@Test
	public void Test_Movie2() throws IOException, NumberFormatException {
		String movieHtml = readResource("TestImdb2-2022.html");
		IIMDBParser parser = createParser(movieHtml);
		IMDBRecord rec = parser.parse(StringUtils.EMPTY, true);
		
		assertEquals("A Most Wanted Man", rec.getTitle());
		assertEquals(2014, rec.getYear());
		assertArrayEquals(new String[] {"Crime", "Drama", "Thriller"}, rec.getAllGenres().toArray(new String[] {}));
		assertEquals("Anton Corbijn", rec.getDirector());
		assertEquals(122, rec.getDurationMinutes());
		assertNotNull(rec.getImageUrl());
		//assertTrue(rec.getImage().length > 0);
		assertEquals("6.8", rec.getRating());
		assertEquals("A Chechen Muslim illegally immigrates to Hamburg, where he gets caught in the international war on terror.", rec.getStory());
			
	}
	
	@Test
	public void Test_Series1() throws IOException {
		String movieHtml = readResource("TestSeries1_2022.html");
		IIMDBParser parser = createParser(movieHtml);
		IMDBRecord rec = parser.parse(StringUtils.EMPTY, true);
		
		assertEquals("The Walking Dead", rec.getTitle());
		assertArrayEquals(new String[] {"Drama", "Horror", "Thriller"}, rec.getAllGenres().toArray(new String[] {}));
		assertEquals("", rec.getDirector());
		assertEquals(44, rec.getDurationMinutes());
		assertNotNull(rec.getImageUrl());
		assertEquals("8.1", rec.getRating());
		assertEquals("Sheriff Deputy Rick Grimes wakes up from a coma to learn the world is in ruins and must lead a group of survivors to stay alive.", rec.getStory());
		assertEquals(11, rec.getAllSeasonUrls().size());
	}

//	@Test
//	public void Test_Series1_2022WithJson() throws IOException {
//		String movieHtml = readResource("TestSeries1_2022.html");
//		IIMDBParser parser = createParser(movieHtml);
//		IMDBRecord rec = parser.parse(StringUtils.EMPTY, false);
//
//		assertEquals("The Walking Dead", rec.getTitle());
//		assertArrayEquals(List.of("Drama", "Horror", "Thriller").toArray(), rec.getAllGenres().toArray());
//		assertEquals("", rec.getDirector());
//		assertEquals(0, rec.getDurationMinutes());
//		assertNotNull(rec.getImageUrl());
//		assertEquals("8.1", rec.getRating());
//		assertEquals("Sheriff Deputy Rick Grimes wakes up from a coma to learn the world is in ruins and must lead a group of survivors to stay alive.", rec.getStory());
//		assertEquals(9, rec.getAllSeasonUrls().size());
//	}

	@Test
	public void Test_ReleaseInfo_No_specific_for_country() throws IOException {
		String searchResults = readResource("TestReleaseInfo.html");
		IMDBFinder finder = createFinder();
		String title = finder.findPreferredTitle(searchResults, "Sweden");
		
		assertEquals("", title);
	}
	
	@Test
	public void Test_Movie_SearchResults() throws IOException {
		String searchResults = readResource("TestImdbSearchResult.html");
		IMDBFinder finder = createFinder();
		String url = finder.findUrl(new ArrayList<String>(), searchResults, 2014, false);
		
		assertEquals("/title/tt1972571/?ref_=fn_tt_tt_1", url);
	}
	
	@Test
	public void Test_Episode1() throws IOException {
		String episodeHtml = readResource("TestEpisode1-S1E1-2022.html");
		IIMDBParser parser = createParser(episodeHtml);
		IMDBRecord rec = parser.parse(StringUtils.EMPTY, true);
		
		assertEquals("Days Gone Bye", rec.getTitle());
		assertArrayEquals(new String[] {"Drama", "Horror", "Thriller"}, rec.getAllGenres().toArray(new String[] {}));
		assertEquals("Frank Darabont", rec.getDirector());
		assertEquals(67, rec.getDurationMinutes());
		assertNotNull(rec.getImageUrl());
		assertEquals("9.2", rec.getRating());
		assertEquals("Deputy Sheriff Rick Grimes awakens from a coma, and searches for his family in a world ravaged by the undead.", rec.getStory());
		
	}

	@Test
	public void Test_2022_series_parser() throws IOException {
		String episodeHtml = readResource("TestSeries2.html");
		IIMDBParser parser = createParser(episodeHtml);
		IMDBRecord rec = parser.parse(StringUtils.EMPTY, false);

		assertEquals("Foundation", rec.getTitle());
		assertEquals(2021, rec.getYear());
		assertEquals("https://m.media-amazon.com/images/M/MV5BMTE5MDY1MGUtMmMxNi00YjA3LWIyZTYtN2FhOWJmNTY2NmM4XkEyXkFqcGdeQXVyMTkxNjUyNQ@@._V1_.jpg", rec.getImageUrl());
		assertArrayEquals(rec.getAllGenres().toArray(), List.of("Drama", "Sci-Fi").toArray());
	}

	private IIMDBParser createParser(String content) throws IOException {
		Document doc = Jsoup.parse(content);

		return new IMDBParser2022(fileReader,
				settings,
				urlRewrite,
				utils,
				loggerFactoryMock,
				doc);
	}

	private IMDBFinder createFinder() throws IOException {
		return new IMDBFinder(settings, webRetrieverMock, urlRewrite, parserFactoryMock, galleryHelper, loggerFactoryMock, waiterMock, utilsMock);
	}

}
