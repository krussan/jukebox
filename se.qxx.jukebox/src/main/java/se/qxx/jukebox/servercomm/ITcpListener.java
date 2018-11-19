package se.qxx.jukebox.servercomm;

import se.qxx.jukebox.comm.JukeboxRpcServer;

public interface ITcpListener {

	public JukeboxRpcServer getServer();
	public Runnable getRunnable();

}