package se.qxx.jukebox.interfaces;

import io.grpc.Server;

import java.io.IOException;

public interface ITcpListener {

	Server getServer();
	void initialize() throws IOException;
	Runnable getRunnable();

}