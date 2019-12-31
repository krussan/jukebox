package se.qxx.jukebox.factories;

import se.qxx.jukebox.interfaces.IVLCConnection;

public interface VLCConnectionFactory {
	public IVLCConnection create(String host, int port);
}
