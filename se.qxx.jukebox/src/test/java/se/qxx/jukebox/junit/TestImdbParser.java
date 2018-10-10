package se.qxx.jukebox.junit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.imdb.IMDBFinder;
import se.qxx.jukebox.imdb.IMDBRecord;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.WebResult;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBException;

public class TestImdbParser {
	@Before
	public void init() throws IOException, JAXBException {
		Settings.initialize();
	}
	
	@Test
	public void TestImdbMovie() throws IOException {
		String movieHtml = readResource("TestImdb1.html");
		IMDBRecord rec = IMDBRecord.parse(movieHtml);
		
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

	@Ignore
	@Test
	public void Test_Movie_Live() throws IOException, NumberFormatException, ParseException {
		IMDBRecord rec = IMDBFinder.Search(
			"A Most Wanted Man",
			2014,
			null,
			false);
		
		assertEquals("A Most Wanted Man", rec.getTitle());
		assertEquals(2014, rec.getYear());
		assertArrayEquals(new String[] {"Crime", "Drama", "Thriller"}, rec.getAllGenres().toArray(new String[] {}));
		assertEquals("Anton Corbijn", rec.getDirector());
		assertEquals(122, rec.getDurationMinutes());
		assertNotNull(rec.getImageUrl());
		assertEquals("6.8", rec.getRating());
		assertEquals("A Chechen Muslim illegally immigrates to Hamburg, where he gets caught in the international war on terror.", rec.getStory());
			
		
	}
	

}
