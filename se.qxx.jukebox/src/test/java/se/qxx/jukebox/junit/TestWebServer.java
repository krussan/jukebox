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
import se.qxx.jukebox.webserver.StreamingFile;
import se.qxx.jukebox.webserver.StreamingWebServer;

public class TestWebServer {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock LoggerFactory loggerFactoryMock;
	ISettings settings;
	@Mock IDatabase dbMock;
	@Mock ISubtitleFileWriter subWriterMock;
	IJukeboxLogger log;
	StreamingWebServer webServer;
	
	@Before
	public void initialize() throws IOException, JAXBException {
		IParserSettings parserSettings = new ParserSettings();
		IImdbSettings imdbSettings = new ImdbSettings();
		
		settings = new Settings(imdbSettings, parserSettings);
		
		log = new Log(settings, LogType.NONE);
		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);
		
		webServer = new StreamingWebServer(
				dbMock, 
				loggerFactoryMock, 
				subWriterMock,
				8001);
		
		webServer.setIpAddress("127.0.0.1");
		webServer.initializeMappings(settings);
	}
	
	@After
	public void tearDown() {
		webServer.stop();
	}

	@Test
	public void TestStreamUri() throws InterruptedException, IOException {
		webServer.initialize();		

		String uri = webServer.getStreamUri("stream1.mp4");
		
		assertTrue(webServer.isAlive());
		assertEquals("http://127.0.0.1:8001/stream1.mp4", uri);
		
		
	}
	
	@Test
	public void TestDuplicateRegistrationSubtitle_returnsLatestFile() {
		webServer.registerFile("stream1.mp4", "original_file.mp4");
		webServer.registerFile("stream1.mp4", "other_file.mp4");
		
		String streamingFile = webServer.getRegisteredFile("stream1.mp4");
		assertEquals("other_file.mp4", streamingFile);

	}
	
	@Test
	public void TestRegisterSubtitle() throws UnsupportedEncodingException {
		webServer.initialize();	
		
		Subtitle sub = Subtitle.newBuilder()
			.setID(1)
			.setFilename("abc.srt")
			.setMediaIndex(0)
			.setRating(Rating.ExactMatch)
			.setDescription("ABC-SUB")
			.setLanguage("SE")
			.setTextdata(ByteString.copyFrom("This is a text", "iso-8859-1"))
			.build();
		
		when(subWriterMock.getTempFile(any(Subtitle.class), any(String.class))).thenReturn(new File("sub1.vtt"));
		StreamingFile file = webServer.registerSubtitle(sub);
		assertEquals("http://127.0.0.1:8001/sub1.vtt", file.getUri());
	}
}
