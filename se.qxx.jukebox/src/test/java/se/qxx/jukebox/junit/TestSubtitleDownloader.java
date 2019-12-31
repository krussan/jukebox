package se.qxx.jukebox.junit;

import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.google.protobuf.ByteString;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import org.mockito.stubbing.Answer;
import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.*;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.ImdbSettings;
import se.qxx.jukebox.settings.ParserSettings;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubtitleDownloader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestSubtitleDownloader {

	private SubtitleDownloader downloader;

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock private LoggerFactory loggerFactoryMock;
	@Mock private IExecutor executor;
	@Mock private IDatabase db;
	@Mock private IMovieBuilderFactory movieBuilderFactory;
	@Mock private ISubFileDownloaderHelper helper;
	@Mock private IMkvSubtitleReader mkvSubtitleReader;
	@Mock private IUnpacker unpacker;
	@Mock private ISubFileUtilHelper fileUtilHelper;
	@Mock private IUtils utils;

	private Settings settings;
	private IJukeboxLogger log;

	@Before
	public void init() throws IOException, JAXBException {
		IParserSettings parserSettings = new ParserSettings();
		IImdbSettings imdbSettings = new ImdbSettings();
		
		settings = new Settings(imdbSettings, parserSettings);
		log = new Log(settings, LogType.NONE);
		downloader = new SubtitleDownloader(db, executor, settings, movieBuilderFactory, helper, mkvSubtitleReader,
				loggerFactoryMock, unpacker, fileUtilHelper, utils);

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
							.setTitle("Episode 1")
							.setSubtitleQueue(JukeboxDomain.SubtitleQueue.newBuilder()
								.setID(1111)
								.setSubtitleRetreiveResult(1)))
					.addEpisode(Episode.newBuilder()
							.setID(222)
							.setTitle("Episode 2")
							.setSubtitleQueue(JukeboxDomain.SubtitleQueue.newBuilder()
									.setID(1111)
									.setSubtitleRetreiveResult(0))))

				.setIdentifiedTitle("Series 1")
				.build());
		
		// Act
		List<MovieOrSeries> result = db.decoupleSeries(series, false);
		
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

	@Test
	public void Should_remove_all_subs() {
		// Arrange
		Episode ep = Episode.newBuilder()
			.setID(1)
			.addMedia(JukeboxDomain.Media.newBuilder()
					.setID(10)
					.setFilename("ABC.mp4")
					.setIndex(0)
					.setFilepath("/media")
					.setDownloadComplete(true)
					.addSubs(JukeboxDomain.Subtitle.newBuilder()
							.setID(100)
							.setFilename("ABC.srt")
							.setDescription("ABC")
							.setRating(JukeboxDomain.Rating.PositiveMatch)
							.setMediaIndex(1)
							.setLanguage("en")
							.setTextdata(ByteString.EMPTY)
							.build())
					.addSubs(JukeboxDomain.Subtitle.newBuilder()
							.setID(110)
							.setFilename("DEF.srt")
							.setDescription("DEF")
							.setRating(JukeboxDomain.Rating.PositiveMatch)
							.setMediaIndex(1)
							.setLanguage("en")
							.setTextdata(ByteString.EMPTY)
							.build())
			)
			.addMedia(JukeboxDomain.Media.newBuilder()
					.setID(20)
					.setFilename("FFF.mp4")
					.setIndex(1)
					.setFilepath("/media")
					.setDownloadComplete(true)
					.addSubs(JukeboxDomain.Subtitle.newBuilder()
							.setID(120)
							.setFilename("FFF.srt")
							.setDescription("FFF")
							.setRating(JukeboxDomain.Rating.PositiveMatch)
							.setMediaIndex(1)
							.setLanguage("en")
							.setTextdata(ByteString.EMPTY)
							.build())
					.addSubs(JukeboxDomain.Subtitle.newBuilder()
							.setID(130)
							.setFilename("EEE.srt")
							.setDescription("EEE")
							.setRating(JukeboxDomain.Rating.PositiveMatch)
							.setMediaIndex(1)
							.setLanguage("en")
							.setTextdata(ByteString.EMPTY)
							.build())
			)
			.build();

		Episode expected = Episode.newBuilder()
				.setID(1)
				.addMedia(JukeboxDomain.Media.newBuilder()
						.setID(10)
						.setFilename("ABC.mp4")
						.setIndex(0)
						.setFilepath("/media")
						.setDownloadComplete(true)
				)
				.addMedia(JukeboxDomain.Media.newBuilder()
						.setID(20)
						.setFilename("FFF.mp4")
						.setIndex(1)
						.setFilepath("/media")
						.setDownloadComplete(true)
				)
				.build();

		when(db.save(any(Episode.class))).thenAnswer((Answer<Episode>) x -> (Episode) x.getArgument(0));

		// Act
		downloader.reenlistEpisode(ep);

		// Assert
		verify(db, times(1)).addEpisodeToSubtitleQueue(expected);
	}

	@Test
	public void Should_return_just_one_found_sub_matching_in_dir() {
		// Arrange
		when(fileUtilHelper.findSubsInDirectory(any(File.class))).thenReturn(List.of("This_is_a_series.S03E01.srt",
				"This_is_a_series.S03E02.srt", "This_is_a_series.S03E03.srt", "This_is_a_series.S03E04.srt",
				"This_is_a_series.S03E05.srt", "This_is_a_series.S03E06.srt", "This_is_a_series.S03E07.srt",
				"This_is_a_series.S03E08.srt", "This_is_a_series.S03E09.srt", "This_is_a_series.S03E10.srt"));
		// Act
		List<SubFile> actual = downloader.checkDirForSubs("This_is_a_series.S03E10", new File("/media"));

		// Assert
		assertEquals(1, actual.size());
		assertEquals("This_is_a_series.S03E10.srt", actual.get(0).getFile().getName());
	}
}
