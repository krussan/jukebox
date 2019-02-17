package se.qxx.jukebox.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.protobuf.ByteString;

import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IImdbSettings;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IParserSettings;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;
import se.qxx.jukebox.settings.parser.ParserSettings;
import se.qxx.jukebox.subtitles.SubtitleFileWriter;
import se.qxx.jukebox.webserver.StreamingFile;
import se.qxx.jukebox.webserver.StreamingWebServer;

public class TestSubtitleFileWriter {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock LoggerFactory loggerFactoryMock;
	ISettings settings;
	@Mock IDatabase dbMock;
	IJukeboxLogger log;
	
	@Before
	public void initialize() throws IOException, JAXBException {
		IParserSettings parserSettings = new ParserSettings();
		IImdbSettings imdbSettings = new ImdbSettings();
		
		settings = new Settings(imdbSettings, parserSettings);
		
		log = new Log(settings, LogType.NONE);
		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);
		
	}
	
	@After
	public void tearDown() {
	}

	@Test
	public void TestGetStreamFile() throws UnsupportedEncodingException {
		SubtitleFileWriter writer = new SubtitleFileWriter(loggerFactoryMock);

		Subtitle sub1 = Subtitle.newBuilder()
				.setID(100)
				.setFilename("ACB1.srt")
				.setMediaIndex(0)
				.setLanguage("SE")
				.setRating(Rating.ExactMatch)
				.setTextdata(ByteString.copyFrom("This is a subtitle test AAA", "iso-8859-1"))
				.setDescription("ABC")
				.build();

		File file = writer.getTempFile(sub1, "vtt");
		
		assertEquals("ACB1.vtt", file.getName());
	}
	
}
