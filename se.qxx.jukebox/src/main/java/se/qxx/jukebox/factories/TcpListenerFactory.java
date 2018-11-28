package se.qxx.jukebox.factories;

import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ITcpListener;

public interface TcpListenerFactory {
	public ITcpListener create(
		@Assisted("webserver") IStreamingWebServer webServer,
		@Assisted("port") int port);
}
