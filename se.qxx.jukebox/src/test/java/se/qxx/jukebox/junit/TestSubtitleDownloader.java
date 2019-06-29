package se.qxx.jukebox.junit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import se.qxx.jukebox.core.DB;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;

public class TestSubtitleDownloader {

	@Mock private LoggerFactory loggerFactoryMock;
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
		IDatabase db = new DB(settings, loggerFactoryMock);
		List<Series> series = new ArrayList<>();
		
		// Act
		db.decoupleSeries(series);
		
		// Assert
		
	}
}
