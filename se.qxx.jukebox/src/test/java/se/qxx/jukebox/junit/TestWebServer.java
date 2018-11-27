package se.qxx.jukebox.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;
import se.qxx.jukebox.webserver.StreamingWebServer;

public class TestWebServer {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock LoggerFactory loggerFactoryMock;
	@Mock ISettings settingsMock;
	@Mock IDatabase dbMock;
	@Mock ISubtitleFileWriter subWriterMock;
	

	@Test
	public void TestStreamUri() throws InterruptedException, IOException {
		
		IJukeboxLogger log = new Log(settingsMock, LogType.NONE);
		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);
		
		StreamingWebServer webServer = new StreamingWebServer(
				dbMock, 
				loggerFactoryMock, 
				subWriterMock,
				8001);
		
		
		webServer.setIpAddress("127.0.0.1");
		webServer.initialize();

		String uri = webServer.getStreamUri("stream1.mp4");
		
		assertTrue(webServer.isAlive());
		assertEquals("http://127.0.0.1:8001/stream1.mp4", uri);
		
		webServer.stop();
		
	}
}
