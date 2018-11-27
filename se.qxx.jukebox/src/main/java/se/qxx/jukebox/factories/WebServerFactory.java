package se.qxx.jukebox.factories;

import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.interfaces.IStreamingWebServer;

public interface WebServerFactory {
	public IStreamingWebServer create(@Assisted("webserverport") int port);
}
