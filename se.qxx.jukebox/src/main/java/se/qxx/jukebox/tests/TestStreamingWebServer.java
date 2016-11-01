package se.qxx.jukebox.tests;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import se.qxx.jukebox.StreamingWebServer;

public class TestStreamingWebServer {

	public static void main(String[] args) throws IOException, JAXBException {

		StreamingWebServer.setup("0.0.0.0", 8001);
		
		System.out.println("Continuing...");
		if (args.length > 0)
			StreamingWebServer.get().registerFile(args[0]);
		
	}
}
