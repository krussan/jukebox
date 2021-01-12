package se.qxx.jukebox.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import se.qxx.jukebox.builders.MovieBuilderFactory;
import se.qxx.jukebox.builders.ParentDirectoryBuilder;
import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.builders.ParserMovie;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.settings.Settings;

public class TestParserBuilder {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	private ParserBuilder parserBuilder;
	private Settings settings;
	private IJukeboxLogger log;
	private MovieBuilderFactory movieBuilderFactory;
	
	@Mock LoggerFactory loggerFactoryMock;
	@Mock IUtils utilsMock;
	
	@Before
	public void init() throws IOException {
		settings = new Settings();
		log = new Log(settings, LogType.NONE);
		
		when(loggerFactoryMock.create(any(Log.LogType.class))).thenReturn(log);
		
		parserBuilder = new ParserBuilder(settings, log);
		movieBuilderFactory = new MovieBuilderFactory(settings, loggerFactoryMock, utilsMock);
	}
	
	@Test
	public void TestSeriesSonsOfAnarchyS04E08() {
		ParserMovie m = parserBuilder.extractMovieParser("", "Lorem.Ipsum.Dolor.S04.E08.Family Recipe.mp4");
		
		assertEquals("Lorem Ipsum Dolor", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(8, m.getEpisode());
	}
	
	@Test
	public void TestParts() {
		ParserMovie m = parserBuilder.extractMovieParser("", "Lorem.Ipsum.DVDRip.XviD-cd1.avi");
		
		assertEquals("Lorem Ipsum", m.getMovieName());
		assertEquals(1, m.getTypes().size());
		assertEquals("DVDRip", m.getTypes().get(0));
		assertEquals(1, m.getFormats().size());
		assertEquals("XviD", m.getFormats().get(0));
		assertEquals(1, m.getPart());
	}

	@Test
	public void TestLanguage() {
		ParserMovie m = parserBuilder.extractMovieParser("", "Lorem.Ipsum.Dolor.Sit.Amet.2007.Swedish.DVDRip.Xvid-zzz.avi");
		
		assertEquals("Lorem Ipsum Dolor Sit Amet", m.getMovieName());
		assertEquals(2007, m.getYear());
		assertEquals(1, m.getTypes().size());
		assertEquals("DVDRip", m.getTypes().get(0));
		assertEquals(1, m.getFormats().size());
		assertEquals("Xvid", m.getFormats().get(0));
		assertEquals("zzz", m.getGroupName());
	}
	
	@Test
	public void TestSeriesFallingSkies1() {
		ParserMovie m = parserBuilder.extractMovieParser("", "0401-tvp-loremipsum-s04e01-1080p.mkv");
		
		assertEquals("loremipsum", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(1, m.getEpisode());
		
		assertEquals(1, m.getTypes().size());
		assertEquals("1080p", m.getTypes().get(0));
		
	}
	
	@Test
	public void TestSeriesFallingSkies2() {
		String filename = "0402-tvs-fs-dd51-ded-dl-18p-ithd-avc-402.mkv";

		ParserMovie m = parserBuilder.extractMovieParser("", filename);
		
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
		String filename = "0410-Lorem.Ipsum.S04E10.GER.DL.DUB.108p.WHD.x264-xyz.mkv";
		
		ParserMovie m = parserBuilder.extractMovieParser("", filename);
		
		assertEquals("Lorem Ipsum", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(10, m.getEpisode());
		
		assertEquals(1, m.getLanguages().size());
		assertEquals("GER", m.getLanguages().get(0));
		
		assertEquals(1, m.getTypes().size());
		assertEquals("108p", m.getTypes().get(0));

		assertEquals(1, m.getFormats().size());
		assertEquals("x264", m.getFormats().get(0));

		assertEquals("xyz", m.getGroupName());
	}
	
	@Test
	public void TestDragon() {
		String filename = "Lorem.ipsum.dolor.sit.amet.2.2014.1080p.WEB-DL.AAC2.0.H264-xyz.mkv";
		
		ParserMovie m = parserBuilder.extractMovieParser("", filename);
		
		assertEquals("Lorem ipsum dolor sit amet 2", m.getMovieName());

		assertEquals(2014, m.getYear());
		
		assertEquals(1, m.getTypes().size());
		assertEquals("1080p", m.getTypes().get(0));

		assertEquals(1, m.getFormats().size());
		assertEquals("H264", m.getFormats().get(0));

		assertEquals(1, m.getSounds().size());
		assertEquals("AAC2", m.getSounds().get(0));
		
		assertEquals("xyz", m.getGroupName());
	}
	
	@Test
	public void TestSeriesWire() {
		String filename = "lorem.ipsum.s05e09.dvdrip.xvid-dolor.avi";
		
		ParserMovie m = parserBuilder.extractMovieParser("", filename);
		
		assertEquals("lorem ipsum", m.getMovieName());
		assertEquals(5, m.getSeason());
		assertEquals(9, m.getEpisode());
		
		assertEquals(1, m.getTypes().size());
		assertEquals("dvdrip", m.getTypes().get(0));

		assertEquals(1, m.getFormats().size());
		assertEquals("xvid", m.getFormats().get(0));
	
		assertEquals("dolor", m.getGroupName());
	}
	
	@Test
	public void TestSpiderman() {
		String filename = "Lorem ipsum dolor sit amet 2 2014 720p WEBRip x264 AAC-JYK.mp4";
		
		ParserMovie m = parserBuilder.extractMovieParser("", filename);
		
		assertEquals("Lorem ipsum dolor sit amet 2", m.getMovieName());
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
		String filename = "10 10 BDRip XviD-DEFACED.dummy";
		
		ParserMovie m = parserBuilder.extractMovieParser("", filename);
		
		assertEquals("10 10", m.getMovieName());

		assertEquals(1, m.getTypes().size());
		assertEquals("BDRip", m.getTypes().get(0));

		assertEquals(1, m.getFormats().size());
		assertEquals("XviD", m.getFormats().get(0));
		
		assertEquals("DEFACED", m.getGroupName());		
	}
	
	@Test
	public void TestGemenskap() {
		// this filename needs a lookahead to get the 1 following the CD token
		String filename = "TT - ABCDEF CD 1.avi";
		
		ParserMovie m = parserBuilder.extractMovieParser("", filename);
		
		assertEquals("TT ABCDEF", m.getMovieName());

		assertEquals(1, m.getPart());
	}
	
	@Test
	public void TestSons() {
		String filename = " Lorem.Ipsum.Dolor.S04.E13Sit Amet, Part 1.mp4";
		
		ParserMovie m = parserBuilder.extractMovieParser("", filename);
		
		assertEquals("Lorem Ipsum Dolor", m.getMovieName());
		assertEquals(4, m.getSeason());
		assertEquals(13, m.getEpisode());
	}
	
	@Test
	public void TestSeasonInTitle() {
		String filename = "This and that Season 1.mp4";
		
		ParserMovie pm = parserBuilder.extractMovieParser("", filename);
		MovieOrSeries mos = pm.build();
		
		assertTrue(mos.isSeries());
		assertEquals("This and that", mos.getMainTitle());
		assertEquals(1, pm.getSeason());
		assertEquals(0, pm.getEpisode());
	}

	@Test
	public void TestCompleteSeasonInTitle() {
		String filename = "This and that Complete Season 1.mp4";
		
		ParserMovie pm = parserBuilder.extractMovieParser("", filename);
		MovieOrSeries mos = pm.build();
		
		assertTrue(mos.isSeries());
		assertEquals("This and that", mos.getMainTitle());
		assertEquals(1, pm.getSeason());
		assertEquals(0, pm.getEpisode());
	}

	@Test
	public void TestSeriesProposals() {
		List<MovieOrSeries> proposals = new ArrayList<MovieOrSeries>();
		MovieOrSeries mos1 = new MovieOrSeries(
				Series.newBuilder()
				.setID(-1)
				.setTitle("This and that 1")
				.setIdentifiedTitle("This and that 1")
				.addSeason(Season.newBuilder()
					.setID(-1)
					.setSeasonNumber(1)
					.addEpisode(Episode.newBuilder()
						.setID(-1)
						.setEpisodeNumber(0)
						.build())
					.build())
				.build());
		mos1.setIdentifierRating(80);
		
		MovieOrSeries mos2 = new MovieOrSeries(
				Series.newBuilder()
				.setID(-1)
				.setTitle("This and that 2")
				.setIdentifiedTitle("This and that 2")
				.addSeason(Season.newBuilder()
					.setID(-1)
					.setSeasonNumber(1)
					.addEpisode(Episode.newBuilder()
						.setID(-1)
						.setEpisodeNumber(3)
						.build())
					.build())
				.build());
		mos2.setIdentifierRating(10);
		
		proposals.add(mos1);
		proposals.add(mos2);

		MovieOrSeries mos = movieBuilderFactory.build("", "This and that Season1.mp4", proposals);
		
		assertTrue(mos.isSeries());
		assertEquals(1, mos.getSeason().getSeasonNumber());
		assertEquals(3, mos.getSeason().getEpisode(0).getEpisodeNumber());		
		assertEquals(mos.getMainTitle(), "This and that 1");
	}

	@Test
	public void should_ignore_season_if_already_set() {
		ParserBuilder pb = new ParserBuilder(settings, log);
		String filepath = "Lorem Ipsum Dolor Season 4 (1080p Web x265 10bit abc)";
		String filename = "Lorem Ipsum Dolor S04E01 Sit Amet (1080p x265 10bit S33 Joy).mkv";

		MovieOrSeries parserResult = pb.extract(filepath, filename);

		assertEquals(4, parserResult.getSeason().getSeasonNumber());
		assertEquals(1, parserResult.getEpisode().getEpisodeNumber());

	}

	@Test
	public void should_not_match_series() {
		// Guardians of the Galaxy Vol. 2 2017  (1080p x265 q22 S90 Joy)
		ParserBuilder pb = new ParserBuilder(settings, log);
		String filepath = "test_path";
		String filename = "Lorem Ipsum Dolor Sit Amet 2 2008 (1080p x265 S33 Joy).mkv";

		MovieOrSeries parserResult = pb.extract(filepath, filename);

		assertFalse(parserResult.isSeries());
		assertEquals(2008, parserResult.getYear());
		assertEquals("x265", parserResult.getFormat());
		assertEquals("1080p", parserResult.getMovie().getType());
	}
	//
}
