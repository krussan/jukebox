package se.qxx.jukebox.junit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;

import static org.junit.Assert.assertEquals;

public class TestSubtitleDownloader {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock private LoggerFactory loggerFactoryMock;
	@Mock private IExecutor executor;
	
	private Settings settings;
	private IJukeboxLogger log;
	
	@Before
	public void init() throws IOException, JAXBException {
		IParserSettings parserSettings = new ParserSettings();
		IImdbSettings imdbSettings = new ImdbSettings();
		
		settings = new Settings(imdbSettings, parserSettings);
		log = new Log(settings, LogType.NONE);
		
		when(loggerFactoryMock.create(any(Log.LogType.class))).thenReturn(log);

	}
	
	@Test
	public void testDecoupleSeries() throws IOException {
		// Arrange
		IDatabase db = new DB(settings, executor, loggerFactoryMock);
		List<Series> series = new ArrayList<>();
		
		// add two episodes to same season/series
		series.add(
			Series.newBuilder()
				.setID(1)
				.addSeason(Season.newBuilder()
						.setID(11)
						.setSeasonNumber(1)
						.addEpisode(Episode.newBuilder()
								.setID(111)
								.setTitle("Episode 1"))
						.addEpisode(Episode.newBuilder()
								.setID(222)
								.setTitle("Episode 2")))
				.setIdentifiedTitle("Series 1")
				.build());
		
		// Act
		List<MovieOrSeries> result = db.decoupleSeries(series);
		
		// Assert
		assertEquals(2, result.size());
		Series s1 = result.get(0).getSeries();
		Series s2 = result.get(1).getSeries();
		
		assertEquals(1, s1.getID());
		assertEquals(1, s1.getSeasonCount());
		assertEquals(11, s1.getSeason(0).getID());
		assertEquals(1, s1.getSeason(0).getEpisodeCount());
		assertEquals(111, s1.getSeason(0).getEpisode(0).getID());
		
		assertEquals(1, s2.getID());
		assertEquals(1, s2.getSeasonCount());
		assertEquals(11, s2.getSeason(0).getID());
		assertEquals(1, s2.getSeason(0).getEpisodeCount());
		assertEquals(222, s2.getSeason(0).getEpisode(0).getID());
		
	}
}
