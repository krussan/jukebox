package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import fi.iki.elonen.NanoHTTPD.Response;
import se.qxx.jukebox.webserver.StreamingWebServer;

public class TestWebServer {
//	@Test
//	public void TestServeFile() throws IOException {
//		StreamingWebServer server = new StreamingWebServer("127.0.0.1", 8888);
//		Map<String, String> headers = new HashMap<String, String>();
//		
//		File file = File.createTempFile("jukebox", "test");
//		Response resp = server.serveFile(headers, file, "video/mp4");
//		String contentType = resp.getHeader("Content-Type");
//		
//		assertEquals("video/mp4", contentType);
//		
//	}
}
