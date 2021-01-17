package se.qxx.jukebox.factories;

import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ITcpListener;

import java.util.concurrent.ExecutorService;

public interface TcpListenerFactory {
	ITcpListener create(
		@Assisted("webserver") IStreamingWebServer webServer,
		@Assisted("port") int port);
}
