package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.builders.ParserMovie;
import se.qxx.jukebox.settings.Settings;

public class TestParserBuilder {

	@Before
	public void init() throws IOException, JAXBException {
		Settings.initialize();
	}
	
	@Test
	public void TestSeriesSonsOfAnarchyS04E08() {
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", "Sons.of.Anarchy.S04.E08.Family Recipe.mp4");
		
		assertEquals("Sons of Anarchy", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(8, m.getEpisode());
	}
	
	@Test
	public void TestParts() {
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", "Shawshank.Redemption.DVDRip.XviD-cd1.avi");
		
		assertEquals("Shawshank Redemption", m.getMovieName());
		assertEquals(1, m.getTypes().size());
		assertEquals("DVDRip", m.getTypes().get(0));
		assertEquals(1, m.getFormats().size());
		assertEquals("XviD", m.getFormats().get(0));
		assertEquals(1, m.getPart());
	}

	@Test
	public void TestLanguage() {
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", "Lilla.Spoket.Laban.Spokdags.2007.Swedish.DVDRip.Xvid-monica112.avi");
		
		assertEquals("Lilla Spoket Laban Spokdags", m.getMovieName());
		assertEquals(2007, m.getYear());
		assertEquals(1, m.getTypes().size());
		assertEquals("DVDRip", m.getTypes().get(0));
		assertEquals(1, m.getFormats().size());
		assertEquals("Xvid", m.getFormats().get(0));
		assertEquals("monica112", m.getGroupName());
	}
	
	@Test
	public void TestMessySeries() {
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", "0401-tvp-fallingskies-s04e01-1080p.mkv");
		
		assertEquals("fallingskies", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(1, m.getEpisode());
		
		assertEquals(2007, m.getYear());
		assertEquals(1, m.getTypes().size());
		assertEquals("1080p", m.getTypes().get(0));
		
	}
	
	//
}
