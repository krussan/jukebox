package se.qxx.jukebox.interfaces;

import io.grpc.Server;

import java.io.IOException;

public interface ITcpListener {

	Server getServer();
	Runnable getRunnable();
	void initialize() throws IOException;

}