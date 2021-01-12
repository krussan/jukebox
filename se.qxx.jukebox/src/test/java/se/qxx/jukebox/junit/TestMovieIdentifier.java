package se.qxx.jukebox.junit;

import com.google.protobuf.ByteString;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import se.qxx.jukebox.builders.MovieBuilderFactory;
import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.core.MovieIdentifier;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.*;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.subtitles.SubFile;
import se.qxx.jukebox.subtitles.SubtitleDownloader;
import se.qxx.jukebox.tools.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestMovieIdentifier {

	private MovieIdentifier identifier;

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock LoggerFactory loggerFactoryMock;
	@Mock IExecutor executor;
	@Mock IDatabase db;
	@Mock IArguments arguments;
	@Mock ISubtitleDownloader subtitleDownloader;
	@Mock IIMDBFinder imdbFinder;
	IUtils utils;
	IMovieBuilderFactory movieBuilderFactory;
	@Mock IMediaMetadataHelper mediaHelper;
	@Mock IFilenameChecker filenameChecker;

	private Settings settings;
	private IJukeboxLogger log;

	@Before
	public void init() throws IOException {
		settings = new Settings();
		log = new Log(settings, LogType.NONE);
		utils = new Util();

		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);

		movieBuilderFactory = new MovieBuilderFactory(settings, loggerFactoryMock, utils);

		identifier = new MovieIdentifier(executor, db, arguments, subtitleDownloader, imdbFinder, movieBuilderFactory,
				loggerFactoryMock, mediaHelper, filenameChecker);
	}

	@Test
	public void Should_return_correct_season_and_episode() {
		MovieOrSeries mos = movieBuilderFactory.identify("test_path","lorem.ipsum.dolor.S01E09.mp4");
		assertEquals("lorem ipsum dolor", mos.getMainTitle());
		assertEquals(1, mos.getSeason().getSeasonNumber());
		assertEquals(9, mos.getEpisode().getEpisodeNumber());

		when(db.findSeries(any(String.class))).thenReturn(
				Series.newBuilder()
					.setID(1)
					.setTitle("Lorem ipsum dolor")
					.setIdentifiedTitle("Lorem ipsum dolor")
					.addSeason(Season.newBuilder()
						.setID(10)
						.setTitle("Season 1")
						.setSeasonNumber(1)
						.addEpisode(Episode.newBuilder()
							.setID(100)
							.setTitle("Episode 8")
							.setEpisodeNumber(8)
							.build()
						)
						.build())
					.build());

		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);

		identifier.createOrSaveSeries(mos);
	}

}
