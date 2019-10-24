package se.qxx.jukebox.interfaces;

import io.grpc.Server;

import java.io.IOException;

public interface ITcpListener {

	public Server getServer();
	public Runnable getRunnable();
	public void initialize() throws IOException;

}