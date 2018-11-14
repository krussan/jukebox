package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.builders.ParserMovie;
import se.qxx.jukebox.domain.MovieOrSeries;
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
	public void TestSeriesFallingSkies1() {
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", "0401-tvp-fallingskies-s04e01-1080p.mkv");
		
		assertEquals("fallingskies", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(1, m.getEpisode());
		
		assertEquals(1, m.getTypes().size());
		assertEquals("1080p", m.getTypes().get(0));
		
	}
	
	@Test
	public void TestSeriesFallingSkies2() {
		String filename = "0402-tvs-fs-dd51-ded-dl-18p-ithd-avc-402.mkv";

		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", filename);
		
		assertEquals("fs", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(2, m.getEpisode());
		
		assertEquals(1, m.getTypes().size());
		assertEquals("18p", m.getTypes().get(0));

		assertEquals(1, m.getSounds().size());
		assertEquals("dd51", m.getSounds().get(0));

		assertEquals("tvs", m.getGroupName());
	}
	
	@Test
	public void TestSeriesFallingSkies3() {
		String filename = "0410-Fallin.Skis.S04E10.GER.DL.DUB.108p.WHD.x264-TVP.mkv";
		
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", filename);
		
		assertEquals("Fallin Skis", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(10, m.getEpisode());
		
		assertEquals(1, m.getLanguages().size());
		assertEquals("GER", m.getLanguages().get(0));
		
		assertEquals(1, m.getTypes().size());
		assertEquals("108p", m.getTypes().get(0));

		assertEquals(1, m.getFormats().size());
		assertEquals("x264", m.getFormats().get(0));

		assertEquals("TVP", m.getGroupName());
	}
	
	@Test
	public void TestDragon() {
		String filename = "How.to.Train.Your.Dragon.2.2014.1080p.WEB-DL.AAC2.0.H264-RARBG.mkv";
		
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", filename);
		
		assertEquals("How to Train Your Dragon 2", m.getMovieName());

		assertEquals(2014, m.getYear());
		
		assertEquals(1, m.getTypes().size());
		assertEquals("1080p", m.getTypes().get(0));

		assertEquals(1, m.getFormats().size());
		assertEquals("H264", m.getFormats().get(0));

		assertEquals(1, m.getSounds().size());
		assertEquals("AAC2", m.getSounds().get(0));
		
		assertEquals("RARBG", m.getGroupName());
	}
	
	@Test
	public void TestSeriesWire() {
		String filename = "the.wire.s05e09.dvdrip.xvid-orpheus.avi";
		
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", filename);
		
		assertEquals("the wire", m.getMovieName());
		assertEquals(5, m.getSeason());
		assertEquals(9, m.getEpisode());
		
		assertEquals(1, m.getTypes().size());
		assertEquals("dvdrip", m.getTypes().get(0));

		assertEquals(1, m.getFormats().size());
		assertEquals("xvid", m.getFormats().get(0));
	
		assertEquals("orpheus", m.getGroupName());
	}
	
	@Test
	public void TestSpiderman() {
		String filename = "The Amazing Spider-Man 2 2014 KORSUB 720p WEBRip x264 AAC-JYK.mp4";
		
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", filename);
		
		assertEquals("The Amazing Spider Man 2", m.getMovieName());
		assertEquals(2014, m.getYear());

		assertEquals(2, m.getTypes().size());
		assertEquals("720p", m.getTypes().get(0));
		assertEquals("WEBRip", m.getTypes().get(1));

		assertEquals(1, m.getFormats().size());
		assertEquals("x264", m.getFormats().get(0));
		
		assertEquals(1, m.getSounds().size());
		assertEquals("AAC", m.getSounds().get(0));
		
		assertEquals("JYK", m.getGroupName());		
	}
	
	@Test
	public void TestFiftyFifty() {
		String filename = "50 50 BDRip XviD-DEFACED.dummy";
		
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", filename);
		
		assertEquals("50 50", m.getMovieName());

		assertEquals(1, m.getTypes().size());
		assertEquals("BDRip", m.getTypes().get(0));

		assertEquals(1, m.getFormats().size());
		assertEquals("XviD", m.getFormats().get(0));
		
		assertEquals("DEFACED", m.getGroupName());		
	}
	
	@Test
	public void TestGemenskap() {
		// this filename needs a lookahead to get the 1 following the CD token
		String filename = "G - Som I Gemenskap CD 1.avi";
		
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", filename);
		
		assertEquals("G Som I Gemenskap", m.getMovieName());

		assertEquals(1, m.getPart());
	}
	
	@Test
	public void TestSons() {
		String filename = " Sons.of.Anarchy.S04.E13To Be, Act 1.mp4";
		
		ParserBuilder b = new ParserBuilder();
		ParserMovie m = b.extractMovieParser("", filename);
		
		assertEquals("Sons of Anarchy", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(13, m.getEpisode());
	}
	
	@Test
	public void TestSeasonInTitle() {
		String filename = "This and that Season 1.mp4";
		
		ParserBuilder b = new ParserBuilder();
		ParserMovie pm = b.extractMovieParser("", filename);
		MovieOrSeries mos = pm.build();
		
		assertTrue(mos.isSeries());
		assertEquals("This and that", mos.getMainTitle());
		assertEquals(1, pm.getSeason());
		assertEquals(0, pm.getEpisode());
	}
	
	
	//
}
