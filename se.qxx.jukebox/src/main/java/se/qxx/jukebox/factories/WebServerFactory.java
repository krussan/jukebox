package se.qxx.jukebox.factories;

import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.interfaces.IStreamingWebServer;

import java.util.concurrent.ExecutorService;

public interface WebServerFactory {
	IStreamingWebServer create(
			@Assisted("webserverport") int port);
}
