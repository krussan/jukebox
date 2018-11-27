package se.qxx.jukebox.factories;

import com.google.inject.assistedinject.Assisted;

import se.qxx.jukebox.interfaces.IJukeboxRpcServerConnection;
import se.qxx.jukebox.interfaces.IStreamingWebServer;

public interface JukeboxRpcServerFactory {
	public IJukeboxRpcServerConnection create(@Assisted("webserver") IStreamingWebServer webServer);
}
